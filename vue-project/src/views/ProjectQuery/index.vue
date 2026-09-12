<template>
  <div class="project-query-container">
    <!-- 搜索/筛选区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称:">
          <el-input
            v-model="searchForm.projectName"
            placeholder="请输入项目名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <!-- <el-form-item label="项目空间:">
          <el-input
            v-model="searchForm.projectSpace"
            placeholder="请输入项目空间"
            clearable
            style="width: 180px"
          />
        </el-form-item> -->
        <el-form-item label="项目状态:">
          <el-select
            v-model="searchForm.status"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option label="草稿" value="draft" />
            <el-option label="已申报" value="submitted" />
            <el-option label="已审批" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="入驻" value="settled" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目类型:">
          <el-select
            v-model="searchForm.projectType"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option label="科技创新" value="科技创新" />
            <el-option label="社会实践" value="社会实践" />
            <el-option label="创业项目" value="创业项目" />
            <el-option label="学术研究" value="学术研究" />
            <el-option label="信息技术服务" value="信息技术服务" />
            <el-option label="制造业" value="制造业" />
            <el-option label="社会服务" value="社会服务" />
            <el-option label="文化创意服务" value="文化创意服务" />
            <el-option label="现代农业" value="现代农业" />
          </el-select>
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
        <!-- 用 min-width 让表格自适应撑满（避免无操作列时右侧留白） -->
        <el-table-column prop="projectName" label="项目名称" min-width="260" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="projectType" label="项目类型" width="180" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="getProjectTypeTagType(row.projectType)" size="small">
              {{ row.projectType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantCollege" label="项目所属学院" width="210" align="center" header-align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.applicantCollege || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申报人" width="160" align="center" header-align="center" />
        <el-table-column prop="submitTime" label="项目填报时间" width="206" align="center" header-align="center">
          <template #default="{ row }">
            {{ formatDate(row.submitTime) }}
          </template>
        </el-table-column>
        <!-- 项目空间列已注释 -->
        <!-- <el-table-column prop="projectSpace" label="项目空间" width="120" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.projectSpace || '-' }}
          </template>
        </el-table-column> -->
        <el-table-column prop="status" label="项目状态" width="180" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <!-- 项目负责人不显示操作列 -->
        <el-table-column v-if="!isProjectLeader" label="操作" width="240" fixed="right" align="center" header-align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleDetail(row)">详情</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
  type Project,
  type ProjectQueryParams,
  type PageResult
} from '@/api/project'

const router = useRouter()
const loginStore = useLoginStore()

// 判断是否是项目负责人
const isProjectLeader = computed(() => {
  return loginStore.userInfo?.role === '项目负责人'
})

// 搜索表单
const searchForm = reactive({
  projectName: '',
  // projectSpace: '', // 项目空间功能已注释
  status: '',
  projectType: ''
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

// 获取项目列表
const fetchProjectList = async () => {
  loading.value = true
  try {
    const params: ProjectQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      projectName: searchForm.projectName || undefined,
      // projectSpace: searchForm.projectSpace || undefined, // 项目空间功能已注释
      status: searchForm.status || undefined,
      projectType: searchForm.projectType || undefined
    }
    
    const response = await getProjectList(params) as unknown as PageResult<Project>
    projectList.value = response?.list ?? []
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
  // searchForm.projectSpace = '' // 项目空间功能已注释
  searchForm.status = ''
  searchForm.projectType = ''
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
      return 'info' // 灰色（已申报）
    case 'approved':
      return 'success' // 绿色（已审批）
    case 'rejected':
      return 'danger' // 红色（已拒绝）
    case 'settled':
      return 'success' // 绿色（入驻）
    case 'draft':
      return '' // 默认（草稿）
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
      return '已审批'
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
      return 'success' // 绿色
    case '制造业':
      return 'info' // 灰色
    case '社会服务':
      return 'danger' // 红色
    case '文化创意服务':
      return 'warning' // 橙色
    case '现代农业':
      return 'primary' // 蓝色
    default:
      return ''
  }
}

// 查看详情
const handleDetail = (project: Project) => {
  router.push(`/project-detail/${project.id}`)
}

// 删除项目
const handleDelete = async (project: Project) => {
  if (!project.id) return
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 “${project.projectName}” 吗？删除后不可恢复。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteProject(project.id)
    ElMessage.success('删除成功')
    // 若删除的是当前页最后一条，尽量回到上一页
    if (projectList.value.length <= 1 && pagination.page > 1) {
      pagination.page -= 1
    }
    fetchProjectList()
  } catch (error: any) {
    // 用户取消时 element-plus 会抛出 cancel，不提示错误
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchProjectList()
})
</script>

<style scoped>
.project-query-container {
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

/* 表格默认撑满容器，窄屏时仍可横向滚动 */
.table-wrapper :deep(.el-table) {
  width: 100% !important;
  min-width: 1220px;
}

/* 确保表格列不会被压缩 */
.table-wrapper :deep(.el-table__header-wrapper),
.table-wrapper :deep(.el-table__body-wrapper) {
  width: auto !important;
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
