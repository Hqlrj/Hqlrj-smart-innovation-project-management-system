package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.ProjectPlan;

import java.util.List;

/**
 * 计划书数据访问层
 */
@Mapper
public interface ProjectPlanMapper {
    /**
     * 根据项目ID查询计划书列表
     */
    List<ProjectPlan> listByProjectId(Integer projectId);

    /**
     * 根据ID查询计划书
     */
    ProjectPlan getById(Integer id);

    /**
     * 新增计划书
     */
    void insert(ProjectPlan plan);

    /**
     * 批量新增计划书
     */
    void batchInsert(@Param("plans") List<ProjectPlan> plans);

    /**
     * 删除计划书
     */
    void delete(Integer id);

    /**
     * 根据项目ID删除所有计划书
     */
    void deleteByProjectId(Integer projectId);
}

