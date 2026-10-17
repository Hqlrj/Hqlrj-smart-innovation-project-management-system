package org.example.springbootproject.config;

import org.example.springbootproject.service.DocumentIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时把已上传但未入向量库的计划书补进去，方便立刻用项目名提问。
 */
@Component
public class VectorIndexBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VectorIndexBackfill.class);

    private final DocumentIndexService documentIndexService;

    public VectorIndexBackfill(DocumentIndexService documentIndexService) {
        this.documentIndexService = documentIndexService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            documentIndexService.reindexExistingPlans();
        } catch (Exception e) {
            log.warn("启动回填向量索引失败: {}", e.getMessage());
        }
    }
}
