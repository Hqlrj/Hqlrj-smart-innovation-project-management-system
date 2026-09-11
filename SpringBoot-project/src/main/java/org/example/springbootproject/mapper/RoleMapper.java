package org.example.springbootproject.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.springbootproject.pojo.Role;

import java.util.List;

@Mapper
public interface RoleMapper {
    /**
     * 分页查询角色列表
     * @param searchText 模糊查询角色编号或名称
     * @return 角色集合
     */
    List<Role> list(@Param("searchText") String searchText);

    /**
     * 根据ID查询
     */
    Role getById(Integer id);

    /**
     * 根据角色编号查询
     */
    Role getByRoleId(String roleId);

    /**
     * 根据角色名称查询
     */
    Role getByRoleName(String roleName);

    /**
     * 新增
     */
    void insert(Role role);

    /**
     * 更新
     */
    void update(Role role);

    /**
     * 删除
     */
    void delete(Integer id);
}

