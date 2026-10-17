package org.example.springbootproject.service;

import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.PageBean;

/**
 * 公告服务接口
 */
public interface NoticeService {

    /**
     * 分页查询公告列表
     */
    PageBean<Notice> list(Integer page, Integer pageSize, String title, String viewerCollege,
                          Integer userId, String readStatus);

    /**
     * 根据ID查询公告
     */
    Notice getById(Integer id, Integer userId);

    /**
     * 标记当前用户已读
     */
    void markRead(Integer noticeId, Integer userId);

    /**
     * 发布公告
     */
    void add(Notice notice);

    /**
     * 删除公告
     */
    void delete(Integer id);
}
