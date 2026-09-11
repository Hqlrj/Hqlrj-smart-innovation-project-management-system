package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库中的user表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id; // 主键ID
    private String studentId; // 学号/职工号
    private String name; // 姓名
    private String phone; // 手机号
    private String roleId; // 角色编号
    @JsonIgnore // 返回JSON时忽略密码字段，保证安全性
    private String password; // 密码
    private String role; // 角色名称
    private String college; // 学院
    private String avatar; // 头像路径
    private String status; // 当前状态
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 格式化日期时间
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime; // 最后登录时间
}

