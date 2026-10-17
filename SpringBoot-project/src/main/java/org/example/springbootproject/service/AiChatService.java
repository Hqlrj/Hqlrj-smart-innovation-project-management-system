package org.example.springbootproject.service;

import org.example.springbootproject.pojo.AiChatRequest;
import org.example.springbootproject.pojo.AiChatResponse;
import org.example.springbootproject.pojo.User;

/**
 * AI聊天服务接口
 */
public interface AiChatService {
    /**
     * 发送消息给AI并获取回复
     */
    AiChatResponse chat(AiChatRequest request);
    
    /**
     * 发送消息给AI并获取回复（使用用户自己的API Key，可按权限查询系统数据）
     */
    AiChatResponse chatWithUserKey(AiChatRequest request, String userApiKey, User caller);

    /**
     * 流式对话：按增量回调文本，结束后回调完整会话 ID。
     */
    void streamWithUserKey(AiChatRequest request, String userApiKey, User caller,
                           java.util.function.Consumer<String> onDelta,
                           java.util.function.Consumer<String> onComplete,
                           java.util.function.Consumer<String> onError);
}
