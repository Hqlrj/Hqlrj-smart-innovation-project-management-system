<template>
  <div class="ai-chat-sidebar">
    <!-- 聊天菜单项 -->
    <div 
      class="nav-item ai-chat-item" 
      @click="toggleChat"
      :class="{ 'active': isVisible }"
      v-if="canAccessAiChat"
    >
      <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
      </svg>
      <span>AI智能助手</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useLoginStore } from '@/stores/login'

const loginStore = useLoginStore()

// 检查是否可以访问AI聊天功能（暂时对所有用户开放，后续可根据需要调整权限）
const canAccessAiChat = computed(() => {
  // 暂时对所有用户开放，可以根据需要调整权限
  return true
  // const role = loginStore.userInfo?.role
  // return role === '评委老师' || role === '审批人' || role === '系统管理员' || role === '项目管理员'
})

// 组件状态
const isVisible = ref(false)

// 切换聊天窗口
const toggleChat = () => {
  isVisible.value = !isVisible.value
  // 向父组件发送事件，通知打开聊天窗口
  window.dispatchEvent(new CustomEvent('openAiChat'))
}

// 监听来自父组件的事件
window.addEventListener('closeAiChat', () => {
  isVisible.value = false
})
</script>

<style scoped>
.ai-chat-sidebar {
  /* 侧边栏中的样式 */
}

.ai-chat-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 2px 8px;
  border-radius: 10px;
  color: rgba(71, 85, 105, 0.95);
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s ease, color 0.2s ease, transform 0.08s ease;
}

.ai-chat-item:hover {
  background: rgba(25, 118, 210, 0.08);
  color: #1976d2;
}

.ai-chat-item.active {
  background: linear-gradient(90deg, rgba(25, 118, 210, 0.18) 0%, rgba(25, 118, 210, 0.08) 100%);
  color: #1976d2;
  font-weight: 600;
  position: relative;
}

.ai-chat-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 999px;
  background: #1976d2;
}

.ai-chat-item .nav-icon {
  color: rgba(100, 116, 139, 0.95);
}

.ai-chat-item:hover .nav-icon,
.ai-chat-item.active .nav-icon {
  color: currentColor;
}
</style>