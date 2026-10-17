package org.example.springbootproject.service;

import org.example.springbootproject.pojo.Notice;

public interface DocumentIndexService {

    void indexNotice(Notice notice);

    void indexPlan(Integer projectId, Integer planId, String filePath, String fileName);

    void reindexExistingPlans();

    void deleteNotice(Integer noticeId);

    void deletePlan(Integer planId);
}
