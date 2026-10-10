<template>
  <el-dialog
    v-model="isVisible"
    width="700px"
    :close-on-click-modal="false"
    class="ai-chat-dialog"
    @close="handleClose"
    :show-close="false"
  >
    <template #header>
      <div class="dialog-header">
        <div class="header-left">
          <div class="ai-avatar">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z"/>
              <path d="M12 6v6l4 2"/>
            </svg>
          </div>
          <div class="header-info">
            <h3>AI智能助手</h3>
            <p>随时为您解答项目审核相关问题</p>
          </div>
        </div>
        <el-icon class="close-icon" @click="handleClose">
          <Close />
        </el-icon>
      </div>
    </template>

    <div class="chat-content">
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="message"
          :class="message.role"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="user-avatar">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <div v-else class="ai-avatar-mini">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z"/>
                <path d="M12 6v6l4 2"/>
              </svg>
            </div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-name">{{ message.role === 'user' ? '我' : 'AI助手' }}</span>
              <span class="message-time">{{ formatTime(message.time) }}</span>
            </div>
            <div class="message-text">{{ message.content }}</div>
          </div>
        </div>
        
        <!-- 加载中 -->
        <div class="message assistant loading" v-if="isLoading">
          <div class="message-avatar">
            <div class="ai-avatar-mini">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z"/>
                <path d="M12 6v6l4 2"/>
              </svg>
            </div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-name">AI助手</span>
            </div>
            <div class="message-text">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            :placeholder="hasApiKey ? '输入您的问题...' : '请先配置API Key后再使用'"
            @keydown.enter="sendMessage"
            :disabled="isLoading"
            class="chat-textarea"
          />
          <el-button 
            @click="sendMessage" 
            :disabled="isLoading || !inputMessage.trim()"
            type="primary"
            class="send-btn"
            size="large"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
        <div class="input-footer">
          <span class="input-hint" v-if="hasApiKey">按 Enter 快速发送</span>
          <span class="input-hint warning" v-else>
            <el-icon><Warning /></el-icon>
            请先配置API Key
          </span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, Promotion, Warning } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 组件状态
const isVisible = ref(false)
const inputMessage = ref('')
const isLoading = ref(false)
const conversationId = ref('')
const messagesContainer = ref<HTMLElement>()

// 消息历史
interface Message {
  role: 'user' | 'assistant'
  content: string
  time: Date
}

const messages = ref<Message[]>([
  {
    role: 'assistant',
    content: '您好！我是AI智能助手，可以帮助您处理项目审核相关问题。点击顶部设置图标配置API Key即可开始使用。',
    time: new Date()
  }
])

// 检查用户是否配置了API Key
const hasApiKey = ref(false)

// 监听来自侧边栏的事件
const handleOpenChat = async () => {
  isVisible.value = true
  
  // 检查用户是否配置了API Key
  try {
    const response = await request.get('/users/api-key')
    hasApiKey.value = response.hasKey === 'true'
    
    if (!hasApiKey.value) {
      // 如果没有配置API Key，更新提示消息
      messages.value = [{
        role: 'assistant',
        content: '您好！我是AI智能助手，可以帮助您处理项目审核相关问题。请问您有什么需要帮助的吗？。',
        time: new Date()
      }]
    }
  } catch (error) {
    console.error('检查API Key失败:', error)
    hasApiKey.value = false
  }
  
  nextTick(() => {
    scrollToBottom()
  })
}

// 监听关闭事件
const handleClose = () => {
  isVisible.value = false
}

// 发送消息
const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  // 检查是否配置了API Key
  if (!hasApiKey.value) {
    ElMessage.warning('请先点击顶部设置图标配置您的DeepSeek API Key')
    return
  }

  console.log('开始发送AI聊天消息:', message)
  console.log('当前对话ID:', conversationId.value)

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: message,
    time: new Date()
  })

  inputMessage.value = ''
  isLoading.value = true

  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })

  try {
    console.log('发送请求到: /projects/ai-chat')
    const response = await request.post('/projects/ai-chat', {
      message: message,
      conversationId: conversationId.value
    })
    console.log('AI聊天响应:', response)

    // 更新对话ID
    if (response.conversationId) {
      conversationId.value = response.conversationId
    }

    // 添加AI回复
    messages.value.push({
      role: 'assistant',
      content: response.reply,
      time: new Date()
    })

  } catch (error: any) {
    console.error('AI聊天错误:', error)
    console.error('错误详情:', {
      message: error.message,
      code: error.code,
      status: error.response?.status,
      data: error.response?.data
    })
    
    // 检查错误信息
    let errorMessage = '抱歉，我暂时无法回答您的问题，请稍后再试。'
    
    if (error.message) {
      if (error.message.includes('API Key') || error.message.includes('配置')) {
        errorMessage = '请先点击顶部设置图标配置您的DeepSeek API Key，配置后即可使用AI聊天功能。'
      } else if (error.message.includes('404')) {
        errorMessage = 'AI服务接口不存在，请联系管理员检查后端配置。'
      } else if (error.message.includes('500')) {
        errorMessage = '服务器错误，请稍后再试。'
      }
    }
    
    // 检查是否是404错误
    if (error.response && error.response.status === 404) {
      errorMessage = 'AI服务接口不存在(404)，请联系管理员检查后端配置。'
    }
    
    messages.value.push({
      role: 'assistant',
      content: errorMessage,
      time: new Date()
    })
  } finally {
    isLoading.value = false
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time: Date) => {
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return time.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 组件挂载时监听事件
onMounted(() => {
  window.addEventListener('openAiChat', handleOpenChat)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('openAiChat', handleOpenChat)
})
</script>

<style scoped>
/* 聊天弹窗样式 */
:deep(.ai-chat-dialog .el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

:deep(.ai-chat-dialog .el-dialog__header) {
  padding: 0;
  margin: 0;
  border-bottom: none;
}

:deep(.ai-chat-dialog .el-dialog__body) {
  padding: 0;
  display: flex;
  flex-direction: column;
}

.dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  backdrop-filter: blur(10px);
}

.header-info h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.header-info p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.close-icon {
  color: white;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 8px;
  border-radius: 8px;
}

.close-icon:hover {
  background: rgba(255, 255, 255, 0.1);
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f8f9fa;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar,
.ai-avatar-mini {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.ai-avatar-mini {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.message-content {
  max-width: 70%;
}

.message.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message.assistant .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.message-name {
  font-size: 12px;
  font-weight: 500;
  color: #666;
}

.message-time {
  font-size: 11px;
  color: #999;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
  color: #333;
}

.message.user .message-text {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: white;
  border: 1px solid #e8e8e8;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #667eea;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-8px);
  }
}

.chat-input {
  padding: 20px 24px;
  border-top: 1px solid #f0f0f0;
  background: white;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-textarea {
  flex: 1;
}

.chat-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  resize: none;
  transition: all 0.2s ease;
}

.chat-textarea :deep(.el-textarea__inner):focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.send-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  transition: all 0.3s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input-footer {
  margin-top: 8px;
  text-align: right;
}

.input-hint {
  font-size: 12px;
  color: #999;
}

.input-hint.warning {
  color: #f56c6c;
  display: flex;
  align-items: center;
  gap: 4px;
}

.input-hint.warning :deep(.el-icon) {
  font-size: 14px;
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5568d3 0%, #65428a 100%);
}
</style>