package org.example.springbootproject.service;

import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Project;

/**
 * 项目服务接口
 * 定义项目相关的业务逻辑方法
 */
public interface ProjectService {
    /**
     * 分页查询项目列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param projectName 项目名称（可选）
     * @param projectType 项目类型（可选）
     * @param status 状态（可选）
     * @param projectSpace 项目空间（可选）
     * @param applicantId 申请人ID（可选）
     * @return 分页结果
     */
    PageBean<Project> list(Integer page, Integer pageSize, String projectName, String projectType, String status, String projectSpace, Integer applicantId);

    /**
     * 根据ID查询项目（包含成员列表）
     * @param id 项目ID
     * @return 项目信息
     */
    Project getById(Integer id);

    /**
     * 新增项目
     * @param project 项目信息（包含成员列表）
     */
    void add(Project project);

    /**
     * 更新项目
     * @param project 项目信息（包含成员列表）
     */
    void update(Project project);

    /**
     * 删除项目
     * @param id 项目ID
     */
    void delete(Integer id);

    /**
     * 提交项目申报
     * @param id 项目ID
     */
    void submit(Integer id);

    /**
     * 审批项目（通过）
     * @param id 项目ID
     * @param auditorName 审批人姓名
     */
    void approve(Integer id, String auditorName);

    /**
     * 驳回项目
     * @param id 项目ID
     * @param auditorName 审批人姓名
     */
    void reject(Integer id, String auditorName, String rejectReason);
}
