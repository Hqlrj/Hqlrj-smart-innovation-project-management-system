package org.example.springbootproject.service;

import org.example.springbootproject.pojo.Award;
import org.example.springbootproject.pojo.PageBean;

import java.util.List;

/**
 * 获奖记录服务接口
 */
public interface AwardService {
    /**
     * 分页查询获奖记录列表（支持条件查询）
     */
    PageBean<Award> list(Integer page, Integer pageSize, String projectName, 
                         String competitionName, String competitionLevel, 
                         String awardLevel, Integer applicantId);

    /**
     * 根据项目ID查询获奖记录列表
     */
    List<Award> listByProjectId(Integer projectId);

    /**
     * 根据ID查询获奖记录
     */
    Award getById(Integer id);

    /**
     * 新增获奖记录
     */
    void add(Award award);

    /**
     * 更新获奖记录
     */
    void update(Award award);

    /**
     * 删除获奖记录
     */
    void delete(Integer id);

    /**
     * 审批获奖记录（通过）
     */
    void approve(Integer id);

    /**
     * 审批获奖记录（未通过）
     */
    void reject(Integer id, String reason);
}
