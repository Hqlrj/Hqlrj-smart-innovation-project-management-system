<template>
  <div class="notice-list-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="query" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="请输入公告标题" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="notices"
        border
        style="width: 100%"
        :empty-text="'暂无公告'"
        @row-dblclick="handleRowDblClick"
      >
        <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
        <!-- 内容列：只显示“查看”按钮，不直接展示内容 -->
        <el-table-column label="内容" width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              text
              size="small"
              :icon="View"
              @click="handleViewClick(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" align="center" />
        <el-table-column prop="publisherRole" label="角色" width="120" align="center" />
        <el-table-column prop="createTime" label="发布时间" width="180" align="center" />
        <el-table-column prop="isTop" label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <!-- 操作列：删除（管理员可删所有，其他只能删自己发布的） -->
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="canDelete(row)"
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 公告详情（美化弹窗） -->
    <el-dialog
      v-model="detailVisible"
      width="720px"
      class="notice-dialog"
      :show-close="false"
      :close-on-click-modal="false"
      destroy-on-close
      center
    >
      <template #header>
        <div class="dialog-header">
          <div class="dialog-title">{{ currentNotice?.title || '公告详情' }}</div>
          <div class="dialog-subtitle">
            <span>发布人：{{ currentNotice?.publisherName || '-' }}</span>
            <span>角色：{{ currentNotice?.publisherRole || '-' }}</span>
            <span>发布时间：{{ currentNotice?.createTime || '-' }}</span>
          </div>
        </div>
      </template>

      <div class="dialog-body">
        <div
          v-if="currentNotice"
          class="notice-content"
          v-html="formatContent(currentNotice.content)"
        ></div>
        <div v-else class="notice-empty">暂无内容</div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="detailVisible = false">我已知晓</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View } from '@element-plus/icons-vue'
import { useLoginStore } from '@/stores/login'
import { getNoticeList, getNoticeById, deleteNotice, type Notice, type NoticeQueryParams, type PageResult } from '@/api/notice'

const loginStore = useLoginStore()
const userId = computed(() => loginStore.userInfo?.id)
const userRole = computed(() => (loginStore.userInfo as any)?.role || '')
const isAdmin = computed(() => userRole.value === '系统管理员' || userRole.value === '项目管理员')

const loading = ref(false)
const notices = ref<Notice[]>([])
const query = ref<NoticeQueryParams>({
  title: '',
  page: 1,
  pageSize: 50
})

const detailVisible = ref(false)
const currentNotice = ref<Notice | null>(null)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getNoticeList(query.value) as unknown as PageResult<Notice>
    notices.value = res?.list || []
  } catch (error: any) {
    ElMessage.error(error.message || '获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.value.page = 1
  fetchList()
}

const handleReset = () => {
  query.value.title = ''
  query.value.page = 1
  fetchList()
}

// 表格行双击 & 按钮点击都复用查看逻辑
const openDetail = async (row: Notice) => {
  try {
    // 重新请求详情，确保内容字段完整
    const detail = await getNoticeById(row.id as number)
    currentNotice.value = detail
    detailVisible.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取公告详情失败')
  }
}

const handleRowDblClick = (row: Notice) => {
  openDetail(row)
}

const handleViewClick = (row: Notice) => {
  openDetail(row)
}

const canDelete = (row: Notice) => {
  if (isAdmin.value) return true
  if (!userId.value) return false
  return row.publisherId === userId.value
}

const handleDelete = async (row: Notice) => {
  if (!row.id) return
  try {
    await ElMessageBox.confirm(
      `确定要删除公告 “${row.title}” 吗？删除后不可恢复。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    // 若当前详情弹窗展示的是被删除公告，关闭弹窗
    if (currentNotice.value?.id === row.id) {
      detailVisible.value = false
      currentNotice.value = null
    }
    fetchList()
  } catch (error: any) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const formatContent = (content: string | undefined) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.notice-list-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  background: white;
  margin-top: 10px;
}

.notice-title {
  margin: 0 0 10px;
  font-size: 18px;
  font-weight: 600;
}

.notice-meta {
  margin-bottom: 16px;
  font-size: 13px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.notice-content {
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.notice-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  padding-bottom: 8px;
}

.dialog-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid #ebeef5;
}

.dialog-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.dialog-subtitle {
  font-size: 12px;
  color: #909399;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.dialog-body {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 4px;
  padding-top: 4px;
}

.notice-empty {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>
