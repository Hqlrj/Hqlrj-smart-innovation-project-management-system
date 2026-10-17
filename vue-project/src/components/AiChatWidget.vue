<template>
  <button
    v-if="canAccessAiChat"
    ref="btnRef"
    type="button"
    class="ai-float-btn"
    :class="{ active: isOpen, dragging: isDragging }"
    :style="btnStyle"
    :title="isOpen ? '收起助手' : '拖动或点击打开 AI 助手'"
    @pointerdown="onPointerDown"
    @click="onClick"
  >
    <span class="pulse-ring" />
    <span class="character" aria-hidden="true">
      <svg viewBox="0 0 64 64" width="46" height="46">
        <circle cx="32" cy="32" r="30" fill="#4f6ef7" />
        <circle cx="32" cy="32" r="26" fill="#6b86ff" />
        <rect x="18" y="20" width="28" height="22" rx="11" fill="#fff" />
        <circle cx="26" cy="31" r="3.2" fill="#2b3a8f" />
        <circle cx="38" cy="31" r="3.2" fill="#2b3a8f" />
        <path d="M26 38c2.2 2.4 9.8 2.4 12 0" stroke="#5b6cff" stroke-width="2" fill="none" stroke-linecap="round" />
        <rect x="14" y="28" width="5" height="8" rx="2.5" fill="#ffd56a" />
        <rect x="45" y="28" width="5" height="8" rx="2.5" fill="#ffd56a" />
        <circle cx="22" cy="16" r="3" fill="#9ad0ff" />
        <circle cx="22" cy="16" r="1.4" fill="#fff" />
      </svg>
    </span>
  </button>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useLoginStore } from '@/stores/login'

const SIZE = 64
const MARGIN = 8
const STORAGE_KEY = 'ai-mascot-pos'
const DRAG_THRESHOLD = 6

const loginStore = useLoginStore()
const isOpen = ref(false)
const isDragging = ref(false)
const dragged = ref(false)
const btnRef = ref<HTMLButtonElement>()
const pos = ref({ x: 0, y: 0 })

const canAccessAiChat = computed(() => {
  const role = loginStore.userInfo?.role
  const restrictedRoles = ['申请人', '项目申请人', '学生申请人', '项目负责人', '项目成员']
  return !restrictedRoles.includes(role || '')
})

const btnStyle = computed(() => ({
  left: `${pos.value.x}px`,
  top: `${pos.value.y}px`
}))

const clamp = (x: number, y: number) => {
  const maxX = Math.max(MARGIN, window.innerWidth - SIZE - MARGIN)
  const maxY = Math.max(MARGIN, window.innerHeight - SIZE - MARGIN)
  return {
    x: Math.min(Math.max(MARGIN, x), maxX),
    y: Math.min(Math.max(MARGIN, y), maxY)
  }
}

const defaultPos = () =>
  clamp(window.innerWidth - SIZE - 28, window.innerHeight - SIZE - 28)

const emitMove = () => {
  window.dispatchEvent(
    new CustomEvent('aiMascotMove', {
      detail: { x: pos.value.x, y: pos.value.y, size: SIZE }
    })
  )
}

const savePos = () => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(pos.value))
}

const restorePos = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw)
      if (typeof parsed?.x === 'number' && typeof parsed?.y === 'number') {
        pos.value = clamp(parsed.x, parsed.y)
        emitMove()
        return
      }
    }
  } catch {
    // ignore
  }
  pos.value = defaultPos()
  emitMove()
}

const onPointerDown = (e: PointerEvent) => {
  if (e.button !== 0 && e.pointerType === 'mouse') return
  const startX = e.clientX
  const startY = e.clientY
  const originX = pos.value.x
  const originY = pos.value.y
  dragged.value = false
  isDragging.value = true
  btnRef.value?.setPointerCapture(e.pointerId)

  const onMove = (ev: PointerEvent) => {
    const dx = ev.clientX - startX
    const dy = ev.clientY - startY
    if (!dragged.value && Math.hypot(dx, dy) > DRAG_THRESHOLD) {
      dragged.value = true
    }
    pos.value = clamp(originX + dx, originY + dy)
    emitMove()
  }

  const onUp = () => {
    isDragging.value = false
    window.removeEventListener('pointermove', onMove)
    window.removeEventListener('pointerup', onUp)
    window.removeEventListener('pointercancel', onUp)
    if (dragged.value) {
      savePos()
    }
  }

  window.addEventListener('pointermove', onMove)
  window.addEventListener('pointerup', onUp)
  window.addEventListener('pointercancel', onUp)
}

const onClick = () => {
  if (dragged.value) {
    dragged.value = false
    return
  }
  isOpen.value = !isOpen.value
  window.dispatchEvent(new CustomEvent(isOpen.value ? 'openAiChat' : 'closeAiChat'))
  emitMove()
}

const handleClosed = () => {
  isOpen.value = false
}

const handleResize = () => {
  pos.value = clamp(pos.value.x, pos.value.y)
  emitMove()
}

onMounted(() => {
  restorePos()
  window.addEventListener('closeAiChat', handleClosed)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('closeAiChat', handleClosed)
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.ai-float-btn {
  position: fixed;
  z-index: 3000;
  width: 64px;
  height: 64px;
  border: 0;
  padding: 0;
  border-radius: 50%;
  background: transparent;
  cursor: grab;
  touch-action: none;
  user-select: none;
  box-shadow: 0 10px 24px rgba(79, 110, 247, 0.35);
}

.ai-float-btn:hover:not(.dragging) {
  box-shadow: 0 14px 28px rgba(79, 110, 247, 0.42);
}

.ai-float-btn.dragging {
  cursor: grabbing;
  box-shadow: 0 16px 32px rgba(79, 110, 247, 0.48);
}

.ai-float-btn.active {
  transform: scale(0.96);
}

.character {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.pulse-ring {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  border: 2px solid rgba(107, 134, 255, 0.45);
  animation: pulse 2.2s ease-out infinite;
  pointer-events: none;
}

.ai-float-btn.dragging .pulse-ring {
  animation: none;
  opacity: 0;
}

@keyframes pulse {
  0% {
    transform: scale(0.92);
    opacity: 0.7;
  }
  100% {
    transform: scale(1.25);
    opacity: 0;
  }
}
</style>
