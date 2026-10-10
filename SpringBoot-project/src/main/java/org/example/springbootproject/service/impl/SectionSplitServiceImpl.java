package org.example.springbootproject.service.impl;

import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentSection;
import org.example.springbootproject.service.SectionSplitService;
import org.example.springbootproject.util.SectionSplitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 章节切分服务实现类
 */
@Service
public class SectionSplitServiceImpl implements SectionSplitService {

    @Autowired
    private SectionSplitUtil sectionSplitUtil;

    @Override
    public List<DocumentSection> splitSectionsForTask(Integer taskId, DocumentParseResult parseResult) {
        try {
            return sectionSplitUtil.splitSections(parseResult);
        } catch (Exception e) {
            throw new RuntimeException("章节切分失败: " + e.getMessage(), e);
        }
    }
}
