package org.example.springbootproject.service;

import org.example.springbootproject.pojo.AiChatRequest;
import org.example.springbootproject.pojo.AiChatResponse;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    /**
     * 发送消息给AI并获取回复
     * @param request 聊天请求
     * @return 聊天响应
     */
    AiChatResponse chat(AiChatRequest request);
    
    /**
     * 发送消息给AI并获取回复（使用用户自己的API Key）
     * @param request 聊天请求
     * @param userApiKey 用户的API Key
     * @return 聊天响应
     */
    AiChatResponse chatWithUserKey(AiChatRequest request, String userApiKey);
}