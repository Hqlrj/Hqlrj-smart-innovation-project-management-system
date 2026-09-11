package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目指导老师实体类
 * 对应数据库中的project_advisor表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAdvisor {
    private Integer id; // 指导老师ID
    private Integer projectId; // 项目ID
    private String name; // 姓名
    private String gender; // 性别：男、女
    private String phone; // 手机号
    private String college; // 所属学院
    private String introduction; // 介绍
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}

