package org.example.springbootproject.service;

import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Role;

public interface RoleService {
    PageBean<Role> list(Integer page, Integer pageSize, String searchText);

    Role getById(Integer id);

    Role getByRoleId(String roleId);

    Role getByRoleName(String roleName);

    void add(Role role);

    void update(Role role);

    void delete(Integer id);
}

