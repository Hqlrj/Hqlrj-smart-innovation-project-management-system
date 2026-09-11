package org.example.springbootproject.service;

import org.example.springbootproject.pojo.Notification;
import org.example.springbootproject.pojo.PageBean;

/**
 * 站内通知服务接口
 */
public interface NotificationService {

    /**
     * 分页查询用户通知列表
     */
    PageBean<Notification> listByUserId(Integer userId, Integer page, Integer pageSize);

    /**
     * 统计用户未读通知数量
     */
    int countUnread(Integer userId);

    /**
     * 创建通知
     */
    void createNotification(Integer userId, String title, String content, String type, Integer relatedId);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(Integer id);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Integer userId);
}