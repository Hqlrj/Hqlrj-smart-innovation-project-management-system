package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 计划书实体类
 * 对应数据库中的projecdct_plan表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPlan {
    private Integer id; // 计划书ID
    private Integer projectId; // 项目ID
    private String filePath; // 文件路径
    private String fileName; // 文件名
    private Long fileSize; // 文件大小（字节）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime; // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime; // 更新时间
}

