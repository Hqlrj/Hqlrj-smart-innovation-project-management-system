package org.example.springbootproject.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.example.springbootproject.mapper.RoleMapper;
import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Role;
import org.example.springbootproject.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 * 实现角色相关的业务逻辑处理
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 分页查询角色列表
     * 使用PageHelper插件实现分页功能
     */
    @Override
    public PageBean<Role> list(Integer page, Integer pageSize, String searchText) {
        // 设置分页参数：PageHelper会自动拦截接下来的第一个查询SQL并添加LIMIT子句
        PageHelper.startPage(page, pageSize);
        
        // 执行查询
        List<Role> roleList = roleMapper.list(searchText);
        
        // 获取分页信息
        Page<Role> p = (Page<Role>) roleList;
        
        // 封装分页结果
        return PageBean.of(p.getTotal(), p.getResult());
    }

    /**
     * 根据ID查询角色
     */
    @Override
    public Role getById(Integer id) {
        return roleMapper.getById(id);
    }

    /**
     * 根据角色编号查询角色
     */
    @Override
    public Role getByRoleId(String roleId) {
        return roleMapper.getByRoleId(roleId);
    }

    /**
     * 根据角色名称查询角色
     */
    @Override
    public Role getByRoleName(String roleName) {
        return roleMapper.getByRoleName(roleName);
    }

    /**
     * 新增角色
     * 自动设置创建时间和更新时间
     */
    @Override
    public void add(Role role) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 设置创建时间
        role.setCreateTime(now);
        
        // 设置更新时间
        role.setUpdateTime(now);
        
        // 调用Mapper插入数据
        roleMapper.insert(role);
    }

    /**
     * 更新角色信息
     * 自动更新修改时间
     */
    @Override
    public void update(Role role) {
        // 更新修改时间为当前时间
        role.setUpdateTime(LocalDateTime.now());
        
        // 调用Mapper更新数据
        roleMapper.update(role);
    }

    /**
     * 删除角色
     *
     */
    @Override
    public void delete(Integer id) {
        roleMapper.delete(id);
    }
}

