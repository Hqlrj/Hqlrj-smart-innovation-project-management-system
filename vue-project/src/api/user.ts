/**
 * 用户管理相关 API 接口
 */
import request from '@/utils/request'

// 用户数据类型
export interface User {
  id: string
  studentId: string
  name: string
  phone: string
  roleId?: string
  role: string
  college: string
  status: string
  createTime: string
  loginTime: string
  password?: string
  email?: string
  class?: string
  department?: string
  avatar?: string
}

export interface ChangePasswordRequest {
  userId: number
  oldPassword: string
  newPassword: string
}

// 用户查询参数
export interface UserQueryParams {
  name?: string
  phone?: string
  role?: string
  college?: string
  status?: string
  page?: number
  pageSize?: number
}

// 分页结果类型
export interface PageResult<T> {
  total: number
  list: T[]
}

/**
 * 查询用户列表
 */
export const getUserList = (params: UserQueryParams) => {
  return request.get<PageResult<User>>('/users', { params })
}

/**
 * 根据 ID 查询用户详情
 */
export const getUserById = (id: string) => {
  return request.get<User>(`/users/${id}`)
}

/**
 * 新增用户
 */
export const addUser = (data: Partial<User>) => {
  return request.post('/users', data)
}

/**
 * 更新用户
 */
export const updateUser = (id: string, data: Partial<User>) => {
  return request.put(`/users/${id}`, data)
}

/**
 * 删除用户
 */
export const deleteUser = (id: string) => {
  return request.delete(`/users/${id}`)
}

export const changePassword = (data: ChangePasswordRequest) => {
  return request.post('/users/changePassword', data)
}

/**
 * 上传用户头像
 */
export const uploadAvatar = (file: File, userId: number) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', userId.toString())
  return request.post<string>('/users/upload-avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

