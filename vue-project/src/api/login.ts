/**
 * 登录相关 API 接口
 */
import request from '@/utils/request'

// 登录请求参数类型
export interface LoginRequest {
  username: string
  password: string
}

// 登录响应数据类型
export interface LoginResponse {
  token: string
  id: number
  username: string
  name: string
  role?: string
  status?: string
  avatar?: string
  [key: string]: any
}

// 注册请求参数类型
export interface RegisterRequest {
  studentId: string
  name: string
  phone: string
  password: string
  roleId: string
  college: string
}

/**
 * 用户登录
 * @param data 登录信息
 * @returns 登录响应数据
 */
export const login = (data: LoginRequest) => {
  return request.post<LoginResponse>('/login', data)
}

/**
 * 用户登出
 */
export const logout = () => {
  return request.post('/logout')
}

/**
 * 用户注册
 * @param data 注册信息
 */
export const register = (data: RegisterRequest) => {
  return request.post('/users/register', data)
}

/** 发送重置密码验证码 */
export const sendResetCode = (phone: string) => {
  return request.post('/auth/send-code', { phone })
}

/** 重置密码 */
export const resetPassword = (phone: string, code: string, newPassword: string) => {
  return request.post('/auth/reset-password', { phone, code, newPassword })
}

