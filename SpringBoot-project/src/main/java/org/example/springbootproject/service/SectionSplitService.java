package org.example.springbootproject.service;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentSection;

import java.util.List;

/**
 * 章节切分服务接口
 */
public interface SectionSplitService {
    /**
     * 为任务切分章节
     * @param taskId 任务ID
     * @param parseResult 文档解析结果
     * @return 章节列表
     */
    List<DocumentSection> splitSectionsForTask(Integer taskId, DocumentParseResult parseResult);
}
