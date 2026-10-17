package org.example.springbootproject.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.example.springbootproject.pojo.AiChatRequest;
import org.example.springbootproject.pojo.AiChatResponse;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.AiChatService;
import org.example.springbootproject.service.AiToolExecutor;
import org.example.springbootproject.service.LangchainAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI聊天服务实现类
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);
    private static final int MAX_TOOL_ROUNDS = 3;
    private static final String SYSTEM_PROMPT = """
            你是智能创新创业项目管理系统的AI助手。
            问计划书/创新点：先 search_projects_by_name，再 search_documents(带 projectId)。
            根据检索到的少量正文片段归纳，最多3条、每条一句话，不要复述整篇。
            不要向用户要学号。
            """;

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

    @Autowired
    private AiToolExecutor toolExecutor;

    @Autowired
    private LangchainAgentService langchainAgentService;

    private final OkHttpClient httpClient;
    private final Gson gson;

    private final Map<String, JsonArray> conversationHistory = new HashMap<>();

    public AiChatServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return chatWithUserKey(request, null, null);
    }
    
    @Override
    public AiChatResponse chatWithUserKey(AiChatRequest request, String userApiKey, User caller) {
        if (userApiKey == null || userApiKey.trim().isEmpty()) {
            return new AiChatResponse(null, request.getConversationId(), false, "请先在设置中配置API Key");
        }
        try {
            String conversationId = ensureConversation(request);
            if (langchainAgentService.enabled()) {
                try {
                    String agentReply = langchainAgentService.chat(
                            request.getMessage(), conversationId, userApiKey, caller);
                    if (agentReply != null && !agentReply.isBlank()) {
                        return new AiChatResponse(agentReply, conversationId, true, null);
                    }
                } catch (Exception e) {
                    log.warn("LangChain Agent 失败，回退 Function Calling: {}", e.getMessage());
                }
            }
            JsonArray messages = prepareMessages(conversationId, request.getMessage());
            String reply = runToolLoop(messages, userApiKey, caller);
            if (reply == null || reply.isBlank()) {
                return new AiChatResponse(null, conversationId, false, "API响应格式错误");
            }
            conversationHistory.put(conversationId, messages);
            return new AiChatResponse(reply, conversationId, true, null);
        } catch (Exception e) {
            return new AiChatResponse(null, request.getConversationId(), false, "AI聊天失败: " + e.getMessage());
        }
    }

    @Override
    public void streamWithUserKey(AiChatRequest request, String userApiKey, User caller,
                                  Consumer<String> onDelta,
                                  Consumer<String> onComplete,
                                  Consumer<String> onError) {
        if (userApiKey == null || userApiKey.trim().isEmpty()) {
            onError.accept("请先在设置中配置API Key");
            return;
        }
        try {
            String conversationId = ensureConversation(request);
            if (langchainAgentService.enabled()) {
                try {
                    String agentReply = langchainAgentService.chat(
                            request.getMessage(), conversationId, userApiKey, caller);
                    if (agentReply != null && !agentReply.isBlank()) {
                        onDelta.accept(agentReply);
                        onComplete.accept(conversationId);
                        return;
                    }
                } catch (Exception e) {
                    log.warn("LangChain Agent 失败，回退 Function Calling: {}", e.getMessage());
                }
            }
            JsonArray messages = prepareMessages(conversationId, request.getMessage());
            String reply = runToolLoop(messages, userApiKey, caller);
            if (reply == null || reply.isBlank()) {
                onError.accept("API没有返回有效内容");
                return;
            }
            conversationHistory.put(conversationId, messages);
            onDelta.accept(reply);
            onComplete.accept(conversationId);
        } catch (Exception e) {
            onError.accept("AI聊天失败: " + e.getMessage());
        }
    }

    private String ensureConversation(AiChatRequest request) {
        String conversationId = request.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        return conversationId;
    }

    private JsonArray prepareMessages(String conversationId, String userText) {
        JsonArray messages = conversationHistory.getOrDefault(conversationId, new JsonArray());
        if (messages.size() == 0) {
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", SYSTEM_PROMPT);
            messages.add(systemMessage);
        } else {
            JsonObject first = messages.get(0).getAsJsonObject();
            if (first.has("role") && "system".equals(first.get("role").getAsString())) {
                first.addProperty("content", SYSTEM_PROMPT);
            }
        }
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", userText);
        messages.add(userMessage);
        return messages;
    }

    private String runToolLoop(JsonArray messages, String apiKey, User caller) throws IOException {
        String lastContent = null;
        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            JsonObject requestBody = baseRequest(messages, false);
            requestBody.add("tools", toolExecutor.toolDefinitions());
            requestBody.addProperty("tool_choice", "auto");

            JsonObject jsonResponse = JsonParser.parseString(sendRequest(requestBody, apiKey)).getAsJsonObject();
            if (!jsonResponse.has("choices") || jsonResponse.getAsJsonArray("choices").size() == 0) {
                break;
            }
            JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (!choice.has("message")) {
                break;
            }
            JsonObject message = choice.getAsJsonObject("message");
            messages.add(message);

            JsonArray toolCalls = message.has("tool_calls") && message.get("tool_calls").isJsonArray()
                    ? message.getAsJsonArray("tool_calls")
                    : new JsonArray();
            if (toolCalls.size() > 0) {
                for (JsonElement element : toolCalls) {
                    JsonObject call = element.getAsJsonObject();
                    String toolCallId = call.has("id") ? call.get("id").getAsString() : "";
                    JsonObject function = call.getAsJsonObject("function");
                    String name = function.get("name").getAsString();
                    String arguments = function.has("arguments") ? function.get("arguments").getAsString() : "{}";
                    String result = toolExecutor.execute(name, arguments, caller);
                    JsonObject toolMessage = new JsonObject();
                    toolMessage.addProperty("role", "tool");
                    toolMessage.addProperty("tool_call_id", toolCallId);
                    toolMessage.addProperty("content", result);
                    messages.add(toolMessage);
                }
                continue;
            }
            if (message.has("content") && !message.get("content").isJsonNull()) {
                lastContent = message.get("content").getAsString();
            }
            break;
        }
        if (lastContent == null || lastContent.isBlank()) {
            JsonObject fallback = JsonParser.parseString(sendRequest(baseRequest(messages, false), apiKey)).getAsJsonObject();
            if (fallback.has("choices") && fallback.getAsJsonArray("choices").size() > 0) {
                JsonObject message = fallback.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message");
                if (message != null && message.has("content") && !message.get("content").isJsonNull()) {
                    lastContent = message.get("content").getAsString();
                    messages.add(message);
                }
            }
        }
        return lastContent;
    }

    private JsonObject baseRequest(JsonArray messages, boolean stream) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelName);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", temperature);
        requestBody.addProperty("max_tokens", maxTokens);
        requestBody.addProperty("stream", stream);
        return requestBody;
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
                String errBody = response.body() != null ? response.body().string() : "";
                throw new IOException("API请求失败，状态码: " + response.code() + " " + errBody);
            }
            return response.body().string();
        }
    }
}
