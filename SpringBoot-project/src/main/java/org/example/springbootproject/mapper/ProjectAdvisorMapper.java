package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.ProjectAdvisor;

import java.util.List;

/**
 * 项目指导老师Mapper接口
 */
@Mapper
public interface ProjectAdvisorMapper {
    /**
     * 根据项目ID查询指导老师列表
     */
    List<ProjectAdvisor> listByProjectId(@Param("projectId") Integer projectId);

    /**
     * 新增指导老师
     */
    void insert(ProjectAdvisor advisor);

    /**
     * 更新指导老师
     */
    void update(ProjectAdvisor advisor);

    /**
     * 根据ID删除指导老师
     */
    void deleteById(@Param("id") Integer id);

    /**
     * 根据项目ID删除所有指导老师
     */
    void deleteByProjectId(@Param("projectId") Integer projectId);

    /**
     * 根据ID查询指导老师
     */
    ProjectAdvisor getById(@Param("id") Integer id);
}

