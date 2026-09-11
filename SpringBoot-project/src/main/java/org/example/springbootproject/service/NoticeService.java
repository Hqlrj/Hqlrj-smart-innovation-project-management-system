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
    PageBean<Notice> list(Integer page, Integer pageSize, String title);

    /**
     * 根据ID查询公告
     */
    Notice getById(Integer id);

    /**
     * 发布公告
     */
    void add(Notice notice);

    /**
     * 删除公告
     */
    void delete(Integer id);
}
