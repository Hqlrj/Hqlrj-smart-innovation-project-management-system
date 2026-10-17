package org.example.springbootproject.service.impl;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.example.springbootproject.ai.AiCallerContext;
import org.example.springbootproject.ai.RedisChatMemoryStore;
import org.example.springbootproject.ai.SystemAiTools;
import org.example.springbootproject.ai.SystemAssistant;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.LangchainAgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LangchainAgentServiceImpl implements LangchainAgentService {

    @Value("${ai.langchain.enabled:true}")
    private boolean enabled;

    @Value("${ai.llm.api-url}")
    private String apiUrl;

    @Value("${ai.llm.model-name}")
    private String modelName;

    @Value("${ai.llm.temperature}")
    private Double temperature;

    @Autowired
    private RedisChatMemoryStore chatMemoryStore;

    @Autowired
    private SystemAiTools systemAiTools;

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public String chat(String message, String conversationId, String apiKey, User caller) {
        AiCallerContext.set(caller);
        try {
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .baseUrl(openAiBaseUrl(apiUrl))
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    .timeout(Duration.ofSeconds(120))
                    .logRequests(false)
                    .logResponses(false)
                    .build();
            MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                    .id(conversationId)
                    .maxMessages(24)
                    .chatMemoryStore(chatMemoryStore)
                    .build();
            SystemAssistant assistant = AiServices.builder(SystemAssistant.class)
                    .chatLanguageModel(model)
                    .chatMemory(memory)
                    .tools(systemAiTools)
                    .build();
            return assistant.chat(message);
        } finally {
            AiCallerContext.clear();
        }
    }

    private String openAiBaseUrl(String completionsUrl) {
        if (completionsUrl == null || completionsUrl.isBlank()) {
            return "https://api.deepseek.com/v1";
        }
        String url = completionsUrl.trim();
        if (url.endsWith("/chat/completions")) {
            return url.substring(0, url.length() - "/chat/completions".length());
        }
        return url;
    }
}
