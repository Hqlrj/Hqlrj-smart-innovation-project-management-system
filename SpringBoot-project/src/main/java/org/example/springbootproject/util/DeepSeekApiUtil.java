package org.example.springbootproject.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek API 调用工具类
 * 用于调用DeepSeek API进行AI评审
 */
@Component
public class DeepSeekApiUtil {

    @Value("${ai.llm.api-url}")
    private String apiUrl;

    @Value("${ai.llm.api-key}")
    private String apiKey;

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

    private final OkHttpClient httpClient;
    private final Gson gson;

    public DeepSeekApiUtil() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    /**
     * 调用DeepSeek API进行章节评审
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return AI响应结果
     */
    public String callDeepSeekApi(String systemPrompt, String userPrompt) throws IOException {
        // 构建请求体
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

        // 发送请求
        return sendRequestWithRetry(requestBody);
    }

    /**
     * 发送请求并支持重试
     * @param requestBody 请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    private String sendRequestWithRetry(JsonObject requestBody) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return sendRequest(requestBody);
            } catch (IOException e) {
                lastException = e;
                // 指数退避重试
                if (attempt < maxRetries - 1) {
                    try {
                        Thread.sleep(1000 * (long) Math.pow(2, attempt)); // 2s, 4s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("请求被中断", ie);
                    }
                }
            }
        }
        
        throw new IOException("API调用失败，已重试 " + maxRetries + " 次", lastException);
    }

    /**
     * 发送请求
     * @param requestBody 请求体
     * @return 响应结果
     * @throws IOException IO异常
     */
    private String sendRequest(JsonObject requestBody) throws IOException {
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                requestBody.toString()
        );

        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API请求失败，状态码: " + response.code());
            }

            String responseBody = response.body().string();
            
            // 解析响应
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");
                    if (message.has("content")) {
                        return message.get("content").getAsString();
                    }
                }
            }
            
            throw new IOException("API响应格式错误: " + responseBody);
        }
    }

    /**
     * 检查API Key是否已配置
     * @return 是否已配置
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    /**
     * 验证API Key有效性
     * @return 是否有效
     */
    public boolean validateApiKey() {
        if (!isApiKeyConfigured()) {
            return false;
        }

        try {
            // 发送一个简单的测试请求
            String testResponse = callDeepSeekApi(
                "你是一个测试助手，请只回复'OK'。",
                "请回复'OK'。"
            );
            return testResponse != null && testResponse.contains("OK");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置临时API Key（用于单次请求）
     * @param tempApiKey 临时API Key
     */
    public void setTemporaryApiKey(String tempApiKey) {
        this.apiKey = tempApiKey;
    }
}
