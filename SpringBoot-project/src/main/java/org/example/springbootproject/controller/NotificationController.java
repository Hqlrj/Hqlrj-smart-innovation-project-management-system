package org.example.springbootproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springbootproject.pojo.Notification;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 站内通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取当前用户通知列表（分页）
     */
    @GetMapping
    public Result<PageBean<Notification>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(notificationService.listByUserId(userId, page, pageSize));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    public Result<Integer> unreadCount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return Result.success(notificationService.countUnread(userId));
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Integer id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 标记当前用户所有通知为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}