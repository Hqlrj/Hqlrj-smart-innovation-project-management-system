/**
 * 角色管理相关 API 接口
 */
import request from '@/utils/request'

// 角色数据类型
export interface Role {
  id: string
  roleId: string
  roleName: string
  description: string
  createTime: string
  updateTime?: string
}

// 角色查询参数a
export interface RoleQueryParams {
  searchText?: string
  page?: number
  pageSize?: number
}

// 分页结果类型
export interface PageResult<T> {
  total: number
  list: T[]
}

/**
 * 查询角色列表
 */
export const getRoleList = (params?: RoleQueryParams) => {
  return request.get<PageResult<Role>>('/roles', { params })
}

/**
 * 根据 ID 查询角色详情
 */
export const getRoleById = (id: string) => {
  return request.get<Role>(`/roles/${id}`)
}

/**
 * 新增角色
 */
export const addRole = (data: Partial<Role>) => {
  return request.post('/roles', data)
}

/**
 * 更新角色
 */
export const updateRole = (id: string, data: Partial<Role>) => {
  return request.put(`/roles/${id}`, data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: string) => {
  return request.delete(`/roles/${id}`)
}

