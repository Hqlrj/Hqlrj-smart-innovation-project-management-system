package org.example.springbootproject.util;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 将 Word 转为 PDF，优先用 LibreOffice（版式最接近 Word），失败再用 POI 转换器。
 */
@Component
public class WordToPdfUtil {

    private static final Logger log = LoggerFactory.getLogger(WordToPdfUtil.class);

    public File toPdf(File source) throws Exception {
        if (source == null || !source.exists() || !source.isFile()) {
            throw new IllegalArgumentException("源文件不存在");
        }
        String name = source.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            return source;
        }

        File cacheDir = new File(source.getParentFile(), "pdf-cache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IllegalStateException("无法创建 PDF 缓存目录");
        }
        String base = source.getName().replaceAll("\\.[^.]+$", "");
        File cached = new File(cacheDir, base + "-" + source.lastModified() + ".pdf");
        if (cached.exists() && cached.length() > 0 && cached.lastModified() >= source.lastModified()) {
            return cached;
        }

        File tempPdf = new File(cacheDir, base + "-tmp-" + System.currentTimeMillis() + ".pdf");
        Exception lastError = null;

        if (convertByLibreOffice(source, cacheDir, tempPdf)) {
            return replaceCache(tempPdf, cached);
        }

        if (name.endsWith(".docx")) {
            try {
                convertByPoi(source, tempPdf);
                return replaceCache(tempPdf, cached);
            } catch (Exception e) {
                lastError = e;
                log.warn("POI 转 PDF 失败: {}", e.getMessage());
            }
        }

        throw new IllegalStateException("Word 转 PDF 失败，请确认已安装 LibreOffice，或上传 .docx。"
                + (lastError != null ? lastError.getMessage() : ""));
    }

    private File replaceCache(File tempPdf, File cached) throws Exception {
        if (cached.exists()) {
            Files.deleteIfExists(cached.toPath());
        }
        if (!tempPdf.renameTo(cached)) {
            Files.copy(tempPdf.toPath(), cached.toPath());
            Files.deleteIfExists(tempPdf.toPath());
        }
        return cached;
    }

    private boolean convertByLibreOffice(File source, File outDir, File expectedPdf) {
        File soffice = findSoffice();
        if (soffice == null) {
            return false;
        }
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(soffice.getAbsolutePath());
            cmd.add("--headless");
            cmd.add("--norestore");
            cmd.add("--convert-to");
            cmd.add("pdf");
            cmd.add("--outdir");
            cmd.add(outDir.getAbsolutePath());
            cmd.add(source.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.directory(outDir);
            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            File loOutput = new File(outDir, source.getName().replaceAll("\\.[^.]+$", "") + ".pdf");
            if (loOutput.exists() && loOutput.length() > 0) {
                if (!loOutput.getAbsolutePath().equals(expectedPdf.getAbsolutePath())) {
                    Files.copy(loOutput.toPath(), expectedPdf.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Files.deleteIfExists(loOutput.toPath());
                }
                return expectedPdf.exists() && expectedPdf.length() > 0;
            }
        } catch (Exception e) {
            log.warn("LibreOffice 转 PDF 失败: {}", e.getMessage());
        }
        return false;
    }

    private File findSoffice() {
        String home = System.getenv("LIBREOFFICE_HOME");
        if (home != null && !home.isBlank()) {
            File fromHome = new File(home, "program/soffice.exe");
            if (fromHome.exists()) {
                return fromHome;
            }
            fromHome = new File(home, "program/soffice");
            if (fromHome.exists()) {
                return fromHome;
            }
        }
        String[] names = {
                "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                "C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe",
                "/usr/bin/soffice",
                "/usr/lib/libreoffice/program/soffice"
        };
        for (String name : names) {
            File file = new File(name);
            if (file.exists()) {
                return file;
            }
        }
        String path = System.getenv("PATH");
        if (path != null) {
            String[] dirs = path.split(File.pathSeparator);
            for (String dir : dirs) {
                File exe = new File(dir, "soffice.exe");
                if (exe.exists()) {
                    return exe;
                }
                File bin = new File(dir, "soffice");
                if (bin.exists()) {
                    return bin;
                }
            }
        }
        return null;
    }

    private void convertByPoi(File source, File target) throws Exception {
        try (InputStream in = new FileInputStream(source);
             XWPFDocument document = new XWPFDocument(in);
             OutputStream out = new FileOutputStream(target)) {
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);
        }
        if (!target.exists() || target.length() == 0) {
            throw new IllegalStateException("生成的 PDF 为空");
        }
    }
}
