package org.example.springbootproject.pojo;

import lombok.Data;

/**
 * 发起审核请求
 */
@Data
public class StartReviewRequest {
    /** MANUAL 或 AI */
    private String mode;
    /** DeepSeek API Key（前端传递） */
    private String apiKey;
}
