package org.example.springbootproject.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.PostConstruct;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API 调用。API Key 必须作为方法参数传入，禁止改写单例字段，避免并发串 Key。
 */
@Component
public class DeepSeekApiUtil {

    @Value("${ai.llm.api-url}")
    private String apiUrl;

    @Value("${ai.llm.api-key:}")
    private String defaultApiKey;

    @Value("${ai.llm.model-name}")
    private String modelName;

    @Value("${ai.llm.temperature}")
    private Double temperature;

    @Value("${ai.llm.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.llm.timeout-seconds}")
    private Integer timeoutSeconds;

    @Value("${ai.llm.max-retries}")
    private Integer maxRetries;

    private OkHttpClient httpClient;

    @PostConstruct
    public void init() {
        int timeout = timeoutSeconds != null && timeoutSeconds > 0 ? timeoutSeconds : 30;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
    }

    public String callDeepSeekApi(String systemPrompt, String userPrompt) throws IOException {
        return callDeepSeekApi(systemPrompt, userPrompt, defaultApiKey);
    }

    public String callDeepSeekApi(String systemPrompt, String userPrompt, String apiKey) throws IOException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("API Key 未配置");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);

        JsonArray messages = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userPrompt);
        messages.add(userMessage);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("stream", false);

        JsonObject responseFormat = new JsonObject();
        responseFormat.addProperty("type", "json_object");
        requestBody.add("response_format", responseFormat);

        return sendRequestWithRetry(requestBody, apiKey.trim());
    }

    private String sendRequestWithRetry(JsonObject requestBody, String apiKey) throws IOException {
        IOException lastException = null;
        int retries = maxRetries != null && maxRetries > 0 ? maxRetries : 3;

        for (int attempt = 0; attempt < retries; attempt++) {
            try {
                return sendRequest(requestBody, apiKey);
            } catch (IOException e) {
                lastException = e;
                if (attempt < retries - 1) {
                    try {
                        Thread.sleep(1000L * (long) Math.pow(2, attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
        }

        throw new IOException("API调用失败，已重试 " + retries + " 次", lastException);
    }

    private String sendRequest(JsonObject requestBody, String apiKey) throws IOException {
        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errBody = response.body() != null ? response.body().string() : "";
                throw new IOException("API请求失败，状态码: " + response.code() + " " + errBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");
                    if (message.has("content")) {
                        return extractJsonObject(message.get("content").getAsString());
                    }
                }
            }

            throw new IOException("API响应格式错误: " + responseBody);
        }
    }

    /**
     * 去掉模型常见的 markdown 代码块，只保留 JSON 对象。
     */
    public static String extractJsonObject(String content) {
        if (content == null) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            int fence = text.lastIndexOf("```");
            if (fence >= 0) {
                text = text.substring(0, fence);
            }
            text = text.trim();
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    public boolean isApiKeyConfigured() {
        return defaultApiKey != null && !defaultApiKey.trim().isEmpty();
    }

    public boolean isApiKeyConfigured(String apiKey) {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    public boolean validateApiKey() {
        return validateApiKey(defaultApiKey);
    }

    public boolean validateApiKey(String apiKey) {
        if (!isApiKeyConfigured(apiKey)) {
            return false;
        }
        try {
            String testResponse = callDeepSeekApi(
                    "你是一个测试助手，请只回复JSON：{\"ok\":true}。",
                    "请按要求输出。",
                    apiKey
            );
            return testResponse != null && testResponse.contains("ok");
        } catch (Exception e) {
            return false;
        }
    }
}
