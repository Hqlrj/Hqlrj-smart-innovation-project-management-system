<template>
  <div class="notice-publish-container">
    <div v-if="isProjectLeader" class="no-permission">
      <el-result
        icon="warning"
        title="无发布权限"
        sub-title="项目负责人不能发布公告，请联系管理员或项目管理员。"
      />
    </div>

    <div v-else class="publish-shell">
      <el-card shadow="never" class="form-card">
        <div class="page-header">
          <div class="page-icon">
            <el-icon :size="18"><EditPen /></el-icon>
          </div>
          <div class="page-header-text">
            <h2 class="page-title">发布公告</h2>
            <p class="page-desc">撰写并发布系统公告，可设置置顶展示</p>
          </div>
        </div>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="72px"
          class="notice-form"
        >
          <el-form-item label="标题" prop="title">
            <div class="title-row">
              <el-input
                v-model="formData.title"
                placeholder="请输入公告标题，例如：本学期创新创业项目申报通知"
                maxlength="50"
                show-word-limit
                clearable
              />
              <div class="pin-inline" :class="{ active: formData.isTop === 1 }">
                <span>置顶</span>
                <el-switch
                  v-model="formData.isTop"
                  :active-value="1"
                  :inactive-value="0"
                />
              </div>
            </div>
          </el-form-item>

          <el-form-item label="内容" prop="content" class="content-item">
            <el-input
              v-model="formData.content"
              type="textarea"
              placeholder="请输入公告正文，建议写清对象、时间、地点和注意事项"
              maxlength="2000"
              show-word-limit
              resize="none"
              class="content-textarea"
            />
          </el-form-item>
        </el-form>

        <div class="action-bar">
          <el-button @click="handleReset">重置</el-button>
          <el-button
            type="primary"
            :loading="submitting"
            :icon="Promotion"
            @click="handleSubmit"
          >
            立即发布
          </el-button>
        </div>
      </el-card>

      <aside class="side-panel">
        <el-card shadow="never" class="preview-card">
          <div class="side-title">实时预览</div>
          <div class="preview-box">
            <div class="preview-meta">
              <el-tag v-if="formData.isTop === 1" type="danger" size="small">置顶</el-tag>
              <span class="preview-role">{{ userRole || '当前账号' }}</span>
            </div>
            <h3>{{ formData.title || '公告标题将显示在这里' }}</h3>
            <p class="preview-body">{{ formData.content || '输入正文后，这里会同步显示公告效果。' }}</p>
          </div>
        </el-card>

        <el-card shadow="never" class="tips-card">
          <div class="side-title">发布建议</div>
          <ul class="tips-list">
            <li>标题控制在 15–25 字，方便列表一眼看懂</li>
            <li>正文写清对象、截止时间和办理入口</li>
            <li>重要通知可开启置顶；对方查看后会从待查看列表移除，不再占着置顶位置</li>
            <li>本院发布仅本院可见，系统管理员发布全校可见</li>
          </ul>
        </el-card>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { EditPen, Promotion } from '@element-plus/icons-vue'
import { addNotice, type Notice } from '@/api/notice'
import { useLoginStore } from '@/stores/login'

const loginStore = useLoginStore()
const userRole = computed(() => loginStore.userInfo?.role || '')
const isProjectLeader = computed(() => userRole.value === '项目负责人')

const formRef = ref<FormInstance>()
const formData = ref<Partial<Notice>>({
  title: '',
  content: '',
  isTop: 0
})

const rules = ref<FormRules>({
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }]
})

const submitting = ref(false)

const handleSubmit = async () => {
  if (!formRef.value) return
  if (isProjectLeader.value) {
    ElMessage.error('项目负责人不能发布公告')
    return
  }

  try {
    await formRef.value.validate()
    submitting.value = true
    await addNotice(formData.value)
    ElMessage.success('发布成功')
    await handleReset()
  } catch (error: any) {
    if (error?.message && !error.message.includes('validate')) {
      ElMessage.error(error.message || '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReset = async () => {
  formRef.value?.resetFields()
  formData.value.title = ''
  formData.value.content = ''
  formData.value.isTop = 0
  await nextTick()
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.notice-publish-container {
  padding: 16px 20px;
  min-height: calc(100vh - 8px);
  background: #f5f7fa;
}

.publish-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: stretch;
  min-height: calc(100vh - 40px);
}

.form-card,
.preview-card,
.tips-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
}

.form-card {
  display: flex;
  flex-direction: column;
}

.form-card :deep(.el-card__body) {
  padding: 18px 22px 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #ebeef5;
}

.page-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  background: #ecf5ff;
  flex-shrink: 0;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 650;
  color: #1f2d3d;
}

.page-desc {
  margin: 2px 0 0;
  font-size: 12px;
  color: #909399;
}

.notice-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.content-item :deep(.el-form-item__content) {
  flex: 1;
}

.notice-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.notice-form :deep(.el-input__wrapper),
.notice-form :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.content-textarea {
  height: 100%;
}

.content-textarea :deep(.el-textarea) {
  height: 100%;
}

.content-textarea :deep(.el-textarea__inner) {
  min-height: 320px;
  height: 100%;
  padding: 12px;
  line-height: 1.7;
  font-size: 14px;
  background: #fafbfc;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.title-row .el-input {
  flex: 1;
  min-width: 0;
}

.pin-inline {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  padding: 0 12px;
  height: 32px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafbfc;
  font-size: 13px;
  color: #606266;
}

.pin-inline.active {
  border-color: #b3d8ff;
  background: #ecf5ff;
  color: #409eff;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 8px;
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-title {
  font-size: 14px;
  font-weight: 650;
  color: #303133;
  margin-bottom: 10px;
}

.preview-box {
  min-height: 280px;
  padding: 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px dashed #dcdfe6;
}

.preview-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.preview-box h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #1f2d3d;
  word-break: break-all;
}

.preview-body {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  font-size: 13px;
  line-height: 1.9;
}

.no-permission {
  padding: 48px 0;
}

@media (max-width: 1100px) {
  .publish-shell {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .content-textarea :deep(.el-textarea__inner) {
    min-height: 240px;
  }
}
</style>
