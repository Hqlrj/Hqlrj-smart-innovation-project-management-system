/**
 * 站内通知相关 API 接口
 */
import request from '@/utils/request'

// 通知数据类型
export interface Notification {
  id: number
  userId: number
  title: string
  content: string
  type: string
  relatedId: number
  isRead: number
  createTime: string
}

// 分页结果类型
export interface PageResult<T> {
  total: number
  list: T[]
}

/**
 * 获取当前用户通知列表（分页）
 */
export const getNotificationList = (params: { page?: number; pageSize?: number }) => {
  return request.get<PageResult<Notification>>('/notifications', { params })
}

/**
 * 获取未读通知数量
 */
export const getUnreadCount = () => {
  return request.get<number>('/notifications/unread-count')
}

/**
 * 标记单条通知为已读
 */
export const markAsRead = (id: number) => {
  return request.put(`/notifications/${id}/read`)
}

/**
 * 标记所有通知为已读
 */
export const markAllRead = () => {
  return request.put('/notifications/read-all')
}