<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    :width="dialogWidth"
    top="2vh"
    destroy-on-close
    class="word-preview-dialog"
    @update:model-value="emit('update:modelValue', $event)"
    @opened="handleOpened"
    @closed="handleClosed"
  >
    <div v-if="!errorMsg && totalPages > 0" class="preview-toolbar">
      <el-button size="small" :disabled="currentPage <= 1" @click="scrollToPage(currentPage - 1)">
        上一页
      </el-button>
      <span class="page-indicator">
        第 {{ currentPage }} / {{ totalPages }} 页
        <template v-if="currentWordPage">（页码 {{ currentWordPage }}）</template>
      </span>
      <el-button size="small" :disabled="currentPage >= totalPages" @click="scrollToPage(currentPage + 1)">
        下一页
      </el-button>
      <el-input-number
        v-model="jumpPage"
        size="small"
        :min="1"
        :max="totalPages"
        controls-position="right"
        class="jump-input"
      />
      <el-button size="small" type="primary" plain @click="scrollToPage(jumpPage)">跳转</el-button>
      <span v-if="paperInfo" class="paper-info">{{ paperInfo }}</span>
    </div>

    <div
      v-loading="loading"
      ref="wrapRef"
      class="word-preview-wrap"
      @scroll="handleScroll"
    >
      <div v-if="errorMsg" class="word-preview-error">
        <el-empty :description="errorMsg" />
      </div>
      <div v-show="!errorMsg" ref="containerRef" class="docx-container"></div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { renderSync } from 'docx-preview-sync'
import request from '@/utils/request'
import {
  areaHasPageField,
  parseDocPageLayout,
  parseDocPageNumberSettings,
  replacePageNumberInArea,
  type DocPageLayout,
  type DocPageNumberSettings
} from '@/utils/docxPageNumber'

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
const containerRef = ref<HTMLElement | null>(null)
const wrapRef = ref<HTMLElement | null>(null)
const totalPages = ref(0)
const currentPage = ref(1)
const jumpPage = ref(1)
const pageEls = ref<HTMLElement[]>([])
/** 物理页索引 -> Word 页码数字，无页码为 null */
const wordPageNums = ref<Array<number | null>>([])
const pageLayout = ref<DocPageLayout | null>(null)

const dialogTitle = computed(() =>
  props.fileName ? `计划书预览 - ${props.fileName}` : '计划书预览'
)

const currentWordPage = computed(() => {
  const n = wordPageNums.value[currentPage.value - 1]
  return n == null ? '' : String(n)
})

const paperInfo = computed(() => {
  if (!pageLayout.value) return ''
  const p = pageLayout.value
  return `纸张参照：${p.paperLabel}（${p.widthMm}×${p.heightMm}mm）`
})

/** 对话框宽度：纸张宽度 + 左右留白，最大不超过视口 */
const dialogWidth = computed(() => {
  const pageW = pageLayout.value?.widthPx || 794
  const prefer = pageW + 120
  if (typeof window === 'undefined') return `${prefer}px`
  return `${Math.min(prefer, Math.max(720, window.innerWidth - 48))}px`
})

const isDocx = (name?: string) => !!name && name.toLowerCase().endsWith('.docx')
const isLegacyDoc = (name?: string) =>
  !!name && name.toLowerCase().endsWith('.doc') && !name.toLowerCase().endsWith('.docx')

const fetchPlanBlob = async (): Promise<Blob> => {
  if (props.planId) {
    return request.get(`/projects/plan/${props.planId}/file`, {
      responseType: 'blob'
    }) as unknown as Blob
  }
  if (props.projectId) {
    return request.get(`/projects/${props.projectId}/plan-file`, {
      responseType: 'blob'
    }) as unknown as Blob
  }
  throw new Error('缺少计划书文件信息')
}

/** 按 Word 纸张尺寸套页面外框（A4 等），模拟打印版式 */
const applyWordPageFrame = (pages: HTMLElement[], layout: DocPageLayout) => {
  pages.forEach((page, index) => {
    // 外层包裹：放页码标签，避免被页面 overflow 裁切
    let wrap = page.parentElement
    if (!wrap || !wrap.classList.contains('word-paper-wrap')) {
      wrap = document.createElement('div')
      wrap.className = 'word-paper-wrap'
      page.parentNode?.insertBefore(wrap, page)
      wrap.appendChild(page)
    }

    page.classList.add('word-paper-page')
    page.style.width = `${layout.widthPx}px`
    page.style.minWidth = `${layout.widthPx}px`
    page.style.maxWidth = `${layout.widthPx}px`
    page.style.minHeight = `${layout.heightPx}px`
    page.style.height = `${layout.heightPx}px`
    page.style.boxSizing = 'border-box'
    page.style.background = '#fff'
    page.style.position = 'relative'
    page.style.overflow = 'hidden'

    // 页边距：库已写入时不强制覆盖，仅在明显缺失时补齐
    const cs = getComputedStyle(page)
    const padTop = parseFloat(cs.paddingTop || '0')
    if (!padTop || padTop < 8) {
      page.style.paddingTop = `${layout.margin.top}px`
      page.style.paddingRight = `${layout.margin.right}px`
      page.style.paddingBottom = `${layout.margin.bottom}px`
      page.style.paddingLeft = `${layout.margin.left}px`
    }

    let tag = wrap.querySelector('.word-page-tag') as HTMLElement | null
    if (!tag) {
      tag = document.createElement('div')
      tag.className = 'word-page-tag'
      wrap.appendChild(tag)
    }
    tag.textContent = `第 ${index + 1} 页 · ${layout.paperLabel}`
  })
}

/**
 * 按 Word 规则写入页码：
 * - 页码常在「页眉」：本科毕业论文(设计) 第 X 页
 * - 正文节 pgNumType start=1，封面/目录节通常没有 PAGE 域
 * - 只给带 PAGE 域的页眉/页脚递增页码，才能和 Word 一致
 */
const applyWordLikePageNumbers = (
  root: HTMLElement,
  settings: DocPageNumberSettings,
  layout: DocPageLayout
) => {
  const pages = Array.from(root.querySelectorAll('section.docx')) as HTMLElement[]
  pageEls.value = pages
  totalPages.value = pages.length
  currentPage.value = pages.length > 0 ? 1 : 0
  jumpPage.value = currentPage.value
  wordPageNums.value = pages.map(() => null)

  if (!pages.length) return

  applyWordPageFrame(pages, layout)

  // 哪些物理页带有页码域
  const numberedIndexes: number[] = []
  pages.forEach((page, index) => {
    page.querySelectorAll('.preview-page-badge, .preview-word-page-no').forEach((el) => el.remove())

    const header = page.querySelector('header') as HTMLElement | null
    const footer = page.querySelector('footer') as HTMLElement | null
    if ((header && areaHasPageField(header)) || (footer && areaHasPageField(footer))) {
      numberedIndexes.push(index)
    }
  })

  // 若一页都没识别到（异常文档），则从最后一节起对所有页编号
  const targets = numberedIndexes.length > 0 ? numberedIndexes : pages.map((_, i) => i)
  const totalNum = targets.length
  const start = settings.bodyStart > 0 ? settings.bodyStart : 1

  targets.forEach((pageIndex, seq) => {
    const pageNum = start + seq
    wordPageNums.value[pageIndex] = pageNum
    const page = pages[pageIndex]
    const header = page.querySelector('header') as HTMLElement | null
    const footer = page.querySelector('footer') as HTMLElement | null

    if (header && (areaHasPageField(header) || numberedIndexes.length === 0)) {
      replacePageNumberInArea(header, pageNum, settings.format, settings.style, totalNum)
    }
    if (footer && (areaHasPageField(footer) || numberedIndexes.length === 0)) {
      replacePageNumberInArea(footer, pageNum, settings.format, settings.style, totalNum)
    }

    // 页眉页脚都没有可写位置时，补一个底部居中页码（尽量贴近 Word）
    if (
      !(header && areaHasPageField(header)) &&
      !(footer && areaHasPageField(footer)) &&
      numberedIndexes.length === 0
    ) {
      let bar = page.querySelector('.preview-fallback-page') as HTMLElement | null
      if (!bar) {
        bar = document.createElement('div')
        bar.className = 'preview-fallback-page'
        page.appendChild(bar)
      }
      bar.textContent =
        settings.style === 'cnPage' ? `第${pageNum}页` : String(pageNum)
    }
  })

  // 目录页码：按标题所在物理页对应的 Word 页码回填
  const idToPage = new Map<string, string>()
  const titleToPage = new Map<string, string>()
  pages.forEach((page, index) => {
    const num = wordPageNums.value[index]
    if (num == null) return
    const text = String(num)
    page.querySelectorAll<HTMLElement>('[id]').forEach((el) => {
      const id = el.getAttribute('id')
      if (id) idToPage.set(id, text)
    })
    page.querySelectorAll<HTMLElement>('h1,h2,h3,h4,h5,h6').forEach((el) => {
      const t = (el.textContent || '').replace(/\s+/g, ' ').trim()
      if (t) titleToPage.set(t, text)
    })
  })

  root.querySelectorAll<HTMLAnchorElement>('a[href^="#"]').forEach((anchor) => {
    const href = anchor.getAttribute('href') || ''
    const targetId = decodeURIComponent(href.replace(/^#/, ''))
    if (!targetId) return
    const entry =
      (anchor.closest('p, div, li') as HTMLElement | null) ||
      (anchor.parentElement as HTMLElement | null)
    if (!entry) return

    entry.classList.add('preview-toc-entry')
    if (!entry.querySelector('.preview-toc-dots')) {
      const dots = document.createElement('span')
      dots.className = 'preview-toc-dots'
      dots.setAttribute('aria-hidden', 'true')
      anchor.insertAdjacentElement('afterend', dots)
    }

    let pageSpan = entry.querySelector('.preview-toc-page') as HTMLElement | null
    if (!pageSpan) {
      const nodes = Array.from(entry.childNodes)
      for (let i = nodes.length - 1; i >= 0; i--) {
        const child = nodes[i]
        if (child.nodeType === Node.TEXT_NODE) {
          const t = (child.textContent || '').trim()
          if (/^\d+$/.test(t) || t === '') entry.removeChild(child)
        }
      }
      pageSpan = document.createElement('span')
      pageSpan.className = 'preview-toc-page'
      entry.appendChild(pageSpan)
    }

    const title = (anchor.textContent || '').replace(/\s+/g, ' ').trim()
    pageSpan.textContent = idToPage.get(targetId) || titleToPage.get(title) || '—'

    anchor.onclick = (e) => {
      e.preventDefault()
      const target = root.querySelector(`#${CSS.escape(targetId)}`) as HTMLElement | null
      if (!target) return
      const pageIndex = pages.findIndex((p) => p.contains(target))
      if (pageIndex >= 0) scrollToPage(pageIndex + 1)
      else target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  })
}

/** 去掉预览中偏灰的字体颜色，统一为黑色 */
const normalizePreviewTextColor = (root: HTMLElement) => {
  const nodes = root.querySelectorAll<HTMLElement>('section.docx, section.docx *')
  nodes.forEach((el) => {
    const style = el.style
    if (!style) return
    // 清除内联灰色/半透明色
    if (style.color) {
      style.color = '#000000'
    }
    if (style.webkitTextFillColor) {
      style.webkitTextFillColor = '#000000'
    }
    if (style.opacity && Number(style.opacity) < 1) {
      style.opacity = '1'
    }
  })
}

const scrollToPage = (page: number) => {
  if (!wrapRef.value || pageEls.value.length === 0) return
  const target = Math.min(Math.max(1, Number(page) || 1), pageEls.value.length)
  const el = pageEls.value[target - 1]
  if (!el) return
  const wrapTop = wrapRef.value.getBoundingClientRect().top
  const elTop = el.getBoundingClientRect().top
  wrapRef.value.scrollTo({
    top: wrapRef.value.scrollTop + (elTop - wrapTop) - 12,
    behavior: 'smooth'
  })
  currentPage.value = target
  jumpPage.value = target
}

const handleScroll = () => {
  if (!wrapRef.value || pageEls.value.length === 0) return
  const wrapRect = wrapRef.value.getBoundingClientRect()
  const mid = wrapRect.top + wrapRect.height / 3
  let best = 1
  let bestDist = Number.POSITIVE_INFINITY
  pageEls.value.forEach((page, index) => {
    const rect = page.getBoundingClientRect()
    const dist = Math.abs(rect.top - mid)
    if (dist < bestDist) {
      bestDist = dist
      best = index + 1
    }
  })
  currentPage.value = best
  jumpPage.value = best
}

const handleOpened = async () => {
  errorMsg.value = ''
  loading.value = true
  totalPages.value = 0
  currentPage.value = 1
  jumpPage.value = 1
  pageEls.value = []
  wordPageNums.value = []
  await nextTick()

  try {
    if (isLegacyDoc(props.fileName)) {
      errorMsg.value = '旧版 .doc 格式暂不支持在线 Word 预览，请下载后用 Word 打开'
      return
    }
    if (props.fileName && !isDocx(props.fileName)) {
      errorMsg.value = '仅支持预览 .docx 格式的 Word 文档'
      return
    }

    const blob = await fetchPlanBlob()
    if (!(blob instanceof Blob) || blob.size === 0) {
      errorMsg.value = '未能获取计划书文件'
      return
    }

    if (blob.type && blob.type.includes('application/json')) {
      const text = await blob.text()
      try {
        const json = JSON.parse(text)
        errorMsg.value = json.msg || '预览失败'
      } catch {
        errorMsg.value = '预览失败'
      }
      return
    }

    if (!containerRef.value) {
      errorMsg.value = '预览容器未就绪'
      return
    }
    containerRef.value.innerHTML = ''

    const settings = await parseDocPageNumberSettings(blob)
    const layout = await parseDocPageLayout(blob)
    pageLayout.value = layout

    // docx-preview-sync：按版式尽量拆页，比原版更接近 Word
    await renderSync(blob, containerRef.value, undefined, {
      className: 'docx',
      inWrapper: true,
      ignoreWidth: false,
      ignoreHeight: false,
      ignoreFonts: false,
      breakPages: true,
      ignoreLastRenderedPageBreak: false,
      experimental: true,
      useBase64URL: true,
      renderHeaders: true,
      renderFooters: true,
      renderFootnotes: true,
      renderEndnotes: true
    } as any)

    await nextTick()
    // 等待布局稳定后再套纸张尺寸、写页码
    await new Promise((r) => requestAnimationFrame(() => r(null)))
    normalizePreviewTextColor(containerRef.value)
    applyWordLikePageNumbers(containerRef.value, settings, layout)
  } catch (e: any) {
    errorMsg.value = e?.message || 'Word 预览失败，请尝试下载查看'
  } finally {
    loading.value = false
  }
}

const handleClosed = () => {
  errorMsg.value = ''
  totalPages.value = 0
  currentPage.value = 1
  jumpPage.value = 1
  pageEls.value = []
  wordPageNums.value = []
  pageLayout.value = null
  if (containerRef.value) {
    containerRef.value.innerHTML = ''
  }
}
</script>

<style scoped>
.preview-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 10px;
  padding: 8px 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.page-indicator {
  min-width: 140px;
  text-align: center;
  font-size: 13px;
  color: #606266;
  font-weight: 600;
}

.paper-info {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

.jump-input {
  width: 110px;
}

.word-preview-wrap {
  min-height: 360px;
  max-height: 74vh;
  overflow: auto;
  /* Word 打印预览灰底 */
  background: #a7a7a7;
  border-radius: 8px;
}

.word-preview-error {
  padding: 48px 16px;
  background: #fff;
}

.docx-container {
  min-height: 320px;
}

.docx-container :deep(.docx-wrapper) {
  background: #a7a7a7;
  padding: 28px 24px 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.docx-container :deep(.word-paper-wrap) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

/* Word 纸张页：白纸 + 阴影 + 固定纸张尺寸 */
.docx-container :deep(.word-paper-page),
.docx-container :deep(.docx-wrapper section.docx) {
  background: #fff !important;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.08), 0 4px 18px rgba(0, 0, 0, 0.22);
  margin: 0 auto !important;
  box-sizing: border-box;
  overflow: hidden !important;
  color: #000 !important;
  -webkit-text-fill-color: #000;
}

.docx-container :deep(.word-page-tag) {
  font-size: 12px;
  color: #f5f5f5;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
  pointer-events: none;
  white-space: nowrap;
}

.docx-container :deep(.docx-wrapper > section.docx),
.docx-container :deep(.docx-wrapper > section.docx p),
.docx-container :deep(.docx-wrapper > section.docx span),
.docx-container :deep(.docx-wrapper > section.docx div),
.docx-container :deep(.docx-wrapper > section.docx li),
.docx-container :deep(.docx-wrapper > section.docx td),
.docx-container :deep(.docx-wrapper > section.docx th),
.docx-container :deep(.docx-wrapper > section.docx h1),
.docx-container :deep(.docx-wrapper > section.docx h2),
.docx-container :deep(.docx-wrapper > section.docx h3),
.docx-container :deep(.docx-wrapper > section.docx h4),
.docx-container :deep(.docx-wrapper > section.docx h5),
.docx-container :deep(.docx-wrapper > section.docx h6),
.docx-container :deep(.docx-wrapper > section.docx header),
.docx-container :deep(.docx-wrapper > section.docx footer),
.docx-container :deep(.docx-wrapper > section.docx a) {
  color: #000 !important;
  -webkit-text-fill-color: #000 !important;
  opacity: 1 !important;
}

.docx-container :deep(header),
.docx-container :deep(footer) {
  display: block;
  width: 100%;
  box-sizing: border-box;
  color: #000 !important;
}

.docx-container :deep(.preview-fallback-page) {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 16px;
  text-align: center;
  color: #000;
  font-size: 14px;
  pointer-events: none;
}

.docx-container :deep(.preview-toc-entry) {
  display: flex !important;
  align-items: baseline;
  gap: 6px;
  width: 100%;
  color: #000 !important;
}

.docx-container :deep(.preview-toc-entry > a) {
  flex: 0 1 auto;
  color: #000 !important;
  text-decoration: none;
  max-width: 75%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.docx-container :deep(.preview-toc-entry > a:hover) {
  color: #000 !important;
  text-decoration: underline;
}

.docx-container :deep(.preview-toc-dots) {
  flex: 1 1 auto;
  border-bottom: 1px dotted #333;
  min-width: 24px;
  height: 0.9em;
  transform: translateY(-0.2em);
}

.docx-container :deep(.preview-toc-page) {
  flex: 0 0 auto;
  color: #000 !important;
  font-variant-numeric: tabular-nums;
  min-width: 1.5em;
  text-align: right;
}
</style>
