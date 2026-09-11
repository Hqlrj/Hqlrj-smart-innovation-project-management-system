package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.User;

import java.util.List;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {
    /**
     * 查询用户列表（支持条件查询和分页）
     */
    List<User> list(@Param("name") String name,
                    @Param("phone") String phone,
                    @Param("role") String role,
                    @Param("college") String college,
                    @Param("status") String status);

    /**
     * 根据ID查询用户
     */
    User getById(Integer id);

    /**
     * 根据手机号查询用户
     */
    User getByPhone(String phone);

    /**
     * 根据学号查询用户
     */
    User getByStudentId(String studentId);

    /**
     * 新增用户
     */
    void insert(User user);

    /**
     * 更新用户
     */
    void update(User user);

    /**
     * 删除用户
     */
    void delete(Integer id);

    /**
     * 更新登录时间
     */
    void updateLoginTime(Integer id);

    /**
     * 更新密码
     */
    void updatePassword(@Param("id") Integer id, @Param("password") String password);

    /**
     * 查询所有状态为已审批的用户
     */
    List<User> listAllApproved();
}

