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
     * 公告列表（按置顶+时间倒序，可按标题、已读状态筛选）
     */
    List<Notice> list(@Param("title") String title,
                      @Param("viewerCollege") String viewerCollege,
                      @Param("userId") Integer userId,
                      @Param("readStatus") String readStatus);

    /**
     * 根据ID查询公告
     */
    Notice getById(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 新增公告
     */
    void insert(Notice notice);

    /**
     * 标记当前用户已读（重复阅读忽略）
     */
    void markRead(@Param("noticeId") Integer noticeId, @Param("userId") Integer userId);

    /**
     * 删除公告
     */
    void delete(Integer id);

    /**
     * 删除某公告的全部已读记录
     */
    void deleteReadsByNoticeId(Integer noticeId);
}
