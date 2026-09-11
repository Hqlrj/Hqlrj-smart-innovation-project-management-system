package org.example.springbootproject.service;

import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.User;

import java.util.List;

/**
 * 用户服务接口
 * 定义用户相关的业务逻辑方法
 */
public interface UserService {
    /**
     * 分页查询用户列表
     * @param page 页码
     * @param pageSize 每页记录数
     * @param name 姓名（可选）
     * @param phone 手机号（可选）
     * @param role 角色（可选）
     * @param college 学院（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    PageBean<User> list(Integer page, Integer pageSize, String name, String phone, String role, String college, String status);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Integer id);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    User getByPhone(String phone);

    /**
     * 根据学号查询用户
     * @param studentId 学号/职工号
     * @return 用户信息
     */
    User getByStudentId(String studentId);

    /**
     * 新增用户
     * @param user 用户信息
     */
    void add(User user);

    /**
     * 更新用户
     * @param user 用户信息
     */
    void update(User user);

    /**
     * 删除用户
     * @param id 用户ID
     */
    void delete(Integer id);

    /**
     * 更新登录时间
     * @param id 用户ID
     */
    void updateLoginTime(Integer id);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 查询所有已审批通过的用户
     * 用于公告发布时批量通知
     */
    List<User> listAll();
}

