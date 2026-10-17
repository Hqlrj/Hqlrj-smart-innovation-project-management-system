<template>
  <div class="notice-page">
    <header class="toolbar">
      <div>
        <h2>公告中心</h2>
        <p>默认只看待查看公告；点开阅读后会从左侧消失，避免置顶占满列表</p>
      </div>
      <div class="toolbar-actions">
        <el-radio-group v-model="query.readStatus" @change="handleTabChange">
          <el-radio-button value="unread">待查看</el-radio-button>
          <el-radio-button value="read">已查看</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="query.title"
          placeholder="搜索公告标题"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleReset"
        />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button v-if="canPublish" type="primary" plain @click="goPublish">发布公告</el-button>
      </div>
    </header>

    <div v-loading="loading" class="notice-shell">
      <aside class="notice-list">
        <div v-if="notices.length === 0" class="empty-list">{{ emptyText }}</div>
        <button
          v-for="item in notices"
          :key="item.id"
          type="button"
          class="notice-item"
          :class="{ active: currentNotice?.id === item.id, pinned: item.isTop === 1 && query.readStatus === 'unread' }"
          @click="openDetail(item)"
        >
          <div class="item-top">
            <el-tag v-if="item.isTop === 1 && query.readStatus === 'unread'" type="danger" size="small" effect="dark">置顶</el-tag>
            <span class="item-title">{{ item.title }}</span>
          </div>
          <div class="item-publisher">
            <span class="avatar">{{ (item.publisherName || '公').slice(0, 1) }}</span>
            <div>
              <strong>发布人：{{ item.publisherName || '未知' }}</strong>
              <p>{{ item.publisherRole || '未知角色' }} · {{ item.createTime }}</p>
            </div>
          </div>
        </button>
      </aside>

      <section class="notice-detail">
        <template v-if="currentNotice">
          <div class="detail-head">
            <div>
              <div class="detail-tags">
                <el-tag v-if="currentNotice.isTop === 1" type="danger" size="small">置顶</el-tag>
                <el-tag size="small" type="info" effect="plain">{{ currentNotice.isRead === 1 ? '已查看' : '待查看' }}</el-tag>
                <el-tag size="small" effect="plain">{{ currentNotice.publisherRole || '公告' }}</el-tag>
              </div>
              <h3>{{ currentNotice.title }}</h3>
              <div class="detail-meta">
                <span>发布人：{{ currentNotice.publisherName || '-' }}</span>
                <span>时间：{{ currentNotice.createTime || '-' }}</span>
              </div>
            </div>
            <el-button
              v-if="canDelete(currentNotice)"
              type="danger"
              plain
              @click="handleDelete(currentNotice)"
            >删除</el-button>
          </div>
          <div class="detail-body" v-html="formatContent(currentNotice.content)" />
        </template>
        <el-empty v-else :description="query.readStatus === 'unread' ? '点左侧待查看公告后，看完会从列表中移除' : '点左侧已查看公告，这里显示完整内容'" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useLoginStore } from '@/stores/login'
import { hasRoutePermission } from '@/utils/permission'
import { getNoticeList, getNoticeById, deleteNotice, type Notice, type NoticeQueryParams, type PageResult } from '@/api/notice'

const router = useRouter()
const route = useRoute()
const loginStore = useLoginStore()
const userId = computed(() => loginStore.userInfo?.id)
const userRole = computed(() => (loginStore.userInfo as any)?.role || '')
const isAdmin = computed(() => userRole.value === '系统管理员' || userRole.value === '项目管理员')
const canPublish = computed(() => hasRoutePermission(userRole.value, 'notice-publish'))

const loading = ref(false)
const notices = ref<Notice[]>([])
const query = ref<NoticeQueryParams>({
  title: '',
  readStatus: 'unread',
  page: 1,
  pageSize: 50
})
const currentNotice = ref<Notice | null>(null)
const emptyText = computed(() =>
  query.value.readStatus === 'unread' ? '暂无待查看公告' : '暂无已查看公告'
)

const fetchList = async (keepCurrent = false) => {
  loading.value = true
  try {
    const res = await getNoticeList(query.value) as unknown as PageResult<Notice>
    notices.value = res?.list || []
    if (
      !keepCurrent &&
      currentNotice.value &&
      !notices.value.some((n) => n.id === currentNotice.value?.id)
    ) {
      currentNotice.value = null
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取公告列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  query.value.page = 1
  currentNotice.value = null
  fetchList()
}

const handleSearch = () => {
  query.value.page = 1
  currentNotice.value = null
  fetchList()
}

const handleReset = () => {
  query.value.title = ''
  query.value.page = 1
  currentNotice.value = null
  fetchList()
}

const goPublish = () => {
  router.push('/notice-publish')
}

const openDetail = async (row: Notice) => {
  try {
    const detail = await getNoticeById(row.id as number)
    currentNotice.value = detail
    if (query.value.readStatus === 'unread') {
      notices.value = notices.value.filter((n) => n.id !== row.id)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取公告详情失败')
  }
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
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteNotice(row.id)
    ElMessage.success('删除成功')
    currentNotice.value = null
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

onMounted(async () => {
  await fetchList()
  const noticeId = Number(route.query.id)
  if (noticeId) {
    await openDetail({ id: noticeId } as Notice)
  }
})

watch(
  () => route.query.id,
  async (id) => {
    const noticeId = Number(id)
    if (noticeId) {
      await openDetail({ id: noticeId } as Notice)
    }
  }
)
</script>

<style scoped>
.notice-page {
  padding: 16px 20px 20px;
  min-height: calc(100vh - 8px);
  background: #f5f7fa;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 14px;
}

.toolbar h2 {
  margin: 0;
  font-size: 20px;
  color: #1f2d3d;
}

.toolbar p {
  margin: 4px 0 0;
  font-size: 13px;
  color: #909399;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.search-input {
  width: 240px;
}

.notice-shell {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 14px;
  min-height: calc(100vh - 96px);
}

.notice-list,
.notice-detail {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
}

.notice-list {
  overflow-y: auto;
  padding: 8px;
}

.notice-item {
  display: block;
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
}

.notice-item:hover {
  background: #f8fafc;
}

.notice-item.active {
  border-color: #b3d8ff;
  background: #ecf5ff;
}

.notice-item.pinned {
  background: #fff7f7;
}

.notice-item.pinned.active {
  background: #ecf5ff;
}

.item-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-title {
  font-size: 14px;
  font-weight: 650;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-publisher {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #4f6ef7;
  color: #fff;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-publisher strong {
  display: block;
  font-size: 13px;
  color: #303133;
}

.item-publisher p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #909399;
}

.empty-list {
  padding: 48px 0;
  text-align: center;
  color: #909399;
}

.notice-detail {
  padding: 22px 24px;
  overflow-y: auto;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.detail-head h3 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
  line-height: 1.4;
}

.detail-meta {
  margin-top: 10px;
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.detail-body {
  padding-top: 18px;
  font-size: 15px;
  line-height: 1.9;
  color: #303133;
}

@media (max-width: 960px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .notice-shell {
    grid-template-columns: 1fr;
  }

  .notice-list {
    max-height: 320px;
  }
}
</style>
