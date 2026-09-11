package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.Award;

import java.util.List;

/**
 * 获奖记录数据访问层
 */
@Mapper
public interface AwardMapper {
    /**
     * 根据项目ID查询获奖记录列表
     */
    List<Award> listByProjectId(@Param("projectId") Integer projectId);

    /**
     * 根据ID查询获奖记录
     */
    Award getById(Integer id);

    /**
     * 新增获奖记录
     */
    void insert(Award award);

    /**
     * 更新获奖记录
     */
    void update(Award award);

    /**
     * 删除获奖记录
     */
    void delete(Integer id);

    /**
     * 根据项目ID删除获奖记录
     */
    void deleteByProjectId(@Param("projectId") Integer projectId);

    /**
     * 分页查询获奖记录列表（支持条件查询）
     */
    List<Award> list(@Param("projectName") String projectName,
                     @Param("competitionName") String competitionName,
                     @Param("competitionLevel") String competitionLevel,
                     @Param("awardLevel") String awardLevel,
                     @Param("applicantId") Integer applicantId);
}
