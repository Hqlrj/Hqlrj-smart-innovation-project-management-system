package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.UserMapper;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.User;
import org.example.springbootproject.service.UserService;
import org.example.springbootproject.util.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑处理
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 分页查询用户列表
     * 使用PageHelper插件实现分页功能，支持多条件筛选查询
     *
     */
    @Override
    public PageBean<User> list(Integer page, Integer pageSize, String name, String phone, String role, String college, String status) {
        // 设置分页参数：PageHelper会自动拦截接下来的第一个查询SQL并添加LIMIT子句
        PageHelper.startPage(page, pageSize);

        // 执行查询（根据传入的条件进行筛选）
        List<User> userList = userMapper.list(name, phone, role, college, status);

        // 获取分页信息
        Page<User> p = (Page<User>) userList;

        // 封装分页结果
        return PageBean.of(p.getTotal(), p.getResult());
    }

    /**
     * 根据ID查询用户
     *
     */
    @Override
    public User getById(Integer id) {
        return userMapper.getById(id);
    }

    /**
     * 根据学号/职工号查询用户
     * 用于登录验证和账号唯一性校验
     */
    @Override
    public User getByPhone(String phone) {
        return userMapper.getByPhone(phone);
    }

    @Override
    public User getByStudentId(String studentId) {
        return userMapper.getByStudentId(studentId);
    }

    /**
     * 新增用户
     * 自动处理密码加密、设置创建时间和默认状态
     *
     */
    @Override
    public void add(User user) {
        // 设置创建时间为当前时间
        user.setCreateTime(LocalDateTime.now());
        
        // 处理密码：如果为空则使用默认密码，否则进行MD5加密
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // 使用默认密码（MD5加密后的'123456'）
            user.setPassword("e10adc3949ba59abbe56e057f20f883e");
        } else {
            // 对明文密码进行MD5加密
            user.setPassword(Md5Util.md5(user.getPassword()));
        }
        
        // 如果状态为空，设置默认状态为"待审批"
        if (user.getStatus() == null || user.getStatus().isEmpty()) {
            user.setStatus("待审批");
        }
        
        // 调用Mapper插入数据到数据库
        userMapper.insert(user);
    }

    /**
     * 更新用户信息
     */
    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    /**
     * 删除用户
     */
    @Override
    public void delete(Integer id) {
        userMapper.delete(id);
    }

    /**
     * 更新用户最后登录时间
     * 用户登录成功后调用此方法记录登录时间
     */
    @Override
    public void updateLoginTime(Integer id) {
        userMapper.updateLoginTime(id);
    }

    /**
     * 修改用户密码
     * 验证旧密码是否正确，然后更新为新密码（MD5加密后存储）
     *
     */
    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        // 查询用户信息
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 将旧密码进行MD5加密后与数据库中的密码对比
        String oldMd5 = Md5Util.md5(oldPassword);
        if (!oldMd5.equalsIgnoreCase(user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }
        
        // 将新密码进行MD5加密后更新到数据库
        String newMd5 = Md5Util.md5(newPassword);
        userMapper.updatePassword(userId, newMd5);
    }

    @Override
    public List<User> listAll() {
        return userMapper.listAllApproved();
    }
}

