package org.example.springbootproject.pojo;

import lombok.Data;

/**
 * AI 评审章节
 */
@Data
public class AiReviewSection {
    private Integer id;
    private Integer taskId;
    private Integer sortOrder;
    private String sectionTitle;
    private Integer sectionLevel;
    private Integer charCount;
    private Double score;
    private String dimensions;
    private String strengths;
    private String weaknesses;
    private String risks;
    private String suggestions;
    private String rawResponse;
    private String status;
    private String errorMsg;
    private java.time.LocalDateTime createTime;
}
