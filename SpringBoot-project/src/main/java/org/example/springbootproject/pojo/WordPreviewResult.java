package org.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Word 计划书在线预览结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordPreviewResult {
    private String fileName;
    private String html;
}
