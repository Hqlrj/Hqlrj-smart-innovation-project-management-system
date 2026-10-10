<template>
  <div class="notice-publish-container">
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

      <div v-if="isProjectLeader" class="no-permission">
        <el-result
          icon="warning"
          title="无发布权限"
          sub-title="项目负责人不能发布公告，请联系管理员或项目管理员。"
        />
      </div>

      <div v-else class="form-body">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="80px"
          class="notice-form"
        >
          <el-form-item label="标题" prop="title">
            <div class="title-row">
              <el-input
                v-model="formData.title"
                placeholder="请输入公告标题"
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

          <el-form-item label="内容" prop="content">
            <el-input
              v-model="formData.content"
              type="textarea"
              :rows="7"
              placeholder="请输入公告正文内容"
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
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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

// 不设置 trigger，仅在点击「立即发布」调用 validate 时校验
const rules = ref<FormRules>({
  title: [{ required: true, message: '请输入公告标题' }],
  content: [{ required: true, message: '请输入公告内容' }]
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
    handleReset()
  } catch (error: any) {
    if (error?.message && !error.message.includes('validate')) {
      ElMessage.error(error.message || '发布失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  formData.value = {
    title: '',
    content: '',
    isTop: 0
  }
  formRef.value?.clearValidate()
}
</script>

<style scoped>
.notice-publish-container {
  padding: 16px 20px;
  min-height: calc(100vh - 60px);
  background:
    radial-gradient(ellipse 80% 50% at 10% 0%, rgba(64, 158, 255, 0.07), transparent 55%),
    #f5f7fa;
}

.form-card {
  max-width: 780px;
  margin: 0 auto;
  border: 1px solid #ebeef5;
  border-radius: 10px;
}

.form-card :deep(.el-card__body) {
  padding: 18px 24px 16px;
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
  background: linear-gradient(145deg, #ecf5ff 0%, #d9ecff 100%);
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

.notice-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.notice-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.notice-form :deep(.el-input__wrapper),
.notice-form :deep(.el-textarea__inner) {
  border-radius: 8px;
}

.content-textarea :deep(.el-textarea__inner) {
  padding: 10px 12px;
  line-height: 1.6;
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
  transition: border-color 0.2s ease, background 0.2s ease, color 0.2s ease;
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
  padding-top: 4px;
}

.no-permission {
  padding: 24px 0;
}

@media (max-width: 768px) {
  .notice-publish-container {
    padding: 12px;
  }

  .form-card :deep(.el-card__body) {
    padding: 14px 14px 12px;
  }

  .title-row {
    flex-direction: column;
    align-items: stretch;
  }

  .pin-inline {
    justify-content: space-between;
    height: 36px;
  }

  .action-bar {
    flex-direction: column-reverse;
  }

  .action-bar .el-button {
    width: 100%;
  }
}
</style>
