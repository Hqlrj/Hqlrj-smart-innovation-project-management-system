<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="860px"
    top="6vh"
    align-center
    destroy-on-close
    class="word-preview-dialog"
    @update:model-value="emit('update:modelValue', $event)"
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div v-loading="loading" class="pdf-preview-wrap">
      <div v-if="errorMsg" class="word-preview-error">
        <el-empty :description="errorMsg" />
      </div>
      <iframe
        v-else-if="pdfUrl"
        :src="pdfUrl"
        class="pdf-frame"
        title="计划书 PDF 预览"
      />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import request from '@/utils/request'

const props = defineProps<{
  modelValue: boolean
  fileName?: string
  planId?: number | null
  projectId?: number | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const loading = ref(false)
const errorMsg = ref('')
const pdfUrl = ref('')

const dialogTitle = computed(() =>
  props.fileName ? `计划书预览 - ${props.fileName}` : '计划书预览'
)

const fetchPlanPdf = async (): Promise<Blob> => {
  const options = { responseType: 'blob' as const, timeout: 90000 }
  if (props.planId) {
    return request.get(`/projects/plan/${props.planId}/preview-pdf`, options) as unknown as Blob
  }
  if (props.projectId) {
    return request.get(`/projects/${props.projectId}/plan-preview-pdf`, options) as unknown as Blob
  }
  throw new Error('缺少计划书文件信息')
}

const revokePdfUrl = () => {
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value)
    pdfUrl.value = ''
  }
}

const handleOpened = async () => {
  errorMsg.value = ''
  loading.value = true
  revokePdfUrl()

  try {
    const blob = await fetchPlanPdf()
    if (!(blob instanceof Blob) || blob.size === 0) {
      errorMsg.value = '未能获取计划书预览文件'
      return
    }

    const type = (blob.type || '').toLowerCase()
    if (type.includes('application/json') || type.includes('text/plain')) {
      const text = await blob.text()
      try {
        const json = JSON.parse(text)
        errorMsg.value = json.msg || '预览失败'
      } catch {
        errorMsg.value = '预览失败'
      }
      return
    }

    const pdfBlob = type.includes('pdf')
      ? blob
      : new Blob([blob], { type: 'application/pdf' })
    pdfUrl.value = URL.createObjectURL(pdfBlob)
  } catch (e: any) {
    errorMsg.value = e?.message || 'PDF 预览失败，请确认已上传 Word 文件后重试'
  } finally {
    loading.value = false
  }
}

const handleClosed = () => {
  errorMsg.value = ''
  revokePdfUrl()
}
</script>

<style scoped>
.word-preview-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 16px;
}

.pdf-preview-wrap {
  height: 62vh;
  max-height: 680px;
  background: #525659;
  border-radius: 8px;
  overflow: hidden;
}

.word-preview-error {
  padding: 48px 16px;
  background: #fff;
  height: 100%;
}

.pdf-frame {
  width: 100%;
  height: 100%;
  border: 0;
  background: #525659;
}
</style>
