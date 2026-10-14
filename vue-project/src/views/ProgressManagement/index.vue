<template>
  <div class="progress-management-container">
    <!-- 搜索/筛选区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称">
          <el-input
            v-model="searchForm.projectName"
            placeholder="请输入项目名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="searchForm.submitTime"
            type="date"
            placeholder="选择记录提交时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 项目列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="projectList"
        border
        style="width: 100%"
        :empty-text="'暂无数据'"
      >
        <el-table-column prop="projectName" label="项目名称" min-width="180" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" min-width="170" align="center" header-align="center">
          <template #default="{ row }">
            {{ formatDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="提交人" min-width="100" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="100" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditorName" label="审批人" min-width="100" align="center" header-align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.auditorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="projectIntro" label="成果概述" min-width="220" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column label="操作" min-width="280" align="center" header-align="center">
          <template #default="{ row }">
            <!-- 项目负责人：未审批显示修改、详情、删除，已审批显示详情、删除 -->
            <template v-if="isProjectLeader">
              <template v-if="row.status === 'submitted'">
                <el-button type="primary" size="small" @click="handleModify(row)">修改</el-button>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
              <template v-else>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </template>
            <!-- 评委老师或审核人：已审核显示详情，未审核显示审批和详情 -->
            <template v-else-if="isJudgeOrAuditor">
              <template v-if="row.status === 'submitted'">
                <!-- 未审核：显示AI审核、审批、详情 -->
                <el-button type="primary" size="small" @click="handleAiReview(row)">AI审核</el-button>
                <el-button type="success" size="small" @click="handleApprove(row)">审批</el-button>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
              </template>
              <template v-else>
                <!-- 已审核：只显示详情 -->
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
              </template>
            </template>
            <!-- 其他角色：保持原逻辑 -->
            <template v-else>
              <!-- 未审批状态：显示修改、审批、详情、删除 -->
              <template v-if="row.status === 'submitted'">
                <el-button type="primary" size="small" @click="handleModify(row)">修改</el-button>
                <el-button type="success" size="small" @click="handleApprove(row)">审批</el-button>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
              <!-- 已审批或驳回状态：显示详情、删除 -->
              <template v-else>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 审批确认弹窗：项目负责人不显示，评委老师和审核人显示 -->
    <el-dialog
      v-if="!isProjectLeader"
      v-model="approveDialogVisible"
      title="审批确认"
      width="500px"
    >
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="项目名称">
          <span>{{ approveForm.projectName }}</span>
        </el-form-item>
        <el-form-item label="审批操作">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="approve">通过</el-radio>
            <el-radio label="reject">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approveForm.action === 'reject'" label="驳回说明">
          <el-input v-model="approveForm.rejectReason" type="textarea" :rows="4"
            placeholder="请输入驳回原因说明" resize="vertical" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApproveConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AI审核进度弹窗 -->
    <el-dialog
      v-model="aiReviewProgressVisible"
      class="ai-review-dialog"
      width="680px"
      :close-on-click-modal="false"
      :show-close="!aiReviewProgress.isRunning"
    >
      <template #header>
        <div class="ai-dialog-header">
          <div class="ai-dialog-icon">
            <el-icon :size="22"><MagicStick /></el-icon>
          </div>
          <div>
            <p class="ai-dialog-title">AI 智能审核</p>
            <p class="ai-dialog-sub">正在阅读计划书并生成评审意见</p>
          </div>
        </div>
      </template>
      <div class="ai-review-progress">
        <el-steps :active="aiReviewProgress.currentStep" align-center finish-status="success">
          <el-step title="文档解析" />
          <el-step title="数据清洗" />
          <el-step title="章节切分" />
          <el-step title="AI评审" />
          <el-step title="结果合并" />
        </el-steps>
        <div class="progress-panel">
          <div class="progress-percent">{{ Math.round(aiReviewProgress.percentage) }}%</div>
          <el-progress
            :percentage="aiReviewProgress.percentage"
            :status="aiReviewProgress.status"
            :stroke-width="10"
            :show-text="false"
          />
          <p class="status-text">
            <span v-if="aiReviewProgress.isRunning" class="status-dot" />
            {{ aiReviewProgress.statusText }}
          </p>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiReviewProgressVisible = false" :disabled="aiReviewProgress.isRunning">关闭</el-button>
      </template>
    </el-dialog>

    <!-- AI审核结果弹窗 -->
    <el-dialog
      v-model="aiReviewResultVisible"
      class="ai-review-dialog"
      width="860px"
    >
      <template #header>
        <div class="ai-dialog-header">
          <div class="ai-dialog-icon is-done">
            <el-icon :size="22"><CircleCheck /></el-icon>
          </div>
          <div>
            <p class="ai-dialog-title">AI 审核结果</p>
            <p class="ai-dialog-sub">以下意见仅供参考，最终结论由人工确认</p>
          </div>
        </div>
      </template>
      <div class="ai-review-result">
        <div class="result-hero">
          <div class="score-ring" :class="'grade-' + (aiReviewResult.grade || 'C').toLowerCase()">
            <span class="score-value">{{ formatTotalScore(aiReviewResult.totalScore) }}</span>
            <span class="score-unit">分</span>
          </div>
          <div class="hero-meta">
            <h3 class="hero-name">{{ aiReviewResult.projectName || '未命名项目' }}</h3>
            <div class="hero-tags">
              <el-tag :type="getGradeType(aiReviewResult.grade)" effect="dark" round>
                等级 {{ aiReviewResult.grade || '-' }}
              </el-tag>
              <span class="hero-time">{{ aiReviewResult.reviewTime || '-' }}</span>
            </div>
            <p class="hero-hint">综合得分由各章节加权汇总，不由模型直接给出</p>
          </div>
        </div>

        <div class="result-block">
          <div class="block-title">总体评价</div>
          <div class="summary-card">{{ formatAiPlain(aiReviewResult.summary) || '暂无总体评价' }}</div>
        </div>

        <div class="result-block">
          <div class="block-title">章节评审</div>
          <div v-if="!aiReviewResult.sections.length" class="empty-hint">暂无章节详情</div>
          <div v-else class="section-list">
            <div v-for="(row, index) in aiReviewResult.sections" :key="index" class="section-card">
              <div class="section-head">
                <span class="section-index">{{ index + 1 }}</span>
                <span class="section-name">{{ row.sectionTitle || '未命名章节' }}</span>
                <span class="section-score" :class="sectionScoreClass(row.score)">
                  {{ formatSectionScore(row.score) }}
                </span>
              </div>
              <div class="section-grid">
                <div class="opinion-col is-good">
                  <p class="opinion-label">优点</p>
                  <ul>
                    <li v-for="(item, i) in splitAiLines(row.strengths)" :key="'s' + i">{{ item }}</li>
                    <li v-if="!splitAiLines(row.strengths).length" class="is-empty">暂无</li>
                  </ul>
                </div>
                <div class="opinion-col is-bad">
                  <p class="opinion-label">不足</p>
                  <ul>
                    <li v-for="(item, i) in splitAiLines(row.weaknesses)" :key="'w' + i">{{ item }}</li>
                    <li v-if="!splitAiLines(row.weaknesses).length" class="is-empty">暂无</li>
                  </ul>
                </div>
                <div class="opinion-col is-tip">
                  <p class="opinion-label">建议</p>
                  <ul>
                    <li v-for="(item, i) in splitAiLines(row.suggestions)" :key="'g' + i">{{ item }}</li>
                    <li v-if="!splitAiLines(row.suggestions).length" class="is-empty">暂无</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="aiReviewResultVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleApproveAfterAiReview">审批通过</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, MagicStick, CircleCheck } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginStore } from '@/stores/login'
import request from '@/utils/request'
import {
  getProjectList,
  deleteProject,
  approveProject,
  rejectProject,
  type Project,
  type ProjectQueryParams,
  type PageResult
} from '@/api/project'

const router = useRouter()
const loginStore = useLoginStore()

// 搜索表单
const searchForm = reactive({
  projectName: '',
  submitTime: ''
})

// 项目列表数据
const projectList = ref<Project[]>([])
const loading = ref(false)

// 分页数据
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 审批弹窗
const approveDialogVisible = ref(false)
const approveForm = reactive({
  projectId: 0,
  projectName: '',
  action: 'approve' as 'approve' | 'reject',
  rejectReason: ''
})

// AI审核进度弹窗
const aiReviewProgressVisible = ref(false)
const aiReviewProgress = reactive({
  currentStep: 0,
  percentage: 0,
  status: '' as any,
  statusText: '',
  isRunning: false,
  currentProjectId: 0
})

let aiReviewPollTimer: ReturnType<typeof setInterval> | null = null
const AI_REVIEW_MAX_POLLS = 450
const AI_REVIEW_STATUS_ORDER = ['PENDING', 'PARSING', 'CLEANING', 'SPLITTING', 'REVIEWING', 'SUMMARIZING', 'DONE']
const AI_REVIEW_STATUS_MAP: Record<string, { step: number; percentage: number; text: string }> = {
  PENDING: { step: 0, percentage: 5, text: '等待处理' },
  PARSING: { step: 0, percentage: 12, text: '正在解析文档' },
  CLEANING: { step: 1, percentage: 28, text: '正在清洗数据' },
  SPLITTING: { step: 2, percentage: 42, text: '正在切分章节' },
  REVIEWING: { step: 3, percentage: 65, text: '正在进行AI评审' },
  SUMMARIZING: { step: 4, percentage: 88, text: '正在合并结果' },
  DONE: { step: 5, percentage: 100, text: '审核完成' }
}

let displayedAiStatus = 'PENDING'
let latestBackendStatus = 'PENDING'
let progressCatchUpRunning = false
let pendingDoneTask: { task: any; taskId: number } | null = null

const clearAiReviewPoll = () => {
  if (aiReviewPollTimer) {
    clearInterval(aiReviewPollTimer)
    aiReviewPollTimer = null
  }
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const applyAiReviewStatus = (status: string) => {
  const progress = AI_REVIEW_STATUS_MAP[status] || { step: 0, percentage: 0, text: '处理中' }
  aiReviewProgress.currentStep = progress.step
  aiReviewProgress.percentage = progress.percentage
  aiReviewProgress.statusText = progress.text
}

const catchUpAiReviewProgress = async () => {
  if (progressCatchUpRunning) {
    return
  }
  progressCatchUpRunning = true
  try {
    while (latestBackendStatus !== 'FAILED') {
      const from = AI_REVIEW_STATUS_ORDER.indexOf(displayedAiStatus)
      const to = AI_REVIEW_STATUS_ORDER.indexOf(latestBackendStatus)
      if (from < 0 || to < 0 || from >= to) {
        break
      }
      const next = AI_REVIEW_STATUS_ORDER[from + 1]
      displayedAiStatus = next
      applyAiReviewStatus(next)
      if (next !== 'DONE' && next !== latestBackendStatus) {
        await sleep(600)
      }
    }
    if (latestBackendStatus === 'DONE' && displayedAiStatus === 'DONE' && pendingDoneTask) {
      const done = pendingDoneTask
      pendingDoneTask = null
      aiReviewProgress.isRunning = false
      showAiReviewResult(done.task, done.taskId)
    }
  } finally {
    progressCatchUpRunning = false
  }
}

// AI审核结果弹窗
const aiReviewResultVisible = ref(false)
const aiReviewResult = reactive({
  projectName: '',
  totalScore: 0,
  grade: '',
  reviewTime: '',
  summary: '',
  sections: [] as any[]
})

// 判断是否是项目负责人
const isProjectLeader = computed(() => {
  return loginStore.userInfo?.role === '项目负责人'
})

// 判断是否是评委老师或审核人
const isJudgeOrAuditor = computed(() => {
  const role = loginStore.userInfo?.role
  return role === '评委老师' || role === '审批人'
})

// 获取项目列表
const fetchProjectList = async () => {
  loading.value = true
  try {
    const params: ProjectQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      projectName: searchForm.projectName || undefined
    }
    
    // 项目负责人只查询自己的项目
    if (isProjectLeader.value) {
      const userId = loginStore.userInfo?.id
      params.applicantId = userId
    }
    
    // 如果有提交时间，需要特殊处理（这里简化处理，实际可能需要后端支持）
    const response = await getProjectList(params) as unknown as PageResult<Project>
    
    // 前端过滤：排除草稿状态，只显示已申报的项目
    let filteredList = (response?.list ?? []).filter(item => item.status !== 'draft')
    
    // 前端过滤提交时间
    if (searchForm.submitTime) {
      filteredList = filteredList.filter(item => {
        if (!item.submitTime) return false
        const submitDate = item.submitTime.split(' ')[0] // 取日期部分
        return submitDate === searchForm.submitTime
      })
    }
    
    projectList.value = filteredList
    pagination.total = response?.total ?? 0
  } catch (error: any) {
    ElMessage.error(error.message || '获取项目列表失败')
    projectList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchProjectList()
}

// 重置
const handleReset = () => {
  searchForm.projectName = ''
  searchForm.submitTime = ''
  pagination.page = 1
  fetchProjectList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchProjectList()
}

// 页码改变
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchProjectList()
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取状态类型（用于标签颜色）
const getStatusType = (status?: string) => {
  switch (status) {
    case 'submitted':
      return 'info' // 灰色（未审批）
    case 'approved':
      return 'success' // 绿色（已审批）
    case 'rejected':
      return 'danger' // 红色（驳回）
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status?: string) => {
  switch (status) {
    case 'submitted':
      return '未审批'
    case 'approved':
      return '已审批'
    case 'rejected':
      return '驳回'
    case 'draft':
      return '草稿'
    default:
      return status || '-'
  }
}

// 修改项目
const handleModify = (project: Project) => {
  router.push(`/project-apply/${project.id}`)
}

// 审批项目
const handleApprove = (project: Project) => {
  approveForm.projectId = project.id!
  approveForm.projectName = project.projectName
  approveForm.action = 'approve'
  approveDialogVisible.value = true
}

// 审批确认
const handleApproveConfirm = async () => {
  try {
    // 获取当前登录用户的真实姓名
    const auditorName = loginStore.userInfo?.name || loginStore.userInfo?.username || '未知'
    
    // 找到列表中对应的项目
    const projectIndex = projectList.value.findIndex(p => p.id === approveForm.projectId)
    
    if (approveForm.action === 'approve') {
      await approveProject(approveForm.projectId)
      ElMessage.success('审批通过')
      
      // 立即更新列表中对应项目的状态和审批人
      if (projectIndex !== -1) {
        projectList.value[projectIndex].status = 'approved'
        projectList.value[projectIndex].auditorName = auditorName
      }
    } else {
      await rejectProject(approveForm.projectId, approveForm.rejectReason)
      ElMessage.success('已驳回')
      
      // 立即更新列表中对应项目的状态和审批人
      if (projectIndex !== -1) {
        projectList.value[projectIndex].status = 'rejected'
        projectList.value[projectIndex].auditorName = auditorName
        projectList.value[projectIndex].rejectReason = approveForm.rejectReason
      }
    }
    approveForm.rejectReason = ''
    approveDialogVisible.value = false
    // 刷新列表以确保数据同步
    fetchProjectList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// AI审核
const handleAiReview = async (project: Project) => {
  // 检查用户是否配置了API Key
  try {
    const response = await request.get('/users/api-key')
    const hasApiKey = response.hasKey === 'true' || response.hasKey === true
    
    if (!hasApiKey) {
      ElMessage.warning('请先在个人设置中配置API Key')
      return
    }
    
    // 已配置API Key，直接开始AI审核
    startAiReviewProcess(project.id!, project.projectName)
  } catch (error) {
    console.error('检查API Key失败:', error)
    ElMessage.error('检查API Key配置失败')
  }
}

// 开始AI审核流程
const startAiReviewProcess = async (projectId: number, projectName: string) => {
  try {
    aiReviewProgressVisible.value = true
    
    // 初始化进度
    clearAiReviewPoll()
    displayedAiStatus = 'PENDING'
    latestBackendStatus = 'PENDING'
    pendingDoneTask = null
    progressCatchUpRunning = false
    aiReviewProgress.currentStep = 0
    aiReviewProgress.percentage = 5
    aiReviewProgress.status = ''
    aiReviewProgress.statusText = '任务已提交，等待后台处理...'
    aiReviewProgress.isRunning = true
    aiReviewProgress.currentProjectId = projectId

    // 调用后端API启动AI审核（不传apiKey，使用用户配置的）
    const response = await request.post(`/projects/${projectId}/start-review`, {
      mode: 'AI'
    })

    const taskId = response?.taskId
    if (taskId) {
      // 开始轮询任务状态
      pollAiReviewStatus(taskId)
    } else {
      // 后端返回了错误消息，这里不应该执行到，因为响应拦截器已经处理了错误
      aiReviewProgressVisible.value = false
      aiReviewProgress.isRunning = false
    }
  } catch (error: any) {
    // 响应拦截器已经处理了错误消息显示，这里只需要关闭进度弹窗
    aiReviewProgressVisible.value = false
    aiReviewProgress.isRunning = false
  }
}

const pollAiReviewStatus = async (taskId: number) => {
  clearAiReviewPoll()
  let pollCount = 0
  aiReviewPollTimer = setInterval(async () => {
    pollCount++
    if (pollCount > AI_REVIEW_MAX_POLLS) {
      clearAiReviewPoll()
      aiReviewProgress.isRunning = false
      aiReviewProgress.status = 'exception'
      aiReviewProgress.statusText = '审核超时，请稍后在进度管理中查看结果'
      ElMessage.error('AI审核超时')
      return
    }
    try {
      const task = await request.get(`/projects/ai-review/${taskId}`)

      if (!task) {
        clearAiReviewPoll()
        throw new Error('任务不存在')
      }

      latestBackendStatus = task.status
      catchUpAiReviewProgress()

      if (task.status === 'DONE') {
        clearAiReviewPoll()
        pendingDoneTask = { task, taskId }
        catchUpAiReviewProgress()
      } else if (task.status === 'FAILED') {
        clearAiReviewPoll()
        aiReviewProgress.isRunning = false
        aiReviewProgress.status = 'exception'
        aiReviewProgress.statusText = 'AI审核失败: ' + (task.errorMsg || task.errorMessage || '未知错误')
        ElMessage.error('AI审核失败: ' + (task.errorMsg || task.errorMessage || '未知错误'))
      }
    } catch (error: any) {
      console.error('轮询AI审核状态失败:', error)
      clearAiReviewPoll()
      aiReviewProgress.isRunning = false
      aiReviewProgress.status = 'exception'
      aiReviewProgress.statusText = '查询审核状态失败'
      ElMessage.error(error.message || '查询审核状态失败')
    }
  }, 800)
}

// 显示AI审核结果
const showAiReviewResult = async (task: any, taskId: number) => {
  aiReviewResult.projectName = task.projectName || ''
  aiReviewResult.totalScore = task.totalScore || 0
  aiReviewResult.grade = task.grade || ''
  aiReviewResult.reviewTime = task.endTime ? formatDate(task.endTime) : ''
  aiReviewResult.summary = task.summary || '暂无总体评价'
  
  // 获取章节详情
  try {
    const sections = await request.get(`/projects/ai-review/${taskId}/sections`)
    aiReviewResult.sections = sections || []
  } catch (error) {
    console.error('获取章节详情失败:', error)
    aiReviewResult.sections = []
  }
  
  aiReviewProgressVisible.value = false
  aiReviewResultVisible.value = true
}

// AI审核后审批通过
const handleApproveAfterAiReview = async () => {
  try {
    await approveProject(aiReviewProgress.currentProjectId)
    ElMessage.success('审批通过')
    aiReviewResultVisible.value = false
    fetchProjectList()
  } catch (error: any) {
    ElMessage.error(error.message || '审批失败')
  }
}

const formatTotalScore = (score: number) => {
  if (score == null || Number.isNaN(Number(score))) return '--'
  return Number(score).toFixed(1)
}

const formatSectionScore = (score: number) => {
  if (score == null || Number.isNaN(Number(score))) return '--'
  return `${Number(score).toFixed(1)} / 5`
}

const sectionScoreClass = (score: number) => {
  if (score >= 4) return 'is-high'
  if (score >= 3) return 'is-mid'
  return 'is-low'
}

const formatAiPlain = (content: string) => {
  if (!content) return ''
  return content
    .replace(/```json|```/g, '')
    .replace(/^[{\[]|[}\]]$/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

const splitAiLines = (content: string) => {
  if (!content) return [] as string[]
  return content
    .split(/\n+/)
    .map((line) => line.replace(/^[\d]+[、.．]\s*/, '').trim())
    .filter((line) => line.length > 0)
}

// 获取等级对应的标签类型
const getGradeType = (grade: string) => {
  switch (grade) {
    case 'A':
      return 'success'
    case 'B':
      return 'warning'
    case 'C':
      return 'info'
    case 'D':
      return 'danger'
    default:
      return ''
  }
}

// 查看详情（跳转到详情页面）
const handleDetail = (project: Project) => {
  const id = project?.id
  if (id == null) {
    ElMessage.error('项目ID不存在，无法查看详情')
    return
  }
  router.push({ name: 'project-detail', params: { id: String(id) } })
}

// 删除项目
const handleDelete = async (project: Project) => {
  ElMessageBox.confirm(
    `确定要删除项目 "${project.projectName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteProject(project.id!)
      ElMessage.success('删除成功')
      fetchProjectList() // 刷新列表
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

onMounted(() => {
  fetchProjectList()
})

onUnmounted(() => {
  clearAiReviewPoll()
})
</script>

<style scoped>
.progress-management-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 20px;
  background: white;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  padding: 0;
  overflow-x: auto;
}

.pagination-container {
  display: flex;
  justify-content: flex-start;
  padding: 20px 0;
  background: white;
}

:deep(.el-table) {
  width: 100% !important;
  background: white;
}

:deep(.el-table th) {
  background: white;
  color: #333;
  font-weight: 600;
}

:deep(.el-table__body tr td) {
  background: white;
}

:deep(.el-table__body tr:hover > td) {
  background: white;
}

.ai-dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-dialog-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  flex-shrink: 0;
}

.ai-dialog-icon.is-done {
  background: linear-gradient(135deg, #2e7d32 0%, #66bb6a 100%);
}

.ai-dialog-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #1f2a37;
  line-height: 1.3;
}

.ai-dialog-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #8a94a6;
}

.ai-review-progress {
  padding: 8px 4px 4px;
}

.ai-review-progress :deep(.el-step__title) {
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
}

.ai-review-progress :deep(.el-step__head.is-wait .el-step__icon) {
  background: #f2f4f7;
  border-color: #e5e7eb;
  color: #9aa3b2;
}

.ai-review-progress :deep(.el-step__title.is-wait) {
  color: #9aa3b2;
}

.ai-review-progress :deep(.el-step__head.is-process) {
  color: #1976d2;
  border-color: #1976d2;
}

.ai-review-progress :deep(.el-step__head.is-process .el-step__icon) {
  background: #1976d2;
  border-color: #1976d2;
  color: #fff;
  box-shadow: 0 0 0 6px rgba(25, 118, 210, 0.12);
}

.ai-review-progress :deep(.el-step__title.is-process) {
  color: #1976d2;
  font-weight: 600;
}

.ai-review-progress :deep(.el-step__head.is-finish) {
  color: #2e7d32;
  border-color: #2e7d32;
}

.ai-review-progress :deep(.el-step__head.is-finish .el-step__icon) {
  background: #2e7d32;
  border-color: #2e7d32;
  color: #fff;
}

.ai-review-progress :deep(.el-step__head.is-finish .el-step__line) {
  background: #a5d6a7;
}

.ai-review-progress :deep(.el-step__title.is-finish) {
  color: #2e7d32;
}

.progress-panel {
  margin: 28px 12px 8px;
  padding: 20px 22px 18px;
  background: linear-gradient(180deg, #f7fbff 0%, #f4f7fb 100%);
  border: 1px solid #e8eef6;
  border-radius: 14px;
}

.progress-percent {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #1976d2;
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}

.progress-panel :deep(.el-progress-bar__outer) {
  background: #e3ecf5;
}

.status-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-align: center;
  margin: 14px 0 0;
  color: #5b6573;
  font-size: 14px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1976d2;
  animation: pulse-dot 1.2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 0.35; transform: scale(0.9); }
  50% { opacity: 1; transform: scale(1.15); }
}

.ai-review-result {
  padding: 4px 2px 8px;
}

.result-hero {
  display: flex;
  gap: 20px;
  align-items: center;
  padding: 18px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f4f9ff 0%, #eef6ff 48%, #f7fbff 100%);
  border: 1px solid #dceaf8;
}

.score-ring {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #fff;
  box-shadow: 0 8px 24px rgba(25, 118, 210, 0.12);
  border: 4px solid #90caf9;
}

.score-ring.grade-a { border-color: #66bb6a; }
.score-ring.grade-b { border-color: #ffa726; }
.score-ring.grade-c { border-color: #42a5f5; }
.score-ring.grade-d { border-color: #ef5350; }

.score-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2a37;
  line-height: 1;
}

.score-unit {
  margin-top: 4px;
  font-size: 12px;
  color: #8a94a6;
}

.hero-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1f2a37;
}

.hero-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hero-time {
  font-size: 13px;
  color: #8a94a6;
}

.hero-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #90a0b5;
}

.result-block {
  margin-top: 20px;
}

.block-title {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 10px;
}

.summary-card {
  padding: 16px 18px;
  background: #f8fafc;
  border-radius: 12px;
  line-height: 1.8;
  color: #475569;
  font-size: 14px;
  border: 1px solid #eef2f6;
}

.empty-hint {
  color: #94a3b8;
  font-size: 13px;
  padding: 12px 0;
}

.section-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 420px;
  overflow-y: auto;
  padding-right: 4px;
}

.section-card {
  border: 1px solid #eef2f6;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
}

.section-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  background: #f8fbff;
  border-bottom: 1px solid #eef2f6;
}

.section-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #1976d2;
  color: #fff;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.section-name {
  flex: 1;
  font-size: 14px;
  font-weight: 600;
  color: #1f2a37;
}

.section-score {
  font-size: 13px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 999px;
}

.section-score.is-high { color: #2e7d32; background: #e8f5e9; }
.section-score.is-mid { color: #ef6c00; background: #fff3e0; }
.section-score.is-low { color: #c62828; background: #ffebee; }

.section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0;
}

.opinion-col {
  padding: 12px 14px 14px;
  min-height: 88px;
}

.opinion-col + .opinion-col {
  border-left: 1px solid #f1f5f9;
}

.opinion-label {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 600;
}

.opinion-col.is-good .opinion-label { color: #2e7d32; }
.opinion-col.is-bad .opinion-label { color: #c62828; }
.opinion-col.is-tip .opinion-label { color: #1565c0; }

.opinion-col ul {
  margin: 0;
  padding-left: 16px;
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}

.opinion-col li.is-empty {
  list-style: none;
  margin-left: -16px;
  color: #cbd5e1;
}

</style>

<style>
.ai-review-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}
.ai-review-dialog .el-dialog__header {
  padding: 18px 20px 8px !important;
  margin-right: 0 !important;
}
.ai-review-dialog .el-dialog__body {
  padding: 8px 20px 12px !important;
}
.ai-review-dialog .el-dialog__footer {
  padding: 8px 20px 16px !important;
}
</style>
