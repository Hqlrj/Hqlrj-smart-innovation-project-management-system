package org.example.springbootproject.util;

import org.example.springbootproject.pojo.PlanValidationResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 文件验证工具类
 * 用于验证上传的计划书文件
 */
@Component
public class FileValidationUtil {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".docx", ".doc"};
    
    /**
     * 验证文件
     * @param file 上传的文件
     * @return 验证结果
     */
    public static PlanValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new PlanValidationResult(false, "文件为空");
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return new PlanValidationResult(false, "文件大小超过限制（最大10MB）");
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return new PlanValidationResult(false, "文件名无效");
        }
        
        String lowerCaseName = originalFilename.toLowerCase();
        boolean hasValidExtension = false;
        
        // 检查是否为允许的文件类型
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerCaseName.endsWith(ext)) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            return new PlanValidationResult(false, "文件格式不支持，仅支持.docx和.doc格式的Word文档");
        }
        
        // 计算文件哈希
        try {
            String fileHash = calculateFileHash(file);
            return new PlanValidationResult(true, null, fileHash);
        } catch (IOException e) {
            // 哈希计算失败不影响文件上传，只是不保存文件指纹
            return new PlanValidationResult(true, null, null);
        }
    }
    
    /**
     * 计算文件哈希（SHA-256）
     * @param file 文件
     * @return 哈希值
     * @throws IOException IO异常
     */
    private static String calculateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = file.getBytes();
            byte[] hashBytes = digest.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            throw new IOException("哈希计算失败", e);
        }
    }
}
