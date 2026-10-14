package org.example.springbootproject.service;

import org.example.springbootproject.pojo.AiReviewTask;
import org.example.springbootproject.pojo.StartReviewRequest;
import org.example.springbootproject.pojo.StartReviewResponse;

import java.util.List;

/**
 * AI 评审服务
 */
public interface AiReviewService {

    StartReviewResponse startReview(Integer projectId, StartReviewRequest request, Integer userId);

    AiReviewTask getAiReviewTask(Integer taskId);

    List<org.example.springbootproject.pojo.AiReviewSection> getAiReviewSections(Integer taskId);
}
