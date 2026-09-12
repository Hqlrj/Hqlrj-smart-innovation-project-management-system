/**
 * Axios 请求封装
 * 功能：
 * 1. 请求拦截器：自动添加 token
 * 2. 响应拦截器：统一处理响应数据，处理 401 错误
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api', // 通过 Vite 代理转发到后端
  timeout: 5000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 登录接口和注册接口不需要token，直接放行
    const url = config.url || ''
    if (url.includes('/login') || url.includes('/users/register')) {
      return config
    }
    
    // 从 sessionStorage 获取 token
    const token = sessionStorage.getItem('token')
    if (token) {
      // 使用标准的 Authorization header 携带 JWT Token
      // 格式：Authorization: Bearer <token>
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 统一处理响应数据
    const res = response.data
    
    // 如果后端返回的格式是 {code, data, msg}
    if (res.code !== undefined) {
      // code === 1 表示成功
      if (res.code === 1) {
        return res.data
      } else {
        // 业务错误
        // 登录和注册接口的错误由调用方自行处理，不在此处显示提示
        const url = response.config?.url || ''
        if (!url.includes('/login') && !url.includes('/users/register')) {
          ElMessage.error(res.msg || '请求失败')
        }
        return Promise.reject(new Error(res.msg || '请求失败'))
      }
    }
    
    // 如果没有 code 字段，直接返回数据
    return res
  },
  (error) => {
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response
      
      // 401 未授权，清除 token 并跳转到登录页（登录接口除外）
      if (status === 401) {
        const url = error.config?.url || ''
        // 如果不是登录接口，才清除token并跳转
        if (!url.includes('/login')) {
          sessionStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          // 使用 window.location 跳转，避免在拦截器中使用 useRouter
          window.location.href = '/login'
          ElMessage.error(data?.msg || '登录已过期，请重新登录')
        } else {
          // 登录接口401错误，显示后端返回的错误信息
          return Promise.reject(error)
        }
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试')
      } else {
        // 显示后端返回的错误消息
        const errorMsg = data?.msg || error.message || '请求失败'
        ElMessage.error(errorMsg)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      // 请求配置错误
      ElMessage.error(error.message || '请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

export default request

