<template>
  <div class="notice-publish-container">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span>发布公告</span>
        </div>
      </template>

      <div v-if="isProjectLeader" class="no-permission">
        <el-result
          icon="warning"
          title="无发布权限"
          sub-title="项目负责人不能发布公告，请联系管理员或项目管理员。"
        />
      </div>

      <div v-else>
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="90px"
          class="notice-form"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入公告标题" />
          </el-form-item>
          <el-form-item label="内容" prop="content">
            <el-input
              v-model="formData.content"
              type="textarea"
              :rows="10"
              placeholder="请输入公告内容"
              resize="vertical"
            />
          </el-form-item>
          <el-form-item label="是否置顶" prop="isTop">
            <el-switch
              v-model="formData.isTop"
              :active-value="1"
              :inactive-value="0"
            />
          </el-form-item>
        </el-form>

        <div class="action-bar">
          <el-button type="primary" :loading="submitting" @click="handleSubmit">发布</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
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
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.form-card {
  max-width: 780px;
  margin: 0 auto;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}

.notice-form {
  margin-top: 10px;
}

.action-bar {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.no-permission {
  padding: 40px 0;
}
</style>
