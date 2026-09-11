package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.Notice;

import java.util.List;

/**
 * 公告数据访问层
 */
@Mapper
public interface NoticeMapper {

    /**
     * 公告列表（按时间倒序，可按标题模糊查询）
     */
    List<Notice> list(@Param("title") String title);

    /**
     * 根据ID查询公告
     */
    Notice getById(Integer id);

    /**
     * 新增公告
     */
    void insert(Notice notice);

    /**
     * 删除公告（目前保留接口，未在前端使用）
     */
    void delete(Integer id);
}
