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
    /** BACKGROUND / INNOVATION / FEASIBILITY 等 */
    private String sectionKind;
    private String strengths;
    private String weaknesses;
    private String risks;
    private String suggestions;
}
