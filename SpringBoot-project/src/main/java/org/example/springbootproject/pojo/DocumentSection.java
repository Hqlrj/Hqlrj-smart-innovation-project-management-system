package org.example.springbootproject.pojo;

import lombok.Data;

/**
 * 文档章节
 */
@Data
public class DocumentSection {
    private Integer sortOrder;
    private String sectionTitle;
    private Integer sectionLevel;
    private Integer charCount;
    private String content;
    private Double score;
    private Double weight;
}
