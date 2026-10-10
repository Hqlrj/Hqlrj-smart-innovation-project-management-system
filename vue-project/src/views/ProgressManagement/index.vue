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
      title="AI审核进度"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="ai-review-progress">
        <el-steps :active="aiReviewProgress.currentStep" align-center>
          <el-step title="文档解析" />
          <el-step title="章节切分" />
          <el-step title="AI评审" />
          <el-step title="结果合并" />
        </el-steps>
        <div class="progress-status">
          <el-progress 
            :percentage="aiReviewProgress.percentage" 
            :status="aiReviewProgress.status"
          />
          <p class="status-text">{{ aiReviewProgress.statusText }}</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="aiReviewProgressVisible = false" :disabled="aiReviewProgress.isRunning">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AI审核结果弹窗 -->
    <el-dialog
      v-model="aiReviewResultVisible"
      title="AI审核结果"
      width="800px"
    >
      <div class="ai-review-result">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称">{{ aiReviewResult.projectName }}</el-descriptions-item>
          <el-descriptions-item label="综合得分">
            <el-tag :type="getScoreType(aiReviewResult.totalScore)" size="large">
              {{ aiReviewResult.totalScore }}分
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评审等级">
            <el-tag :type="getGradeType(aiReviewResult.grade)" size="large">
              {{ aiReviewResult.grade }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ aiReviewResult.reviewTime }}</el-descriptions-item>
        </el-descriptions>

        <div class="result-section">
          <h4>章节评审详情</h4>
          <el-table :data="aiReviewResult.sections" border style="width: 100%; margin-top: 10px">
            <el-table-column prop="sectionTitle" label="章节名称" width="200" />
            <el-table-column prop="score" label="得分" width="80" align="center">
              <template #default="{ row }">
                {{ row.score }}分
              </template>
            </el-table-column>
            <el-table-column prop="strengths" label="优点" show-overflow-tooltip>
              <template #default="{ row }">
                <div v-html="formatAiContent(row.strengths)"></div>
              </template>
            </el-table-column>
            <el-table-column prop="weaknesses" label="不足" show-overflow-tooltip>
              <template #default="{ row }">
                <div v-html="formatAiContent(row.weaknesses)"></div>
              </template>
            </el-table-column>
            <el-table-column prop="suggestions" label="建议" show-overflow-tooltip>
              <template #default="{ row }">
                <div v-html="formatAiContent(row.suggestions)"></div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="result-section">
          <h4>总体评价</h4>
          <div class="summary-text" v-html="formatAiContent(aiReviewResult.summary)"></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="aiReviewResultVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleApproveAfterAiReview">审批通过</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
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
  status: 'success' as any,
  statusText: '',
  isRunning: false,
  currentProjectId: 0
})

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
    aiReviewProgress.currentStep = 0
    aiReviewProgress.percentage = 0
    aiReviewProgress.statusText = '开始AI审核...'
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

// 轮询AI审核状态
const pollAiReviewStatus = async (taskId: number) => {
  const pollInterval = setInterval(async () => {
    try {
      const task = await request.get(`/projects/ai-review/${taskId}`)
      console.log('AI审核任务状态:', task)

      if (!task) {
        clearInterval(pollInterval)
        throw new Error('任务不存在')
      }

      // 更新进度
      updateProgressByStatus(task.status)

      if (task.status === 'DONE') {
        clearInterval(pollInterval)
        aiReviewProgress.isRunning = false
        aiReviewProgress.percentage = 100
        aiReviewProgress.statusText = '审核完成'
        
        // 停顿1秒后显示审核结果
        setTimeout(() => {
          showAiReviewResult(task, taskId)
        }, 1000)
      } else if (task.status === 'FAILED') {
        clearInterval(pollInterval)
        aiReviewProgress.isRunning = false
        aiReviewProgress.status = 'exception'
        aiReviewProgress.statusText = 'AI审核失败: ' + (task.errorMsg || task.errorMessage || '未知错误')
        ElMessage.error('AI审核失败: ' + (task.errorMsg || task.errorMessage || '未知错误'))
      }
    } catch (error: any) {
      console.error('轮询AI审核状态失败:', error)
      clearInterval(pollInterval)
      aiReviewProgress.isRunning = false
      aiReviewProgress.status = 'exception'
      aiReviewProgress.statusText = '查询审核状态失败'
      ElMessage.error(error.message || '查询审核状态失败')
    }
  }, 2000) // 每2秒轮询一次
}

// 根据状态更新进度
const updateProgressByStatus = (status: string) => {
  // 步骤0: 文档解析, 步骤1: 章节切分, 步骤2: AI评审, 步骤3: 结果合并
  const statusMap: Record<string, { step: number; percentage: number; text: string }> = {
    'PENDING': { step: 0, percentage: 5, text: '等待处理' },
    'PARSING': { step: 0, percentage: 15, text: '正在解析文档' },
    'CLEANING': { step: 0, percentage: 25, text: '正在清洗数据' },
    'SPLITTING': { step: 1, percentage: 40, text: '正在切分章节' },
    'REVIEWING': { step: 2, percentage: 60, text: '正在进行AI评审' },
    'SUMMARIZING': { step: 3, percentage: 85, text: '正在合并结果' },
    'DONE': { step: 4, percentage: 100, text: '审核完成' }
  }

  const progress = statusMap[status] || { step: 0, percentage: 0, text: '处理中' }
  aiReviewProgress.currentStep = progress.step
  aiReviewProgress.percentage = progress.percentage
  aiReviewProgress.statusText = progress.text
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

// 获取分数对应的标签类型
const getScoreType = (score: number) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'danger'
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

// 格式化AI返回的内容，去掉英文和括号，分条显示
const formatAiContent = (content: string) => {
  if (!content) return ''
  
  // 去掉JSON格式和括号
  let formatted = content
    .replace(/\[|\]|{|}/g, '') // 去掉方括号和花括号
    .replace(/"/g, '') // 去掉引号
    .replace(/,/g, '，') // 英文逗号替换为中文逗号
    .replace(/\./g, '。') // 英文句号替换为中文句号
    .replace(/:/g, '：') // 英文冒号替换为中文冒号
    .replace(/;/g, '；') // 英文分号替换为中文分号
  
  // 去掉常见英文单词（保留中文和数字）
  formatted = formatted.replace(/\b(strengths|weaknesses|risks|suggestions|point|evidence|target|from_section|severity|priority|target_section)\b/gi, '')
  
  // 将换行符转换为HTML换行
  formatted = formatted.replace(/\n/g, '<br>')
  
  // 如果内容中有序号，确保它们是中文格式
  formatted = formatted.replace(/(\d+)\./g, (match, p1) => {
    return `${p1}、`
  })
  
  // 去掉多余的空格
  formatted = formatted.replace(/\s+/g, ' ')
  
  return formatted.trim()
}

// 查看详情（跳转到详情页面）
const handleDetail = (project: Project) => {
  router.push(`/project-detail/${project.id}`)
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

// 组件挂载时获取数据
onMounted(() => {
  fetchProjectList()
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

/* AI审核相关样式 */
.ai-review-progress {
  padding: 20px 0;
}

/* 统一对话框标题字体 */
:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 自定义步骤条颜色 */
.ai-review-progress :deep(.el-step__head.is-wait) {
  color: #909399;
  border-color: #e4e7ed;
}

.ai-review-progress :deep(.el-step__head.is-wait .el-step__icon) {
  background: #e4e7ed;
  border-color: #dcdfe6;
  color: #909399;
}

.ai-review-progress :deep(.el-step__head.is-wait .el-step__line) {
  background: #e4e7ed;
}

/* 进行中的步骤 - 灰色 */
.ai-review-progress :deep(.el-step__head.is-process) {
  color: #909399;
  border-color: #e4e7ed;
}

.ai-review-progress :deep(.el-step__head.is-process .el-step__icon) {
  background: #e4e7ed;
  border-color: #dcdfe6;
  color: #909399;
}

.ai-review-progress :deep(.el-step__head.is-process .el-step__line) {
  background: #e4e7ed;
}

/* 已完成的步骤 - 绿色 */
.ai-review-progress :deep(.el-step__head.is-finish) {
  color: #67c23a;
  border-color: #67c23a;
}

.ai-review-progress :deep(.el-step__head.is-finish .el-step__icon) {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.ai-review-progress :deep(.el-step__head.is-finish .el-step__line) {
  background: #67c23a;
}

/* 步骤标题颜色和字体样式统一 */
.ai-review-progress :deep(.el-step__title) {
  font-size: 14px;
  font-weight: 500;
}

.ai-review-progress :deep(.el-step__title.is-wait) {
  color: #909399;
}

.ai-review-progress :deep(.el-step__title.is-process) {
  color: #909399;
}

.ai-review-progress :deep(.el-step__title.is-finish) {
  color: #67c23a;
}

.progress-status {
  margin-top: 30px;
  padding: 0 20px;
}

.status-text {
  text-align: center;
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.ai-review-result {
  padding: 10px 0;
}

.result-section {
  margin-top: 20px;
}

.result-section h4 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.summary-text {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #606266;
}
</style>
