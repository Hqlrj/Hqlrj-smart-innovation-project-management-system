package org.example.springbootproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.NoticeService;
import org.example.springbootproject.service.NotificationService;
import org.example.springbootproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 公告列表（所有角色可查看）
     */
    @GetMapping
    public Result<PageBean<Notice>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String title) {
        PageBean<Notice> pageBean = noticeService.list(page, pageSize, title);
        return Result.success(pageBean);
    }

    /**
     * 公告详情（所有角色可查看）
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Integer id) {
        Notice notice = noticeService.getById(id);
        return Result.success(notice);
    }

    /**
     * 发布公告
     * 限制：项目负责人不能发布
     */
    @PostMapping
    public Result<Notice> add(@RequestBody Notice notice, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if ("项目负责人".equals(role)) {
            return Result.error("项目负责人不能发布公告");
        }

        User user = userService.getById(userId);

        notice.setPublisherId(userId);
        notice.setPublisherRole(role);
        if (user != null) {
            notice.setPublisherName(user.getName());
        }

        noticeService.add(notice);
        // 给除发布者外的所有用户发送通知
        List<User> allUsers = userService.listAll();
        for (User targetUser : allUsers) {
            if (targetUser.getId() != null && !targetUser.getId().equals(userId)) {
                notificationService.createNotification(
                    targetUser.getId(),
                    "新公告发布",
                    "「" + notice.getTitle() + "」已发布",
                    "notice_publish", notice.getId());
            }
        }
        return Result.success(notice);
    }

    /**
     * 删除公告
     * 权限：
     * - 系统管理员/项目管理员：可删除所有公告
     * - 其他角色：只能删除自己发布的公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        boolean isAdmin = "系统管理员".equals(role) || "项目管理员".equals(role);

        Notice notice = noticeService.getById(id);
        if (notice == null) {
            return Result.error("公告不存在");
        }

        if (!isAdmin) {
            if (notice.getPublisherId() == null || !notice.getPublisherId().equals(userId)) {
                return Result.error("无权限删除该公告（只能删除自己发布的公告）");
            }
        }

        noticeService.delete(id);
        return Result.success();
    }
}
