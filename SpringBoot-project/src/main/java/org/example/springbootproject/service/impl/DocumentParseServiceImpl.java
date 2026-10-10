package org.example.springbootproject.service.impl;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.service.DocumentParseService;
import org.example.springbootproject.util.DocumentParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文档解析服务实现类
 */
@Service
public class DocumentParseServiceImpl implements DocumentParseService {

    @Autowired
    private DocumentParserUtil documentParserUtil;

    @Override
    public DocumentParseResult parseDocument(String filePath) {
        try {
            return documentParserUtil.parseDocument(filePath);
        } catch (Exception e) {
            DocumentParseResult result = new DocumentParseResult();
            result.setErrorMsg("文档解析失败: " + e.getMessage());
            return result;
        }
    }
}
