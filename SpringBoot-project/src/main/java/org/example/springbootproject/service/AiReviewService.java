package org.example.springbootproject.service;

import org.example.springbootproject.pojo.AiReviewTask;
import org.example.springbootproject.pojo.DocumentParseResult;
import org.example.springbootproject.pojo.DocumentSection;
import org.example.springbootproject.pojo.StartReviewRequest;
import org.example.springbootproject.pojo.StartReviewResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI 评审服务接口
 * 定义AI评审相关的业务逻辑方法
 */
public interface AiReviewService {
    /**
     * 发起AI评审
     * @param projectId 项目ID
     * @param request 评审请求
     * @param userId 用户ID
     * @return 评审响应
     */
    StartReviewResponse startReview(Integer projectId, StartReviewRequest request, Integer userId);
    
    /**
     * 执行完整的AI评审流程
     * @param taskId 任务ID
     * @param parseResult 文档解析结果
     * @return 是否成功
     */
    CompletableFuture<Boolean> executeAiReview(Integer taskId, DocumentParseResult parseResult);
    
    /**
     * 执行章节评审
     * @param taskId 任务ID
     * @param sections 章节列表
     * @return 是否成功
     */
    boolean executeSectionReview(Integer taskId, List<DocumentSection> sections);
    
    /**
     * 执行合并总结
     * @param taskId 任务ID
     * @param sections 章节列表
     * @return 是否成功
     */
    boolean executeSummary(Integer taskId, List<DocumentSection> sections);
    
    /**
     * 获取AI评审任务详情
     * @param taskId 任务ID
     * @return 任务详情
     */
    AiReviewTask getAiReviewTask(Integer taskId);
    
    /**
     * 获取AI评审章节详情
     * @param taskId 任务ID
     * @return 章节列表
     */
    List<org.example.springbootproject.pojo.AiReviewSection> getAiReviewSections(Integer taskId);
}
