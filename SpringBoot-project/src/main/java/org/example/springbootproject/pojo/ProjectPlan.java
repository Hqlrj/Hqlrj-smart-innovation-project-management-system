package org.example.springbootproject.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目计划书实体类
 */
@Data
public class ProjectPlan {
    private Integer id;
    private Integer projectId;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String fileHash;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
