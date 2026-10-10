package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 对话ID（可选，用于多轮对话）
     */
    private String conversationId;
}