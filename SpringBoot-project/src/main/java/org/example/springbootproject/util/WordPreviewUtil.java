package org.example.springbootproject.util;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 将 Word 计划书转为 HTML，供浏览器直接预览，无需下载。
 */
@Component
public class WordPreviewUtil {

    public String convertToHtml(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("计划书文件不存在");
        }
        String name = file.getName().toLowerCase();
        if (name.endsWith(".docx")) {
            return convertDocx(file);
        }
        if (name.endsWith(".doc")) {
            return convertDoc(file);
        }
        throw new IllegalArgumentException("仅支持预览 .doc / .docx 格式");
    }

    private String convertDocx(File file) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"word-preview-body\">");
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    appendParagraph(html, paragraph);
                } else if (element instanceof XWPFTable table) {
                    appendTable(html, table);
                }
            }
        }
        html.append("</div>");
        return html.toString();
    }

    private void appendParagraph(StringBuilder html, XWPFParagraph paragraph) {
        String style = paragraph.getStyle();
        String tag = "p";
        if (style != null) {
            String lower = style.toLowerCase();
            if (lower.contains("heading1") || "1".equals(style)) {
                tag = "h1";
            } else if (lower.contains("heading2") || "2".equals(style)) {
                tag = "h2";
            } else if (lower.contains("heading3") || "3".equals(style)) {
                tag = "h3";
            } else if (lower.contains("heading4") || "4".equals(style)) {
                tag = "h4";
            }
        }

        html.append("<").append(tag);
        String align = mapAlign(paragraph.getAlignment());
        if (align != null) {
            html.append(" style=\"text-align:").append(align).append(";\"");
        }
        html.append(">");

        boolean hasContent = false;
        for (XWPFRun run : paragraph.getRuns()) {
            List<XWPFPicture> pictures = run.getEmbeddedPictures();
            if (pictures != null) {
                for (XWPFPicture picture : pictures) {
                    String img = toDataUri(picture);
                    if (img != null) {
                        html.append("<img src=\"").append(img).append("\" alt=\"图片\" />");
                        hasContent = true;
                    }
                }
            }
            String text = run.getText(0);
            if (text == null || text.isEmpty()) {
                continue;
            }
            hasContent = true;
            boolean bold = run.isBold();
            boolean italic = run.isItalic();
            boolean underline = run.getUnderline() != null
                    && run.getUnderline() != org.apache.poi.xwpf.usermodel.UnderlinePatterns.NONE;
            if (bold) {
                html.append("<strong>");
            }
            if (italic) {
                html.append("<em>");
            }
            if (underline) {
                html.append("<u>");
            }
            html.append(escapeHtml(text).replace("\n", "<br/>"));
            if (underline) {
                html.append("</u>");
            }
            if (italic) {
                html.append("</em>");
            }
            if (bold) {
                html.append("</strong>");
            }
        }

        if (!hasContent) {
            html.append("&nbsp;");
        }
        html.append("</").append(tag).append(">");
    }

    private void appendTable(StringBuilder html, XWPFTable table) {
        html.append("<table>");
        for (XWPFTableRow row : table.getRows()) {
            html.append("<tr>");
            for (XWPFTableCell cell : row.getTableCells()) {
                html.append("<td>");
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    appendParagraph(html, paragraph);
                }
                html.append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table>");
    }

    private String mapAlign(ParagraphAlignment alignment) {
        if (alignment == null) {
            return null;
        }
        return switch (alignment) {
            case CENTER -> "center";
            case RIGHT -> "right";
            case BOTH -> "justify";
            default -> "left";
        };
    }

    private String toDataUri(XWPFPicture picture) {
        try {
            XWPFPictureData data = picture.getPictureData();
            if (data == null) {
                return null;
            }
            String mime = data.getPackagePart().getContentType();
            String base64 = Base64.getEncoder().encodeToString(data.getData());
            return "data:" + mime + ";base64," + base64;
        } catch (Exception e) {
            return null;
        }
    }

    private String convertDoc(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis)) {
            Document htmlDocument = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument();
            WordToHtmlConverter converter = new WordToHtmlConverter(htmlDocument);
            converter.setPicturesManager(new PicturesManager() {
                @Override
                public String savePicture(byte[] content, PictureType pictureType, String suggestedName,
                                          float widthInches, float heightInches) {
                    String mime = mimeOf(pictureType);
                    return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(content);
                }
            });
            converter.processDocument(document);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(new DOMSource(converter.getDocument()), new StreamResult(out));
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private String mimeOf(PictureType pictureType) {
        if (pictureType == null) {
            return "image/png";
        }
        String ext = pictureType.getExtension();
        if (ext == null) {
            return "image/png";
        }
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            default -> "image/png";
        };
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
