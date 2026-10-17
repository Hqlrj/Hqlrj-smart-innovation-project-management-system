package org.example.springbootproject.service;

import org.example.springbootproject.pojo.User;

public interface LangchainAgentService {

    boolean enabled();

    String chat(String message, String conversationId, String apiKey, User caller);
}
