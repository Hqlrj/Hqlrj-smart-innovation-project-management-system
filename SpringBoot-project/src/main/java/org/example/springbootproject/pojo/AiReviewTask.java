package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 评审任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewTask {
    private Integer id;
    private Integer projectId;
    private Integer planId;
    /** PENDING / PARSING / REVIEWING / SUMMARIZING / DONE / FAILED */
    private String status;
    /** MANUAL / AI_ASSISTED */
    private String reviewMode;
    /** MANUAL / AI */
    private String mode;
    private Integer userId;
    private Integer sectionTotal;
    private Integer sectionDone;
    private BigDecimal totalScore;
    private String grade;
    private String summary; // 总体评价（纯文本）
    private String fileHash;
    private String modelName;
    private String promptVersion;
    private Integer tokenInput;
    private Integer tokenOutput;
    private Long durationMs;
    private String errorMsg;
    private Integer createdBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
