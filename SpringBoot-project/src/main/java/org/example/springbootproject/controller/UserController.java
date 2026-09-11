package org.example.springbootproject.controller;

import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.service.RoleService;
import org.example.springbootproject.pojo.Role;
import org.example.springbootproject.pojo.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

/**
 * 用户管理控制器
 * 提供用户管理的RESTful API接口
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<PageBean<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name, String phone, String role, String college, String status) {
        PageBean<User> pageBean = userService.list(page, pageSize, name, phone, role, college, status);
        return Result.success(pageBean);
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> add(@RequestBody User user) {
        // 校验学号是否已存在
        User exists = userService.getByStudentId(user.getStudentId());
        if (exists != null) {
            return Result.error("学号/职工号已存在");
        }
        
        // 根据角色名称查找角色编号
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            Role role = roleService.getByRoleName(user.getRole());
            if (role == null) {
                return Result.error("角色不存在");
            }
            // 设置角色编号
            user.setRoleId(role.getRoleId());
        } else {
            return Result.error("角色不能为空");
        }
        
        userService.add(user);
        return Result.success();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        // 校验角色
        Role role = roleService.getByRoleId(user.getRoleId());
        if (role == null) {
            return Result.error("角色不存在");
        }
        // 校验账号是否存在
        User exists = userService.getByStudentId(user.getStudentId());
        if (exists != null) {
            return Result.error("账号已存在");
        }
        // 同步角色名称冗余字段
        user.setRole(role.getRoleName());
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest req) {
        if (req.getUserId() == null || req.getOldPassword() == null || req.getNewPassword() == null) {
            return Result.error("参数不完整");
        }
        try {
            userService.changePassword(req.getUserId(), req.getOldPassword(), req.getNewPassword());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id); // 确保ID一致
        userService.update(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        userService.delete(id);
        return Result.success();
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @RequestParam("userId") Integer userId) {
        if (file == null || file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型（只允许图片）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return Result.error("文件名不能为空");
        }
        String lowerFilename = originalFilename.toLowerCase();
        if (!lowerFilename.endsWith(".jpg") && !lowerFilename.endsWith(".jpeg") 
            && !lowerFilename.endsWith(".png") && !lowerFilename.endsWith(".gif")) {
            return Result.error("只支持上传图片文件（.jpg、.jpeg、.png、.gif格式）");
        }

        try {
            // 创建上传目录
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = "uploads/avatars";
            File avatarDir = new File(projectRoot, uploadDir);
            if (!avatarDir.exists()) {
                if (!avatarDir.mkdirs()) {
                    return Result.error("无法创建上传目录: " + avatarDir.getAbsolutePath());
                }
            }

            // 生成唯一文件名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            File targetFile = new File(avatarDir, uniqueFilename);

            // 保存文件
            file.transferTo(targetFile);

            // 构建文件路径（相对路径，用于前端访问）
            String filePath = "/uploads/avatars/" + uniqueFilename;

            // 更新用户头像路径
            User user = userService.getById(userId);
            if (user == null) {
                // 如果文件已保存但用户不存在，删除文件
                targetFile.delete();
                return Result.error("用户不存在");
            }

            // 如果用户已有头像，删除旧头像文件
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                String oldAvatarPath = user.getAvatar();
                if (oldAvatarPath.startsWith("/uploads/avatars/")) {
                    File oldFile = new File(projectRoot, oldAvatarPath.substring(1));
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }
            }

            // 更新数据库
            user.setAvatar(filePath);
            userService.update(user);

            return Result.success(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("头像上传失败：" + e.getMessage());
        }
    }
}

