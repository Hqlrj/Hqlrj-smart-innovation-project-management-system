package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.Project;

import java.util.List;

/**
 * 项目数据访问层
 */
@Mapper
public interface ProjectMapper {
    /**
     * 查询项目列表（支持条件查询和分页）
     */
    List<Project> list(@Param("projectName") String projectName,
                      @Param("projectType") String projectType,
                      @Param("status") String status,
                      @Param("projectSpace") String projectSpace,
                      @Param("applicantId") Integer applicantId);

    /**
     * 根据ID查询项目
     */
    Project getById(Integer id);

    /**
     * 新增项目
     */
    void insert(Project project);

    /**
     * 更新项目
     */
    void update(Project project);

    /**
     * 删除项目
     */
    void delete(Integer id);
}
