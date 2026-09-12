<template>
  <div class="project-detail-container">
    <!-- 返回按钮 -->
    <div class="header-bar">
      <el-button
        type="primary"
        plain
        :icon="ArrowLeft"
        @click="handleBack"
      >
        返回
      </el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 基本信息标签页 -->
      <el-tab-pane label="基本信息" name="basic">
        <div v-if="project" class="basic-info">
          <!-- 项目概览卡片 -->
          <el-card shadow="hover" class="info-card overview-card">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><InfoFilled /></el-icon>
                <span>项目概览</span>
              </div>
            </template>
            <el-form
              :model="project"
              label-width="120px"
              class="project-form"
            >
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="项目类型">
                    <el-tag :type="getProjectTypeTagType(project.projectType)" size="default">
                      {{ project.projectType || '-' }}
                    </el-tag>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="项目状态">
                    <el-tag :type="getStatusType(project.status)" size="default">
                      {{ getStatusText(project.status) }}
                    </el-tag>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item v-if="project.status === 'rejected' && project.rejectReason" label="驳回原因">
                <el-input :model-value="project.rejectReason" type="textarea" :rows="3"
                  readonly class="readonly-textarea" style="color: #f56c6c;" />
              </el-form-item>
              <el-form-item label="项目名称">
                <el-input :model-value="project.projectName" readonly class="readonly-input" />
              </el-form-item>
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="项目所属学院">
                    <el-input :model-value="project.applicantCollege || '-'" readonly class="readonly-input" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="填报人">
                    <el-input :model-value="project.applicantName || '-'" readonly class="readonly-input" />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="填报时间">
                <el-input :model-value="formatDate(project.submitTime)" readonly class="readonly-input" />
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 项目详情卡片 -->
          <el-card shadow="hover" class="info-card detail-card">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><Document /></el-icon>
                <span>项目详情</span>
              </div>
            </template>
            <el-form
              :model="project"
              label-width="120px"
              class="project-form"
            >
              <el-form-item label="项目简介">
                <el-input
                  :model-value="project.projectIntro || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>

              <el-form-item label="项目创新点">
                <el-input
                  :model-value="project.innovationPoints || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>

              <el-form-item label="项目背景">
                <el-input
                  :model-value="project.projectBackground || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>

              <el-form-item label="项目意义">
                <el-input
                  :model-value="project.projectSignificance || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>

              <el-form-item label="市场需求分析">
                <el-input
                  :model-value="project.marketDemandAnalysis || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>

              <el-form-item label="项目关键技术介绍">
                <el-input
                  :model-value="project.keyTechnology || '-'"
                  type="textarea"
                  :rows="4"
                  readonly
                  resize="vertical"
                  class="readonly-textarea"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 计划书标签页 -->
      <el-tab-pane label="计划书" name="plan">
        <div class="plan-section">
          <!-- 优先显示 plans 数组中的计划书 -->
          <template v-if="project?.plans && project.plans.length > 0">
            <el-card shadow="hover" class="plan-card" v-for="(plan, index) in project.plans" :key="plan.id || index">
              <div class="file-display">
                <div class="file-icon-wrapper">
                  <el-icon class="file-icon" :size="64">
                    <Document />
                  </el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ plan.fileName }}</div>
                  <div class="file-actions">
                    <el-button type="success" :icon="Document" @click="handleDownloadPlanByPlan(plan)">下载文件</el-button>
                  </div>
                </div>
              </div>
            </el-card>
          </template>
          <!-- 如果没有 plans 数组，则显示 planFileName -->
          <el-card shadow="hover" class="plan-card" v-else-if="project?.planFileName">
            <div class="file-display">
              <div class="file-icon-wrapper">
                <el-icon class="file-icon" :size="64">
                  <Document />
                </el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ project.planFileName }}</div>
                <div class="file-actions">
                  <el-button type="success" :icon="Document" @click="handleDownloadPlan">下载文件</el-button>
                </div>
              </div>
            </div>
          </el-card>
          <el-card shadow="hover" class="plan-card" v-else>
            <el-empty description="暂无计划书" />
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 项目成员标签页 -->
      <el-tab-pane label="项目成员" name="members">
        <div class="members-section">
            <el-table
              :data="project?.members || []"
              border
              style="width: 100%"
              :empty-text="'暂无数据'"
            >
              <el-table-column prop="studentId" label="学号" width="120" align="center" header-align="center" />
              <el-table-column prop="name" label="姓名" width="120" align="center" header-align="center" />
              <el-table-column prop="gender" label="性别" width="80" align="center" header-align="center" />
              <el-table-column prop="phone" label="手机号" width="150" align="center" header-align="center" />
              <el-table-column prop="college" label="所属学院" width="150" align="center" header-align="center" />
              <el-table-column prop="className" label="班级" width="120" align="center" header-align="center" />
              <el-table-column prop="introduction" label="介绍" show-overflow-tooltip align="center" header-align="center" />
            </el-table>
        </div>
      </el-tab-pane>

      <!-- 指导老师标签页 -->
      <el-tab-pane label="指导老师" name="advisors">
        <div class="advisors-section">
            <el-table
              :data="project?.advisors || []"
              border
              style="width: 100%"
              :empty-text="'暂无数据'"
            >
              <el-table-column prop="name" label="姓名" width="120" align="center" header-align="center" />
              <el-table-column prop="gender" label="性别" width="80" align="center" header-align="center" />
              <el-table-column prop="phone" label="手机号" width="150" align="center" header-align="center" />
              <el-table-column prop="college" label="所属学院" width="150" align="center" header-align="center" />
              <el-table-column prop="introduction" label="介绍" show-overflow-tooltip align="center" header-align="center" />
            </el-table>
        </div>
      </el-tab-pane>

      <!-- 获奖记录标签页（只显示已审批的记录） -->
      <el-tab-pane label="获奖记录" name="awards">
        <div class="awards-section">
          <div v-if="approvedAwards.length > 0" class="awards-list">
            <el-card
              v-for="award in approvedAwards"
              :key="award.id"
              shadow="hover"
              class="award-card"
            >
              <div class="award-item">
                <div class="award-info">
                  <div class="award-main">
                    <el-icon class="award-icon"><Trophy /></el-icon>
                    <span class="award-competition">{{ award.competitionName }}</span>
                    <el-tag
                      v-if="award.competitionLevel"
                      :type="getCompetitionLevelType(award.competitionLevel)"
                      size="small"
                      class="award-level-tag"
                    >
                      {{ award.competitionLevel }}
                    </el-tag>
                  </div>
                  <div v-if="award.awardLevel" class="award-level">
                    <span class="award-level-label">获奖等级：</span>
                    <span class="award-level-text">{{ award.awardLevel }}</span>
                  </div>
                </div>
                <div v-if="award.awardCertificate" class="award-certificate">
                  <el-image
                    :src="getAwardCertificateUrl(award.awardCertificate)"
                    :preview-src-list="[getAwardCertificateUrl(award.awardCertificate)]"
                    fit="cover"
                    class="certificate-image"
                    :preview-teleported="true"
                  />
                </div>
              </div>
            </el-card>
          </div>
          <div v-else class="empty-awards">
            <el-empty description="暂无已审批的获奖记录" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Document, Trophy, InfoFilled } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getProjectById,
  type Project,
  type Award,
  type ProjectPlan
} from '@/api/project'

const route = useRoute()
const router = useRouter()

// 当前激活的标签页
const activeTab = ref('basic')

// 项目数据
const project = ref<Project | null>(null)

// 已审批的获奖记录（计算属性）
const approvedAwards = computed(() => {
  if (!project.value || !project.value.awards) {
    return []
  }
  // 过滤出状态为"approved"的获奖记录
  return project.value.awards.filter(award => award.status === 'approved')
})

// 获取项目详情
const fetchProjectDetail = async () => {
  const id = route.params.id as string
  console.log('项目ID:', id)
  
  if (!id || isNaN(Number(id))) {
    ElMessage.error('项目ID不存在或格式错误')
    router.back()
    return
  }

  const projectId = Number(id)

  try {
    // request 拦截器已经处理了 Result 格式，直接返回 data 部分
    const response = await getProjectById(projectId) as unknown as Project
    console.log('项目详情响应:', response)
    
    // response 已经是 Project 对象（经过拦截器处理）
    // 如果项目不存在，后端会返回错误，拦截器会显示错误消息并 reject
    if (response && response.id) {
      project.value = response
    } else {
      ElMessage.error('项目不存在')
      router.back()
    }
  } catch (error: any) {
    // 如果后端返回错误（如项目不存在），拦截器已经显示了错误消息
    // 这里只需要跳转回去，不显示重复的错误消息
    console.error('获取项目详情错误:', error)
    // 如果拦截器没有显示错误消息，这里才显示
    if (!error.response || error.response.status !== 401) {
      // 拦截器已经处理了错误消息，这里不需要重复显示
    }
    router.back()
  }
}

// 返回
const handleBack = () => {
  router.back()
}

// 获取文件URL
const getPlanFileUrl = (path: string) => {
  if (!path) return ''
  // 如果是完整URL，直接返回
  if (path.startsWith('http')) return path
  // 如果路径以 uploads 开头，通过代理访问
  if (path.startsWith('uploads/')) {
    return `/api/${path}`
  }
  // 否则拼接API地址
  return `/api/${path}`
}

// 下载计划书（从 planFilePath 和 planFileName）
const handleDownloadPlan = () => {
  if (!project.value?.planFilePath) return
  const fileUrl = getPlanFileUrl(project.value.planFilePath)
  // 创建临时a标签下载
  const link = document.createElement('a')
  link.href = fileUrl
  link.download = project.value.planFileName || '计划书'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 下载计划书（从 plans 数组）
const handleDownloadPlanByPlan = (plan: ProjectPlan) => {
  if (!plan.filePath) return
  const fileUrl = getPlanFileUrl(plan.filePath)
  // 创建临时a标签下载
  const link = document.createElement('a')
  link.href = fileUrl
  link.download = plan.fileName || '计划书'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
      return 'info'
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    case 'settled':
      return 'success'
    case 'draft':
      return ''
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status?: string) => {
  switch (status) {
    case 'submitted':
      return '已申报'
    case 'approved':
      return '审批通过'
    case 'rejected':
      return '已拒绝'
    case 'settled':
      return '入驻'
    case 'draft':
      return '草稿'
    default:
      return status || '-'
  }
}

// 获取项目类型标签颜色
const getProjectTypeTagType = (projectType?: string) => {
  switch (projectType) {
    case '信息技术服务':
      return 'success'
    case '制造业':
      return 'info'
    case '社会服务':
      return 'danger'
    case '文化创意服务':
      return 'warning'
    case '现代农业':
      return 'primary'
    default:
      return ''
  }
}

// 获取比赛级别标签颜色
const getCompetitionLevelType = (level?: string) => {
  switch (level) {
    case '国家级':
      return 'danger' // 红色
    case '省级':
      return 'warning' // 橙色
    case '校级':
      return 'success' // 绿色
    default:
      return 'info' // 灰色
  }
}

// 获取获奖证明图片URL
const getAwardCertificateUrl = (path: string) => {
  if (!path) return ''
  // 如果是完整URL，直接返回
  if (path.startsWith('http')) return path
  // 如果路径以 uploads 开头，通过代理访问
  if (path.startsWith('uploads/')) {
    return `/api/${path}`
  }
  // 否则拼接API地址
  return `/api/${path}`
}

// 组件挂载时获取数据
onMounted(() => {
  fetchProjectDetail()
})
</script>

<style scoped>
.project-detail-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
}

.header-bar {
  margin-bottom: 24px;
}

/* 标签页样式优化 */
.detail-tabs {
  margin-top: 24px;
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
  background: white;
  padding: 0 20px;
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  height: 3px;
}

:deep(.el-tabs__content) {
  padding: 0;
}

/* 卡片通用样式 */
.info-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-icon {
  color: #409eff;
  font-size: 18px;
}

.project-form {
  max-width: 100%;
}

/* 只读输入框样式 */
:deep(.readonly-input .el-input__inner) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
  cursor: default;
}

:deep(.readonly-textarea .el-textarea__inner) {
  background-color: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
  cursor: default;
}

/* 计划书卡片 */
.plan-section {
  padding: 20px 0;
}

.plan-card {
  border-radius: 12px;
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
}

.file-display {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px;
  width: 100%;
  box-sizing: border-box;
}

.file-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  flex-shrink: 0;
}

.file-icon {
  color: white;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
  width: 100%;
}

.file-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
  overflow-wrap: break-word;
  width: 100%;
}

.file-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.members-section,
.advisors-section {
  padding: 20px 0;
}

/* 表格样式 - 参考项目申报页面 */
:deep(.el-table) {
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

:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-all;
}

/* 获奖记录样式 */
.awards-section {
  padding: 0;
}

.awards-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
  padding: 0;
}

.award-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.award-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.award-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 8px;
}

.award-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.award-main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.award-icon {
  color: #f39c12;
  font-size: 24px;
  flex-shrink: 0;
}

.award-competition {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.award-level-tag {
  flex-shrink: 0;
}

.award-level {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.award-level-label {
  color: #909399;
}

.award-level-text {
  color: #409eff;
  font-weight: 600;
}

.award-certificate {
  flex-shrink: 0;
}

.certificate-image {
  width: 140px;
  height: 100px;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  cursor: pointer;
  object-fit: cover;
  transition: all 0.3s ease;
}

.certificate-image:hover {
  border-color: #409eff;
  transform: scale(1.05);
}

.empty-awards {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .awards-list {
    grid-template-columns: 1fr;
  }
  
  .award-item {
    flex-direction: column;
  }
  
  .certificate-image {
    width: 100%;
    height: auto;
  }
}

/* 标签页内容区域 */
:deep(.el-tab-pane) {
  padding: 0;
}

/* 优化卡片内边距 */
:deep(.el-card__body) {
  padding: 24px;
}

:deep(.el-card__header) {
  padding: 18px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
}
</style>

