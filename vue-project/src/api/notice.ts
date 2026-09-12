/**
 * 公告管理相关 API
 */
import request from '@/utils/request'

// 公告类型
export interface Notice {
  id?: number
  title: string
  content: string
  publisherId?: number
  publisherName?: string
  publisherRole?: string
  isTop?: number
  status?: string
  createTime?: string
  updateTime?: string
}

// 查询参数
export interface NoticeQueryParams {
  title?: string
  page?: number
  pageSize?: number
}

// 分页结果
export interface PageResult<T> {
  total: number
  list: T[]
}

// 公告列表
export const getNoticeList = (params: NoticeQueryParams) => {
  return request.get<PageResult<Notice>>('/notices', { params })
}

// 公告详情
export const getNoticeById = (id: number) => {
  return request.get<Notice>(`/notices/${id}`)
}

// 发布公告
export const addNotice = (data: Partial<Notice>) => {
  return request.post('/notices', data) as Promise<Notice>
}

// 删除公告
export const deleteNotice = (id: number) => {
  return request.delete(`/notices/${id}`)
}
