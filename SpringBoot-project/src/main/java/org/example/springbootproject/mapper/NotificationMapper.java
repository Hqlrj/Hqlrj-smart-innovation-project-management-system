package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.Notification;

import java.util.List;

/**
 * 站内通知数据访问层
 */
@Mapper
public interface NotificationMapper {

    /**
     * 根据用户ID查询通知列表（按已读状态和时间倒序）
     */
    List<Notification> listByUserId(@Param("userId") Integer userId);

    /**
     * 统计用户未读通知数量
     */
    int countUnread(@Param("userId") Integer userId);

    /**
     * 新增通知
     */
    void insert(Notification notification);

    /**
     * 标记单条通知为已读
     */
    void markAsRead(@Param("id") Integer id);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(@Param("userId") Integer userId);
}