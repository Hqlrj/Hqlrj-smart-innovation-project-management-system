package org.example.springbootproject.service;

import org.example.springbootproject.pojo.DocumentParseResult;

/**
 * 文档解析服务接口
 */
public interface DocumentParseService {
    /**
     * 解析文档
     * @param filePath 文件路径
     * @return 解析结果
     */
    DocumentParseResult parseDocument(String filePath);
}
