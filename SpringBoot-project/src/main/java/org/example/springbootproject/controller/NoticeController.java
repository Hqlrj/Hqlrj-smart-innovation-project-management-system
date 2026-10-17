package org.example.springbootproject.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.springbootproject.pojo.Notice;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.util.CollegeScope;
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
            String title,
            @RequestParam(defaultValue = "unread") String readStatus,
            HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String viewerCollege = currentCollegeFilter(request);
        PageBean<Notice> pageBean = noticeService.list(page, pageSize, title, viewerCollege, userId, readStatus);
        return Result.success(pageBean);
    }

    /**
     * 公告详情：打开即记为已读，之后不再出现在待查看/置顶列表
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Integer id, HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Notice notice = noticeService.getById(id, userId);
        if (notice == null) {
            return Result.error("公告不存在");
        }
        if (!canViewNotice(request, notice)) {
            return Result.error("只能查看本院公告或系统公告");
        }
        noticeService.markRead(id, userId);
        notice.setIsRead(1);
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
        boolean globalNotice = CollegeScope.isGlobalViewer(role);
        String publisherCollege = user == null ? null : user.getCollege();
        List<User> allUsers = userService.listAll();
        for (User targetUser : allUsers) {
            if (targetUser.getId() == null || targetUser.getId().equals(userId)) {
                continue;
            }
            if (globalNotice || CollegeScope.canViewCollege(role, publisherCollege, targetUser.getCollege())) {
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

        Notice notice = noticeService.getById(id, userId);
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

    private String currentCollegeFilter(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        User user = userId == null ? null : userService.getById(userId);
        return CollegeScope.collegeFilter(user, role);
    }

    private boolean canViewNotice(HttpServletRequest request, Notice notice) {
        Integer userId = (Integer) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        User user = userId == null ? null : userService.getById(userId);
        return CollegeScope.canViewNotice(
                role,
                CollegeScope.collegeFilter(user, role),
                notice.getPublisherRole(),
                notice.getPublisherCollege());
    }
}
