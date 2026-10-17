package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Integer id;
    private String projectType;
    private String projectName;
    private String projectIntro;
    private String innovationPoints;
    private String projectBackground;
    private String projectSignificance;
    private String marketDemandAnalysis;
    private String keyTechnology;
    private String planFilePath;
    private String planFileName;
    private Integer applicantId;
    private String applicantName;
    private String status;
    private String auditorName;
    private String rejectReason;
    private String projectSpace;
    private String reviewMode;
    private Integer aiTaskId;
    private String applicantCollege;
    /** 仅 AI 按学号查询时使用：负责人 / 成员 */
    private String relation;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    // 关联对象
    private List<ProjectMember> members;
    private List<ProjectAdvisor> advisors;
    private List<ProjectPlan> plans;
    private List<Award> awards;
}
