/**
 * 项目申报相关 API 接口
 */
import request from '@/utils/request'

// 项目数据类型
export interface Project {
  id?: number
  projectType?: string
  projectName: string
  projectIntro?: string
  innovationPoints?: string
  projectBackground?: string
  projectSignificance?: string
  marketDemandAnalysis?: string
  keyTechnology?: string
  planFilePath?: string
  planFileName?: string
  status?: string
  applicantId?: number
  applicantName?: string
  auditorName?: string
  rejectReason?: string
  projectSpace?: string
  applicantCollege?: string
  createTime?: string
  updateTime?: string
  submitTime?: string
  members?: ProjectMember[]
  advisors?: ProjectAdvisor[]
  awards?: Award[]
  plans?: ProjectPlan[]
}

// 项目成员数据类型
export interface ProjectMember {
  id?: number
  projectId?: number
  studentId: string
  name: string
  gender?: string
  phone?: string
  college?: string
  className?: string
  introduction?: string
  createTime?: string
  updateTime?: string
}

// 项目指导老师数据类型
export interface ProjectAdvisor {
  id?: number
  projectId?: number
  name: string
  gender?: string
  phone?: string
  college?: string
  introduction?: string
  createTime?: string
  updateTime?: string
}

// 项目查询参数
export interface ProjectQueryParams {
  projectName?: string
  projectType?: string
  status?: string
  projectSpace?: string
  applicantId?: number
  page?: number
  pageSize?: number
}

// 获奖记录数据类型
export interface Award {
  id?: number
  projectId?: number
  projectName?: string
  competitionName: string
  competitionLevel?: string
  awardLevel?: string
  awardCertificate?: string
  status?: string // 审批状态：pending-未审批, approved-已审批通过, rejected-已审批未通过
  rejectReason?: string // 未通过原因说明
  createTime?: string
  updateTime?: string
}

// 获奖记录查询参数
export interface AwardQueryParams {
  projectName?: string
  competitionName?: string
  competitionLevel?: string
  awardLevel?: string
  applicantId?: number // 项目申请人ID（用于过滤）
  page?: number
  pageSize?: number
}

// 分页结果类型
export interface PageResult<T> {
  total: number
  list: T[]
}

/**
 * 查询项目列表
 */
export const getProjectList = (params: ProjectQueryParams) => {
  return request.get<PageResult<Project>>('/projects', { params })
}

/**
 * 根据 ID 查询项目详情
 */
export const getProjectById = (id: number) => {
  return request.get<Project>(`/projects/${id}`)
}

/**
 * 新增项目
 */
export const addProject = (data: Partial<Project>) => {
  return request.post('/projects', data) as Promise<Project>
}

/**
 * 更新项目
 */
export const updateProject = (id: number, data: Partial<Project>) => {
  return request.put(`/projects/${id}`, data)
}

/**
 * 删除项目
 */
export const deleteProject = (id: number) => {
  return request.delete(`/projects/${id}`)
}

/**
 * 提交项目申报
 */
export const submitProject = (id: number) => {
  return request.post(`/projects/${id}/submit`)
}

// 计划书数据类型
export interface ProjectPlan {
  id?: number
  projectId?: number
  filePath: string
  fileName: string
  fileSize?: number
  createTime?: string
  updateTime?: string
}

/**
 * 上传计划书文件（支持多文件）
 */
export const uploadPlan = (files: File[], projectId?: number) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  if (projectId) {
    formData.append('projectId', projectId.toString())
  }
  return request.post<ProjectPlan[]>('/projects/upload-plan', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除计划书文件
 */
export const deletePlan = (id: number) => {
  return request.delete(`/projects/plan/${id}`)
}

/**
 * 下载计划书模板
 */
export const downloadTemplate = () => {
  return request.get<string>('/projects/download-template')
}

/**
 * 审批项目（通过）
 */
export const approveProject = (id: number) => {
  return request.post(`/projects/${id}/approve`)
}

/**
 * 驳回项目
 */
export const rejectProject = (id: number, reason?: string) => {
  return request.post(`/projects/${id}/reject`, { reason })
}

/**
 * 分页查询获奖记录列表
 */
export const getAwardList = (params: AwardQueryParams) => {
  return request.get<PageResult<Award>>('/awards', { params })
}

/**
 * 根据项目ID查询获奖记录列表
 */
export const getAwardsByProjectId = (projectId: number) => {
  return request.get<Award[]>(`/awards/project/${projectId}`)
}

/**
 * 根据ID查询获奖记录
 */
export const getAwardById = (id: number) => {
  return request.get<Award>(`/awards/${id}`)
}

/**
 * 新增获奖记录
 */
export const addAward = (data: Partial<Award>) => {
  return request.post('/awards', data) as Promise<Award>
}

/**
 * 更新获奖记录
 */
export const updateAward = (id: number, data: Partial<Award>) => {
  return request.put(`/awards/${id}`, data)
}

/**
 * 删除获奖记录
 */
export const deleteAward = (id: number) => {
  return request.delete(`/awards/${id}`)
}

/**
 * 审批获奖记录（通过）
 */
export const approveAward = (id: number) => {
  return request.post(`/awards/${id}/approve`)
}

/**
 * 审批获奖记录（未通过）
 */
export const rejectAward = (id: number, reason?: string) => {
  // 将reason作为JSON对象发送，避免URL编码问题
  return request.post(`/awards/${id}/reject`, { reason })
}

/**
 * 上传获奖证明图片
 */
export const uploadAwardCertificate = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<string>('/awards/upload-certificate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取当前用户创建的项目列表（用于下拉选择）
 */
export const getMyProjects = () => {
  return request.get<Project[]>('/projects/my-projects')
}
