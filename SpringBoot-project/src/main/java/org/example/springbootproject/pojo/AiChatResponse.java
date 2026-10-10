package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    /**
     * AI回复消息
     */
    private String reply;
    
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String error;
}