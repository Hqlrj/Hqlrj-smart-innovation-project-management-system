package org.example.springbootproject.pojo;

import lombok.Data;
import java.util.List;

/**
 * 文档解析结果
 */
@Data
public class DocumentParseResult {
    private String fileName;
    private Integer paragraphCount;
    private Integer charCount;
    private List<DocumentParagraph> paragraphs;
    private String errorMsg;
}
