package org.example.springbootproject.controller;

import org.example.springbootproject.pojo.PageBean;
import org.example.springbootproject.pojo.Result;
import org.example.springbootproject.pojo.Role;
import org.example.springbootproject.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public Result<PageBean<Role>> list(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String searchText) {
        PageBean<Role> pageBean = roleService.list(page, pageSize, searchText);
        return Result.success(pageBean);
    }

    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public Result<Void> add(@RequestBody Role role) {
        roleService.add(role);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Role role) {
        role.setId(id);
        roleService.update(role);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return Result.success();
    }
}

