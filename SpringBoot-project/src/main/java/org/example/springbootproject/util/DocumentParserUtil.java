package org.example.springbootproject.util;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentParagraph;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
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
        if (filePath == null) {
            result.setErrorMsg("文档路径为空");
            return result;
        }
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".doc") && !lower.endsWith(".docx")) {
            return parseDoc(filePath);
        }
        return parseDocx(filePath);
    }

    private DocumentParseResult parseDocx(String filePath) {
        DocumentParseResult result = new DocumentParseResult();
        List<DocumentParagraph> paragraphs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            List<XWPFParagraph> docParagraphs = document.getParagraphs();
            int totalChars = 0;
            for (int i = 0; i < docParagraphs.size(); i++) {
                String text = docParagraphs.get(i).getText();
                if (text == null) {
                    continue;
                }
                text = text.trim();
                if (text.isEmpty()) {
                    continue;
                }
                DocumentParagraph docParagraph = new DocumentParagraph();
                docParagraph.setIndex(i);
                docParagraph.setText(text);
                docParagraph.setLevel(0);
                docParagraph.setCharCount(text.length());
                paragraphs.add(docParagraph);
                totalChars += text.length();
            }
            for (XWPFTable table : document.getTables()) {
                String tableText = flattenTable(table);
                if (tableText.isBlank()) {
                    continue;
                }
                DocumentParagraph tablePara = new DocumentParagraph();
                tablePara.setIndex(paragraphs.size());
                tablePara.setText(tableText);
                tablePara.setLevel(0);
                tablePara.setCharCount(tableText.length());
                paragraphs.add(tablePara);
                totalChars += tableText.length();
            }
            fillResult(result, filePath, paragraphs, totalChars);
        } catch (Exception e) {
            result.setErrorMsg("文档解析失败: " + e.getMessage());
        }
        return result;
    }

    private String flattenTable(XWPFTable table) {
        StringBuilder sb = new StringBuilder();
        for (XWPFTableRow row : table.getRows()) {
            List<String> cells = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = cell.getText() == null ? "" : cell.getText().replace('\n', ' ').trim();
                if (!cellText.isEmpty()) {
                    cells.add(cellText);
                }
            }
            if (!cells.isEmpty()) {
                sb.append(String.join("：", cells)).append('\n');
            }
        }
        return sb.toString().trim();
    }

    private DocumentParseResult parseDoc(String filePath) {
        DocumentParseResult result = new DocumentParseResult();
        List<DocumentParagraph> paragraphs = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            String[] lines = extractor.getParagraphText();
            int totalChars = 0;
            int index = 0;
            for (String line : lines) {
                if (line == null) {
                    continue;
                }
                String text = line.trim();
                if (text.isEmpty()) {
                    continue;
                }
                DocumentParagraph docParagraph = new DocumentParagraph();
                docParagraph.setIndex(index++);
                docParagraph.setText(text);
                docParagraph.setLevel(0);
                docParagraph.setCharCount(text.length());
                paragraphs.add(docParagraph);
                totalChars += text.length();
            }
            fillResult(result, filePath, paragraphs, totalChars);
        } catch (Exception e) {
            result.setErrorMsg("文档解析失败: " + e.getMessage());
        }
        return result;
    }

    private void fillResult(DocumentParseResult result, String filePath,
                            List<DocumentParagraph> paragraphs, int totalChars) {
        result.setParagraphs(paragraphs);
        result.setParagraphCount(paragraphs.size());
        result.setCharCount(totalChars);
        int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        result.setFileName(slash >= 0 ? filePath.substring(slash + 1) : filePath);
    }
}
