package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.example.springbootproject.pojo.AiChatRequest;
import org.example.springbootproject.pojo.AiChatResponse;
import org.example.springbootproject.service.AiChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AI聊天服务实现类
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    @Value("${ai.llm.api-url}")
    private String apiUrl;

    @Value("${ai.llm.model-name}")
    private String modelName;

    @Value("${ai.llm.temperature}")
    private Double temperature;

    @Value("${ai.llm.max-tokens}")
    private Integer maxTokens;

    @Value("${ai.llm.timeout-seconds}")
    private Integer timeoutSeconds;

    private final OkHttpClient httpClient;
    private final Gson gson;

    // 简单的对话历史存储（生产环境应该使用数据库）
    private final Map<String, JsonArray> conversationHistory = new HashMap<>();

    public AiChatServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return chatWithUserKey(request, null);
    }
    
    @Override
    public AiChatResponse chatWithUserKey(AiChatRequest request, String userApiKey) {
        if (userApiKey == null || userApiKey.trim().isEmpty()) {
            return new AiChatResponse(null, request.getConversationId(), false, "请先在设置中配置API Key");
        }
        
        return doChat(request, userApiKey);
    }
    
    private AiChatResponse doChat(AiChatRequest request, String apiKey) {
        try {
            String conversationId = request.getConversationId();
            
            // 如果没有对话ID，创建新的
            if (conversationId == null || conversationId.isEmpty()) {
                conversationId = UUID.randomUUID().toString();
            }

            // 获取或创建对话历史
            JsonArray messages = conversationHistory.getOrDefault(conversationId, new JsonArray());
            
            // 如果是新对话，添加系统提示
            if (messages.size() == 0) {
                JsonObject systemMessage = new JsonObject();
                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", "你是一个智能创新创业项目管理系统的AI助手，专门帮助审核人和管理员处理项目审核相关问题。请用专业、友好的语气回答问题。");
                messages.add(systemMessage);
            }

            // 添加用户消息
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", request.getMessage());
            messages.add(userMessage);

            // 构建请求体
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", modelName);
            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", temperature);
            requestBody.addProperty("max_tokens", maxTokens);
            requestBody.addProperty("stream", false);

            // 发送请求
            String response = sendRequest(requestBody, apiKey);

            // 解析响应
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            
            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                if (choice.has("message")) {
                    JsonObject message = choice.getAsJsonObject("message");
                    if (message.has("content")) {
                        String reply = message.get("content").getAsString();
                        
                        // 添加AI回复到对话历史
                        JsonObject assistantMessage = new JsonObject();
                        assistantMessage.addProperty("role", "assistant");
                        assistantMessage.addProperty("content", reply);
                        messages.add(assistantMessage);
                        
                        // 保存对话历史
                        conversationHistory.put(conversationId, messages);
                        
                        return new AiChatResponse(reply, conversationId, true, null);
                    }
                }
            }

            return new AiChatResponse(null, conversationId, false, "API响应格式错误");

        } catch (Exception e) {
            return new AiChatResponse(null, request.getConversationId(), false, "AI聊天失败: " + e.getMessage());
        }
    }

    /**
     * 发送请求到DeepSeek API
     */
    private String sendRequest(JsonObject requestBody, String apiKey) throws IOException {
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
            return responseBody;
        }
    }
}