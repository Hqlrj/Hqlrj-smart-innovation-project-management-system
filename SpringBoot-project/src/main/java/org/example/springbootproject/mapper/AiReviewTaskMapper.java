package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.AiReviewTask;

import java.util.List;

/**
 * AI 评审任务数据访问层
 */
@Mapper
public interface AiReviewTaskMapper {
    /**
     * 根据ID查询任务
     */
    AiReviewTask getById(Integer id);

    /**
     * 根据项目ID查询任务列表
     */
    List<AiReviewTask> listByProjectId(Integer projectId);

    /**
     * 根据项目ID查询最新的进行中任务
     */
    AiReviewTask getLatestPendingTask(Integer projectId);

    /**
     * 新增任务
     */
    void insert(AiReviewTask task);

    /**
     * 更新任务状态
     */
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 更新任务
     */
    void update(AiReviewTask task);

    /**
     * 更新任务进度
     */
    void updateProgress(@Param("id") Integer id, @Param("sectionDone") Integer sectionDone);

    /**
     * 更新任务结果
     */
    void updateResult(AiReviewTask task);

    /**
     * 记录任务失败
     */
    void recordFailure(@Param("id") Integer id, @Param("errorMsg") String errorMsg);

    /**
     * 删除任务
     */
    void delete(Integer id);
}
