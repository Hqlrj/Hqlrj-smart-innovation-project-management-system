package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.springbootproject.pojo.AiReviewReport;

@Mapper
public interface AiReviewReportMapper {

    void insert(AiReviewReport report);

    void deleteByTaskId(Integer taskId);

    AiReviewReport getByTaskId(Integer taskId);
}
