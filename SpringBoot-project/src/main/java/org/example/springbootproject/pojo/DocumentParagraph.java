package org.example.springbootproject.pojo;

import lombok.Data;

/**
 * 文档段落
 */
@Data
public class DocumentParagraph {
    private Integer index;
    private String text;
    private Integer level;
    private Integer charCount;
}
