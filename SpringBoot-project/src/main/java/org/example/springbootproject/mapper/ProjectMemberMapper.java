package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.ProjectMember;

import java.util.List;

/**
 * 项目成员数据访问层
 */
@Mapper
public interface ProjectMemberMapper {
    /**
     * 根据项目ID查询成员列表
     */
    List<ProjectMember> listByProjectId(Integer projectId);

    /**
     * 根据ID查询成员
     */
    ProjectMember getById(Integer id);

    /**
     * 新增成员
     */
    void insert(ProjectMember member);

    /**
     * 批量新增成员
     */
    void batchInsert(@Param("members") List<ProjectMember> members);

    /**
     * 更新成员
     */
    void update(ProjectMember member);

    /**
     * 删除成员
     */
    void delete(Integer id);

    /**
     * 根据项目ID删除所有成员
     */
    void deleteByProjectId(Integer projectId);
}
