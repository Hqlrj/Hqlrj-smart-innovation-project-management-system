/**
 * 登录状态管理 Store
 * 功能：
 * 1. 管理登录用户信息
 * 2. 持久化到 localStorage
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, type LoginRequest, type LoginResponse } from '@/api/login'
import { getUserById, type User } from '@/api/user'

// 用户信息类型
export interface UserInfo {
  id: number
  username: string
  name: string
  token: string
  [key: string]: any
}

export const useLoginStore = defineStore('login', () => {
  // 用户信息
  const userInfo = ref<UserInfo | null>(null)
  
  // 从 localStorage 恢复用户信息
  const initUserInfo = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch (e) {
        console.error('解析用户信息失败:', e)
        localStorage.removeItem('userInfo')
      }
    }
  }
  
  // 初始化时恢复用户信息
  initUserInfo()
  
  /**
   * 登录
   * @param loginData 登录信息
   * @returns 登录结果
   */
  const login = async (loginData: LoginRequest) => {
    try {
      const apiResponse = await loginApi(loginData)
      // 响应拦截器已经处理了数据，直接使用
      const response = apiResponse as any as LoginResponse
      
      // 注意：后端已经验证了状态，这里不需要再次检查
      // 如果后端返回了数据，说明状态已经是"已审批"
      
      // 先保存 token 到 sessionStorage（用于后续请求）
      sessionStorage.setItem('token', response.token)
      
      // 登录成功后，获取完整的用户信息（包括头像等）
      let fullUserInfo: any = {
        id: response.id,
        username: response.username,
        name: response.name,
        token: response.token,
        role: response.role || '',
        status: response.status || '',
        avatar: response.avatar || null
      }
      
      // 如果登录响应中没有头像，尝试从数据库获取完整用户信息
      if (!response.avatar && response.id) {
        try {
          const user = await getUserById(String(response.id)) as any as User
          if (user) {
            // 更新头像
            if (user.avatar) {
              fullUserInfo.avatar = user.avatar
            }
            // 合并其他用户信息（使用展开运算符，避免重复字段）
            fullUserInfo = {
              ...fullUserInfo,
              ...user
            }
          }
        } catch (e) {
          // 获取用户详情失败不影响登录，使用登录接口返回的信息
          console.warn('获取用户详情失败:', e)
        }
      }
      
      // 保存用户信息
      userInfo.value = fullUserInfo
      
      // 持久化到 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      
      return { success: true, data: response }
    } catch (error: any) {
      return { success: false, error: error.message || '登录失败' }
    }
  }
  
  /**
   * 登出
   */
  const logout = () => {
    userInfo.value = null
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }
  
  /**
   * 检查是否已登录
   */
  const isLoggedIn = () => {
    return userInfo.value !== null && sessionStorage.getItem('token') !== null
  }
  
  return {
    userInfo,
    login,
    logout,
    isLoggedIn,
    initUserInfo
  }
})

