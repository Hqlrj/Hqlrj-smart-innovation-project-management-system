package org.example.springbootproject.util;

import org.example.springbootproject.pojo.DocumentParagraph;
import org.example.springbootproject.pojo.DocumentParseResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 计划书轻量清洗：去噪声、统一全半角、去掉页码/目录行、合并被 Word 拆断的句子。
 * 不脱敏，避免破坏评审 evidence 所需的原文。
 */
@Component
public class DataCleaningUtil {

    private static final Pattern ZERO_WIDTH = Pattern.compile("[\\u200B\\u200C\\u200D\\uFEFF\\u00AD]");
    private static final Pattern PAGE_NUMBER = Pattern.compile(
            "^(\\d{1,4}|[-–—]?\\s*\\d{1,4}\\s*[-–—]?|第\\s*\\d{1,4}\\s*页|\\d{1,4}\\s*/\\s*\\d{1,4})$"
    );
    private static final Pattern TOC_DOTS = Pattern.compile("[.·•…]{5,}");
    private static final Pattern SECTION_TITLE = Pattern.compile(
            "^(第[一二三四五六七八九十\\d]+[章节部分]|\\d+\\.\\d+|\\d+\\.|[一二三四五六七八九十]、).*"
    );
    private static final String END_PUNCT = "。！？；：.!?;:";

    public DocumentParseResult clean(DocumentParseResult parseResult) {
        DocumentParseResult result = new DocumentParseResult();
        if (parseResult == null || parseResult.getParagraphs() == null) {
            result.setErrorMsg("没有可清洗的解析结果");
            return result;
        }

        List<DocumentParagraph> normalized = new ArrayList<>();
        for (DocumentParagraph paragraph : parseResult.getParagraphs()) {
            if (paragraph == null || paragraph.getText() == null) {
                continue;
            }
            String text = normalizeLine(paragraph.getText());
            if (text.isEmpty() || isNoiseLine(text)) {
                continue;
            }
            DocumentParagraph cleaned = new DocumentParagraph();
            cleaned.setIndex(paragraph.getIndex());
            cleaned.setLevel(paragraph.getLevel());
            cleaned.setText(text);
            cleaned.setCharCount(text.length());
            normalized.add(cleaned);
        }

        List<DocumentParagraph> merged = mergeSoftLineBreaks(normalized);
        int totalChars = 0;
        for (int i = 0; i < merged.size(); i++) {
            DocumentParagraph paragraph = merged.get(i);
            paragraph.setIndex(i);
            totalChars += paragraph.getCharCount() == null ? 0 : paragraph.getCharCount();
        }

        result.setFileName(parseResult.getFileName());
        result.setParagraphs(merged);
        result.setParagraphCount(merged.size());
        result.setCharCount(totalChars);
        return result;
    }

    private String normalizeLine(String raw) {
        String text = ZERO_WIDTH.matcher(raw).replaceAll("");
        text = toHalfWidth(text);
        text = text.replace('\u00A0', ' ').replace('\u3000', ' ');
        text = text.replaceAll("[ \\t\\f\\r]+", " ").trim();
        return text;
    }

    private boolean isNoiseLine(String text) {
        if (PAGE_NUMBER.matcher(text).matches()) {
            return true;
        }
        if (TOC_DOTS.matcher(text).find() && text.length() <= 80) {
            return true;
        }
        return text.length() == 1 && ".-—–_".indexOf(text.charAt(0)) >= 0;
    }

    private List<DocumentParagraph> mergeSoftLineBreaks(List<DocumentParagraph> paragraphs) {
        List<DocumentParagraph> merged = new ArrayList<>();
        for (DocumentParagraph current : paragraphs) {
            if (merged.isEmpty()) {
                merged.add(copy(current));
                continue;
            }
            DocumentParagraph last = merged.get(merged.size() - 1);
            if (shouldMerge(last.getText(), current.getText())) {
                String combined = last.getText() + current.getText();
                last.setText(combined);
                last.setCharCount(combined.length());
            } else {
                merged.add(copy(current));
            }
        }
        return merged;
    }

    /**
     * 上一行不像完整句（不以句末标点结束），且本行不是章节标题，视为 Word 软换行。
     */
    private boolean shouldMerge(String previous, String current) {
        if (previous == null || previous.isEmpty() || current == null || current.isEmpty()) {
            return false;
        }
        if (SECTION_TITLE.matcher(current).matches() || SECTION_TITLE.matcher(previous).matches()) {
            return false;
        }
        char last = previous.charAt(previous.length() - 1);
        if (END_PUNCT.indexOf(last) >= 0) {
            return false;
        }
        return previous.length() < 80 || current.length() < 40;
    }

    private DocumentParagraph copy(DocumentParagraph source) {
        DocumentParagraph target = new DocumentParagraph();
        target.setIndex(source.getIndex());
        target.setLevel(source.getLevel());
        target.setText(source.getText());
        target.setCharCount(source.getCharCount());
        return target;
    }

    private String toHalfWidth(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\u3000') {
                sb.append(' ');
            } else if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else if (c >= 'Ａ' && c <= 'Ｚ') {
                sb.append((char) (c - 'Ａ' + 'A'));
            } else if (c >= 'ａ' && c <= 'ｚ') {
                sb.append((char) (c - 'ａ' + 'a'));
            } else {
                switch (c) {
                    case '，' -> sb.append(',');
                    case '；' -> sb.append(';');
                    case '：' -> sb.append(':');
                    case '！' -> sb.append('!');
                    case '？' -> sb.append('?');
                    case '（' -> sb.append('(');
                    case '）' -> sb.append(')');
                    case '【' -> sb.append('[');
                    case '】' -> sb.append(']');
                    default -> sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}
