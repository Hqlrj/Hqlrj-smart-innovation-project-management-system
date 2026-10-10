package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.springbootproject.pojo.AiReviewSection;

import java.util.List;

/**
 * AI 评审章节数据访问层
 */
@Mapper
public interface AiReviewSectionMapper {
    /**
     * 根据任务ID查询章节列表
     */
    List<AiReviewSection> listByTaskId(Integer taskId);

    /**
     * 新增章节记录
     */
    void insert(AiReviewSection section);

    /**
     * 根据ID查询章节
     */
    AiReviewSection getById(Integer id);

    /**
     * 删除章节
     */
    void delete(Integer id);
}
