package org.example.springbootproject.controller;

import org.example.springbootproject.mapper.UserMapper;
import org.example.springbootproject.pojo.LoginRequest;
import org.example.springbootproject.pojo.LoginResponse;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.util.JwtUtil;
import org.example.springbootproject.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Result.error("账号或密码不能为空");
        }
        User user = userService.getByStudentId(request.getUsername());
        if (user == null) {
            return Result.error("账号不存在");
        }
        String md5Pwd = Md5Util.md5(request.getPassword());
        if (!md5Pwd.equalsIgnoreCase(user.getPassword())) {
            return Result.error("账号或密码错误");
        }
        // 仅允许已审批用户登录
        if (!"已审批".equals(user.getStatus())) {
            return Result.error("当前状态未审批，无法登录");
        }
        userService.updateLoginTime(user.getId());
        
        // 使用JWT生成Token（包含用户ID、用户名、角色）
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getStudentId(),
                user.getRole()
        );
        
        LoginResponse resp = new LoginResponse(
                token,  // JWT Token
                user.getId(),
                user.getStudentId(),
                user.getName(),
                user.getRole(),
                user.getStatus(),
                user.getAvatar()  // 头像路径
        );
        return Result.success(resp);
    }

    // ==================== 忘记密码 ====================

    // 验证码缓存: phone -> { code, expireTime, sendTime }
    private final ConcurrentHashMap<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    private static class CodeEntry {
        String code;
        long expireTime;  // 验证码过期时间
        long sendTime;    // 上次发送时间(用于60秒限制)

        CodeEntry(String code, long expireTime, long sendTime) {
            this.code = code;
            this.expireTime = expireTime;
            this.sendTime = sendTime;
        }
    }

    @PostMapping("/auth/send-code")
    public Result<Void> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        // 查找用户
        User user = userService.getByPhone(phone);
        if (user == null) {
            return Result.error("该手机号未注册");
        }
        // 60秒内不可重复发送
        CodeEntry existing = codeCache.get(phone);
        if (existing != null && System.currentTimeMillis() - existing.sendTime < 60000) {
            long waitSec = (60000 - (System.currentTimeMillis() - existing.sendTime)) / 1000 + 1;
            return Result.error("请等待" + waitSec + "秒后重试");
        }
        // 生成6位验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        long expireTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟有效
        codeCache.put(phone, new CodeEntry(code, expireTime, System.currentTimeMillis()));
        // 开发阶段: 控制台打印验证码
        System.out.println("========== 验证码 ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: 5分钟");
        System.out.println("============================");
        return Result.success();
    }

    @PostMapping("/auth/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        String newPassword = body.get("newPassword");
        if (phone == null || code == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        // 校验验证码
        CodeEntry entry = codeCache.get(phone);
        if (entry == null) {
            return Result.error("请先获取验证码");
        }
        if (System.currentTimeMillis() > entry.expireTime) {
            codeCache.remove(phone);
            return Result.error("验证码已过期，请重新获取");
        }
        if (!entry.code.equals(code)) {
            return Result.error("验证码错误");
        }
        // 查找用户并重置密码
        User user = userService.getByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String md5Pwd = Md5Util.md5(newPassword);
        userMapper.updatePassword(user.getId(), md5Pwd);
        // 删除验证码
        codeCache.remove(phone);
        System.out.println("密码重置成功: " + phone + " (" + user.getStudentId() + ")");
        return Result.success();
    }
}

