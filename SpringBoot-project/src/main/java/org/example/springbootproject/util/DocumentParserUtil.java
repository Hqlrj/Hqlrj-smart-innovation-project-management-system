package org.example.springbootproject.util;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析工具类
 * 用于解析Word文档
 */
@Component
public class DocumentParserUtil {

    /**
     * 解析Word文档
     * @param filePath 文件路径
     * @return 解析结果
     */
    public DocumentParseResult parseDocument(String filePath) throws IOException {
        DocumentParseResult result = new DocumentParseResult();
        List<DocumentParagraph> paragraphs = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            List<XWPFParagraph> docParagraphs = document.getParagraphs();
            int totalChars = 0;
            
            for (int i = 0; i < docParagraphs.size(); i++) {
                XWPFParagraph para = docParagraphs.get(i);
                String text = para.getText().trim();
                
                if (!text.isEmpty()) {
                    DocumentParagraph docParagraph = new DocumentParagraph();
                    docParagraph.setIndex(i);
                    docParagraph.setText(text);
                    docParagraph.setLevel(0); // 简化处理，使用默认层级
                    docParagraph.setCharCount(text.length());
                    
                    paragraphs.add(docParagraph);
                    totalChars += text.length();
                }
            }
            
            result.setParagraphs(paragraphs);
            result.setParagraphCount(paragraphs.size());
            result.setCharCount(totalChars);
            result.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));
            
        } catch (Exception e) {
            result.setErrorMsg("文档解析失败: " + e.getMessage());
        }
        
        return result;
    }
}
