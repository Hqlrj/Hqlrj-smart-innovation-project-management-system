package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目成员实体类
 * 对应数据库中的project_member表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {
    private Integer id; // 成员ID
    private Integer projectId; // 项目ID
    private String studentId; // 学号
    private String name; // 姓名
    private String gender; // 性别
    private String phone; // 手机号
    private String college; // 所属学院
    private String className; // 班级
    private String introduction; // 介绍
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}
