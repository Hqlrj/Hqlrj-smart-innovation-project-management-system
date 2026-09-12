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
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="projectList"
          border
          style="width: 100%"
          :empty-text="'暂无数据'"
        >
        <el-table-column prop="projectName" label="项目名称" width="200" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="190" align="center" header-align="center">
          <template #default="{ row }">
            {{ formatDate(row.submitTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="提交人" width="130" align="center" header-align="center" />
        <el-table-column prop="status" label="状态" width="130" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditorName" label="审批人" width="130" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.auditorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="projectIntro" label="成果概述" width="286" align="center" header-align="center" show-overflow-tooltip />
       
        <el-table-column label="操作" width="320" fixed="right" align="center" header-align="center">
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
                <!-- 未审核：显示审批、详情 -->
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
      </div>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginStore } from '@/stores/login'
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
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-wrapper {
  width: 100%;
  overflow-x: auto;
}

/* 确保表格有足够的最小宽度，避免列被挤压 */
.table-wrapper :deep(.el-table) {
  min-width: 1200px;
}

.pagination-container {
  display: flex;
  justify-content: flex-start;
  padding: 20px 0;
  background: white;
}

/* 确保表格颜色为白色 */
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

/* 表格单元格换行，长文本自动折行 */
:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-all;
}

</style>
