package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI回复响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReplyResponse {
    private String reply;
}