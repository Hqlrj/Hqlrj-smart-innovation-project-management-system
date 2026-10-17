package org.example.springbootproject.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiVectorChunk {
    private Integer id;
    private String sourceType;
    private Integer sourceId;
    private Integer projectId;
    private String college;
    private String title;
    private String content;
    private String embedding;
    private LocalDateTime createTime;
}
