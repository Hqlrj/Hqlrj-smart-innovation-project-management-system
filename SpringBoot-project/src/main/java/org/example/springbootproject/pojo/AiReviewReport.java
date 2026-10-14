package org.example.springbootproject.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AiReviewReport {
    private Integer id;
    private Integer taskId;
    private BigDecimal totalScore;
    private String grade;
    private String summary;
    private String highlights;
    private String mainProblems;
    private String risks;
    private String suggestions;
    private String missingSections;
    private String aiRecommendation;
    private String consistencyNote;
    private String rawJson;
    private LocalDateTime createTime;
}
