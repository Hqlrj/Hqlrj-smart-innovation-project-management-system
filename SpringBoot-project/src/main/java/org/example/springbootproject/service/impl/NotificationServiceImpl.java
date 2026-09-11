package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.NotificationMapper;
import org.example.springbootproject.pojo.Notification;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public PageBean<Notification> listByUserId(Integer userId, Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Notification> list = notificationMapper.listByUserId(userId);
        Page<Notification> p = (Page<Notification>) list;
        return PageBean.of(p.getTotal(), p.getResult());
    }

    @Override
    public int countUnread(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void createNotification(Integer userId, String title, String content, String type, Integer relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    @Override
    public void markAsRead(Integer id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        notificationMapper.markAllAsRead(userId);
    }
}