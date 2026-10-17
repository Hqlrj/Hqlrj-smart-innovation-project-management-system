<template>
  <Teleport to="body">
    <transition name="ai-panel">
      <aside
        v-if="isVisible"
        class="ai-panel"
        role="dialog"
        aria-label="AI 智能助手"
        :style="panelStyle"
      >
        <header class="panel-header">
          <div class="brand">
            <div class="brand-avatar">
              <svg viewBox="0 0 64 64" width="28" height="28">
                <circle cx="32" cy="32" r="30" fill="#4f6ef7" />
                <rect x="18" y="20" width="28" height="22" rx="11" fill="#fff" />
                <circle cx="26" cy="31" r="3" fill="#2b3a8f" />
                <circle cx="38" cy="31" r="3" fill="#2b3a8f" />
                <path d="M26 38c2.2 2.4 9.8 2.4 12 0" stroke="#5b6cff" stroke-width="2" fill="none" stroke-linecap="round" />
              </svg>
            </div>
            <div>
              <h3>AI 智能助手</h3>
              <p>{{ hasApiKey ? 'DeepSeek · 在线' : '待配置 API Key' }}</p>
            </div>
          </div>
          <div class="header-actions">
            <button type="button" class="icon-btn" title="新对话" @click="resetConversation">
              <el-icon><RefreshRight /></el-icon>
            </button>
            <button type="button" class="icon-btn" title="关闭" @click="handleClose">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </header>

        <div class="chat-body" ref="messagesContainer">
          <div v-if="showWelcome" class="welcome">
            <div class="welcome-avatar">
              <svg viewBox="0 0 64 64" width="56" height="56">
                <circle cx="32" cy="32" r="30" fill="#4f6ef7" />
                <rect x="18" y="20" width="28" height="22" rx="11" fill="#fff" />
                <circle cx="26" cy="31" r="3.2" fill="#2b3a8f" />
                <circle cx="38" cy="31" r="3.2" fill="#2b3a8f" />
                <path d="M26 38c2.2 2.4 9.8 2.4 12 0" stroke="#5b6cff" stroke-width="2" fill="none" stroke-linecap="round" />
              </svg>
            </div>
            <h2>你好，我是项目智能助手</h2>
            <p>可以帮你解答申报、评审、计划书撰写等问题。</p>
            <div class="suggest-list">
              <button
                v-for="item in suggestions"
                :key="item"
                type="button"
                class="suggest-chip"
                @click="useSuggestion(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>

          <div
            v-for="(message, index) in displayMessages"
            :key="index"
            class="row"
            :class="message.role"
          >
            <div v-if="message.role === 'assistant'" class="row-avatar">
              <svg viewBox="0 0 64 64" width="28" height="28">
                <circle cx="32" cy="32" r="30" fill="#4f6ef7" />
                <rect x="18" y="20" width="28" height="22" rx="11" fill="#fff" />
                <circle cx="26" cy="31" r="3" fill="#2b3a8f" />
                <circle cx="38" cy="31" r="3" fill="#2b3a8f" />
              </svg>
            </div>
            <div
              class="bubble"
              :class="{ streaming: isLoading && index === messages.length - 1 && message.role === 'assistant' }"
            >
              <div v-if="!message.content && isLoading && index === messages.length - 1" class="thinking">
                <span /><span /><span />
              </div>
              <template v-else>{{ message.content }}</template>
            </div>
          </div>
        </div>

        <footer class="composer">
          <div v-if="!hasApiKey" class="key-tip">
            请先点击右上角设置图标配置 DeepSeek API Key
          </div>
          <div class="composer-box">
            <textarea
              v-model="inputMessage"
              rows="1"
              :placeholder="hasApiKey ? '输入问题，Enter 发送，Shift+Enter 换行' : '请先配置 API Key'"
              :disabled="isLoading || !hasApiKey"
              @keydown="handleKeydown"
              @input="autoResize"
              ref="textareaRef"
            />
            <button
              type="button"
              class="send-btn"
              :disabled="isLoading || !inputMessage.trim() || !hasApiKey"
              @click="sendMessage"
            >
              发送
            </button>
          </div>
        </footer>
      </aside>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, RefreshRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Message {
  role: 'user' | 'assistant'
  content: string
  time: Date
}

const suggestions = [
  '创新创业项目怎么申报？',
  '计划书一般包含哪些章节？',
  '评审流程是怎样的？'
]

const isVisible = ref(false)
const inputMessage = ref('')
const isLoading = ref(false)
const conversationId = ref('')
const hasApiKey = ref(false)
const messagesContainer = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const messages = ref<Message[]>([])
const mascot = ref({ x: 0, y: 0, size: 64 })

const panelStyle = computed(() => {
  const margin = 8
  const gap = 12
  const vw = window.innerWidth
  const vh = window.innerHeight
  const pw = Math.min(400, vw - margin * 2)
  const ph = Math.min(680, vh - margin * 2)
  const { x, y, size } = mascot.value

  let left = x + size - pw
  let top = y - gap - ph
  if (top < margin) {
    top = y + size + gap
  }
  if (top + ph > vh - margin) {
    top = Math.max(margin, vh - ph - margin)
  }
  left = Math.min(Math.max(margin, left), vw - pw - margin)
  return {
    left: `${left}px`,
    top: `${top}px`,
    width: `${pw}px`,
    height: `${ph}px`
  }
})

const displayMessages = computed(() => messages.value)
const showWelcome = computed(() => messages.value.length === 0 && !isLoading.value)

const handleOpenChat = async () => {
  isVisible.value = true
  try {
    const response = await request.get('/users/api-key')
    hasApiKey.value = response.hasKey === 'true' || response.hasKey === true
  } catch {
    hasApiKey.value = false
  }
  await nextTick()
  scrollToBottom()
  textareaRef.value?.focus()
}

const hidePanel = () => {
  isVisible.value = false
}

const handleClose = () => {
  hidePanel()
  window.dispatchEvent(new CustomEvent('closeAiChat'))
}

const resetConversation = () => {
  messages.value = []
  conversationId.value = ''
  inputMessage.value = ''
}

const useSuggestion = (text: string) => {
  inputMessage.value = text
  sendMessage()
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 120)}px`
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return
  if (!hasApiKey.value) {
    ElMessage.warning('请先点击顶部设置图标配置您的 DeepSeek API Key')
    return
  }

  messages.value.push({ role: 'user', content: message, time: new Date() })
  messages.value.push({ role: 'assistant', content: '', time: new Date() })
  inputMessage.value = ''
  await nextTick()
  if (textareaRef.value) textareaRef.value.style.height = 'auto'
  isLoading.value = true
  scrollToBottom()

  const assistantIndex = messages.value.length - 1
  const appendDelta = (text: string) => {
    const current = messages.value[assistantIndex]
    if (!current) return
    messages.value[assistantIndex] = {
      ...current,
      content: current.content + text
    }
    scrollToBottom()
  }

  try {
    const token = sessionStorage.getItem('token') || ''
    const baseURL = (request.defaults.baseURL || 'http://localhost:8080').replace(/\/$/, '')
    const response = await fetch(`${baseURL}/projects/ai-chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      body: JSON.stringify({
        message,
        conversationId: conversationId.value
      })
    })

    if (!response.ok) {
      throw new Error(response.status === 401 ? '登录已过期，请重新登录' : `请求失败(${response.status})`)
    }
    if (!response.body) {
      throw new Error('浏览器不支持流式读取')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let sawContent = false

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''
      for (const part of parts) {
        const line = part.split('\n').find((item) => item.startsWith('data:'))
        if (!line) continue
        const payload = line.slice(5).trim()
        if (!payload) continue
        let event: { type?: string; content?: string; conversationId?: string; message?: string }
        try {
          event = JSON.parse(payload)
        } catch {
          continue
        }
        if (event.type === 'delta' && event.content) {
          sawContent = true
          appendDelta(event.content)
        } else if (event.type === 'done') {
          if (event.conversationId) {
            conversationId.value = event.conversationId
          }
        } else if (event.type === 'error') {
          throw new Error(event.message || 'AI聊天失败')
        }
      }
    }

    if (!sawContent && !messages.value[assistantIndex]?.content) {
      appendDelta('暂时没有生成回复，请再试一次。')
    }
  } catch (error: any) {
    let errorMessage = '抱歉，我暂时无法回答，请稍后再试。'
    const raw = error?.message || ''
    if (raw.includes('API Key') || raw.includes('配置')) {
      errorMessage = '请先点击右上角设置图标配置 DeepSeek API Key。'
    } else if (raw) {
      errorMessage = raw
    }
    const current = messages.value[assistantIndex]
    if (current && !current.content) {
      current.content = errorMessage
    } else {
      messages.value.push({ role: 'assistant', content: errorMessage, time: new Date() })
    }
  } finally {
    isLoading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const handleMascotMove = (e: Event) => {
  const detail = (e as CustomEvent).detail
  if (!detail) return
  mascot.value = {
    x: Number(detail.x) || 0,
    y: Number(detail.y) || 0,
    size: Number(detail.size) || 64
  }
}

onMounted(() => {
  window.addEventListener('openAiChat', handleOpenChat)
  window.addEventListener('closeAiChat', hidePanel)
  window.addEventListener('aiMascotMove', handleMascotMove)
})

onUnmounted(() => {
  window.removeEventListener('openAiChat', handleOpenChat)
  window.removeEventListener('closeAiChat', hidePanel)
  window.removeEventListener('aiMascotMove', handleMascotMove)
})
</script>

<style scoped>
.ai-panel {
  position: fixed;
  z-index: 2990;
  display: flex;
  flex-direction: column;
  background: #fff;
  border: 1px solid #e7eaf0;
  border-radius: 20px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #eef1f6;
  background: #fbfcfe;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
}

.brand h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 650;
  color: #1f2937;
}

.brand p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 6px;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
}

.icon-btn:hover {
  background: #eef2ff;
  color: #4f6ef7;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 18px 16px 12px;
  background: #fff;
}

.welcome {
  text-align: center;
  padding: 36px 12px 12px;
}

.welcome-avatar {
  margin-bottom: 12px;
}

.welcome h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.welcome p {
  margin: 8px 0 18px;
  font-size: 13px;
  color: #6b7280;
}

.suggest-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggest-chip {
  border: 1px solid #e5e7eb;
  background: #f8fafc;
  color: #374151;
  border-radius: 12px;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  font-size: 13px;
}

.suggest-chip:hover {
  border-color: #c7d2fe;
  background: #eef2ff;
  color: #4338ca;
}

.row {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.row.user {
  justify-content: flex-end;
}

.row-avatar {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  margin-top: 2px;
}

.bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.row.user .bubble {
  background: #4f6ef7;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.row.assistant .bubble {
  background: #f3f4f6;
  color: #111827;
  border-bottom-left-radius: 4px;
}

.row.assistant .bubble.streaming::after {
  content: '▍';
  margin-left: 2px;
  color: #4f6ef7;
  animation: caret 0.9s steps(1) infinite;
}

@keyframes caret {
  50% {
    opacity: 0;
  }
}

.thinking {
  display: flex;
  align-items: center;
  gap: 5px;
  min-width: 52px;
}

.thinking span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9ca3af;
  animation: blink 1.2s infinite ease-in-out;
}

.thinking span:nth-child(2) {
  animation-delay: 0.15s;
}

.thinking span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes blink {
  0%,
  80%,
  100% {
    opacity: 0.35;
  }
  40% {
    opacity: 1;
  }
}

.composer {
  padding: 10px 12px 14px;
  border-top: 1px solid #eef1f6;
  background: #fff;
}

.key-tip {
  font-size: 12px;
  color: #ef4444;
  margin-bottom: 8px;
  padding: 0 4px;
}

.composer-box {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 8px 8px 8px 12px;
  background: #f8fafc;
}

.composer-box:focus-within {
  border-color: #4f6ef7;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.12);
}

.composer-box textarea {
  flex: 1;
  border: 0;
  outline: none;
  resize: none;
  background: transparent;
  font-size: 14px;
  line-height: 1.5;
  max-height: 120px;
  color: #111827;
}

.send-btn {
  height: 34px;
  padding: 0 14px;
  border: 0;
  border-radius: 10px;
  background: #4f6ef7;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.chat-body::-webkit-scrollbar {
  width: 6px;
}

.chat-body::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 6px;
}

.ai-panel-enter-active,
.ai-panel-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.ai-panel-enter-from,
.ai-panel-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

</style>
