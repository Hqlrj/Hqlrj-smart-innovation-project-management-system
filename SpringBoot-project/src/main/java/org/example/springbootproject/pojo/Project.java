package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目实体类
 * 对应数据库中的project表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Integer id; // 项目ID
    private String projectType; // 项目类型
    private String projectName; // 项目名称
    private String projectIntro; // 项目简介
    private String innovationPoints; // 项目创新点
    private String projectBackground; // 项目背景
    private String projectSignificance; // 项目意义
    private String marketDemandAnalysis; // 市场需求分析
    private String keyTechnology; // 项目关键技术介绍
    private String planFilePath; // 计划书文件路径
    private String planFileName; // 计划书文件名
    private String status; // 项目状态：draft-草稿, submitted-已申报, approved-已审批, rejected-已拒绝
    private Integer applicantId; // 申请人ID
    private String applicantName; // 申请人姓名
    private String auditorName; // 审批人姓名
    private String rejectReason; // 驳回原因说明
    private String projectSpace; // 项目空间（暂时空着）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime; // 提交时间
    
    // 项目成员列表（不存储在数据库，用于前端展示）
    private List<ProjectMember> members;
    
    // 项目指导老师列表（不存储在数据库，用于前端展示）
    private List<ProjectAdvisor> advisors;
    
    // 项目所属学院（从申请人获取，不存储在数据库）
    private String applicantCollege;
    
    // 获奖记录列表（不存储在数据库，用于前端展示）
    private List<Award> awards;
    
    // 计划书列表（不存储在数据库，用于前端展示）
    private List<ProjectPlan> plans;
}
