package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 获奖记录实体类
 * 对应数据库中的award表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Award {
    private Integer id; // 获奖记录ID
    private Integer projectId; // 项目ID
    private String projectName; // 项目名称
    private String competitionName; // 比赛名称
    private String competitionLevel; // 比赛级别：校级、省级、国家级等
    private String awardLevel; // 获奖等级：一等奖、二等奖、三等奖等
    private String awardCertificate; // 获奖证明（图片路径）
    private String status; // 审批状态：pending-未审批, approved-已审批通过, rejected-已审批未通过
    private String rejectReason; // 未通过原因说明
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}
