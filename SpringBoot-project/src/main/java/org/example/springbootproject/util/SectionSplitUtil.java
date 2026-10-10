package org.example.springbootproject.util;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentParagraph;
import org.example.springbootproject.pojo.DocumentSection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 章节切分工具类
 * 使用正则表达式进行章节切分
 */
@Component
public class SectionSplitUtil {

    // 章节标题正则表达式（支持多种格式）
    private static final Pattern SECTION_PATTERN = Pattern.compile(
        "^(第[一二三四五六七八九十\\d]+[章节部分]|\\d+\\.\\d+|\\d+\\.|[一二三四五六七八九十]、)[\\s\\u3000]*.*",
        Pattern.MULTILINE
    );

    /**
     * 切分章节
     * @param parseResult 文档解析结果
     * @return 章节列表
     */
    public List<DocumentSection> splitSections(DocumentParseResult parseResult) {
        List<DocumentSection> sections = new ArrayList<>();
        
        if (parseResult == null || parseResult.getParagraphs() == null || parseResult.getParagraphs().isEmpty()) {
            System.out.println("文档解析结果为空或没有段落");
            return sections;
        }

        List<DocumentParagraph> paragraphs = parseResult.getParagraphs();
        System.out.println("开始章节切分，总段落数: " + paragraphs.size());
        
        DocumentSection currentSection = null;
        StringBuilder currentContent = new StringBuilder();
        int sectionIndex = 0;
        boolean hasSectionTitle = false;

        for (int i = 0; i < paragraphs.size(); i++) {
            DocumentParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();
            
            // 检查是否是章节标题
            if (isSectionTitle(text)) {
                hasSectionTitle = true;
                // 保存上一个章节
                if (currentSection != null) {
                    currentSection.setContent(currentContent.toString().trim());
                    currentSection.setCharCount(currentContent.length());
                    sections.add(currentSection);
                    System.out.println("保存章节: " + currentSection.getSectionTitle() + ", 字数: " + currentContent.length());
                }
                
                // 创建新章节
                currentSection = new DocumentSection();
                currentSection.setSortOrder(sectionIndex++);
                currentSection.setSectionTitle(text.trim());
                currentSection.setSectionLevel(paragraph.getLevel());
                currentSection.setWeight(calculateWeight(paragraph.getLevel()));
                currentContent = new StringBuilder();
            } else {
                // 添加到当前章节内容
                if (currentSection == null) {
                    // 如果还没有章节标题，创建一个默认章节
                    currentSection = new DocumentSection();
                    currentSection.setSortOrder(sectionIndex++);
                    currentSection.setSectionTitle("文档内容");
                    currentSection.setSectionLevel(0);
                    currentSection.setWeight(0.1);
                    System.out.println("创建默认章节");
                }
                currentContent.append(text).append("\n");
            }
        }

        // 保存最后一个章节
        if (currentSection != null) {
            currentSection.setContent(currentContent.toString().trim());
            currentSection.setCharCount(currentContent.length());
            sections.add(currentSection);
            System.out.println("保存最后章节: " + currentSection.getSectionTitle() + ", 字数: " + currentContent.length());
        }

        // 如果没有找到任何章节标题，创建一个包含所有内容的章节
        if (!hasSectionTitle && !paragraphs.isEmpty()) {
            sections.clear();
            DocumentSection wholeDocument = new DocumentSection();
            wholeDocument.setSortOrder(0);
            wholeDocument.setSectionTitle("完整文档");
            wholeDocument.setSectionLevel(0);
            wholeDocument.setWeight(1.0);
            
            StringBuilder allContent = new StringBuilder();
            for (DocumentParagraph paragraph : paragraphs) {
                allContent.append(paragraph.getText()).append("\n");
            }
            wholeDocument.setContent(allContent.toString().trim());
            wholeDocument.setCharCount(allContent.length());
            sections.add(wholeDocument);
            System.out.println("未找到章节标题，将整个文档作为一个章节处理，总字数: " + allContent.length());
        }
        
        // 如果仍然没有章节，返回空列表（避免空指针）
        if (sections.isEmpty()) {
            System.out.println("章节切分失败：文档没有找到任何可识别的章节标题");
        } else {
            System.out.println("章节切分完成，共 " + sections.size() + " 个章节");
        }

        return sections;
    }

    /**
     * 判断是否是章节标题
     * @param text 文本
     * @return 是否是章节标题
     */
    private boolean isSectionTitle(String text) {
        return SECTION_PATTERN.matcher(text).matches();
    }

    /**
     * 计算章节权重
     * @param level 章节层级
     * @return 权重
     */
    private double calculateWeight(int level) {
        // 一级章节权重最高，逐渐递减
        switch (level) {
            case 1:
                return 0.3;
            case 2:
                return 0.25;
            case 3:
                return 0.2;
            case 4:
                return 0.15;
            default:
                return 0.1;
        }
    }
}
