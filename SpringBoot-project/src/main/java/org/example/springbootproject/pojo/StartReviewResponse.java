package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发起审核响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartReviewResponse {
    private Integer projectId;
    /** MANUAL / AI_ASSISTED */
    private String reviewMode;
    /** AI 模式时返回任务 ID，人工模式为 null */
    private Integer taskId;
    /** 任务当前状态（AI 模式） */
    private String taskStatus;
    private String message;
}
