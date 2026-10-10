import JSZip from 'jszip'

export type PageNumFormat = 'decimal' | 'upperRoman' | 'lowerRoman' | 'upperLetter' | 'lowerLetter'

export interface DocPageNumberSettings {
  /** 正文页码起始（来自带 PAGE 域的节） */
  bodyStart: number
  format: PageNumFormat
  /** 页码样式：中文「第X页」等 */
  style: 'cnPage' | 'dashed' | 'plain' | 'pagePrefix' | 'pageOf'
}

/**
 * 从 docx 解析页码设置：优先取带 PAGE 域的节 / 最后一次 pgNumType@start
 */
export async function parseDocPageNumberSettings(blob: Blob): Promise<DocPageNumberSettings> {
  const defaults: DocPageNumberSettings = {
    bodyStart: 1,
    format: 'decimal',
    style: 'cnPage'
  }
  try {
    const zip = await JSZip.loadAsync(blob)
    const xml = await zip.file('word/document.xml')?.async('string')
    if (!xml) return defaults

    const sectBlocks = xml.match(/<w:sectPr[\s\S]*?<\/w:sectPr>/g) || []
    let bodyStart = 1
    let format: PageNumFormat = 'decimal'
    for (const block of sectBlocks) {
      const pg = block.match(/<w:pgNumType([^>]*)\/?>/)
      if (!pg) continue
      const startMatch = pg[1].match(/w:start="(\d+)"/)
      const fmtMatch = pg[1].match(/w:fmt="([^"]+)"/)
      if (startMatch) bodyStart = Number(startMatch[1]) || 1
      if (fmtMatch && isPageNumFormat(fmtMatch[1])) format = fmtMatch[1]
    }

    // 探测页眉/页脚里的页码样式
    let style: DocPageNumberSettings['style'] = 'plain'
    const parts = Object.keys(zip.files).filter((f) => /word\/(header|footer)\d*\.xml$/.test(f))
    for (const p of parts) {
      const t = await zip.file(p)?.async('string')
      if (!t) continue
      const text = t.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ')
      if (/第\s*PAGE|第\s*\d+\s*页/.test(text) || /第\s*PAGE/i.test(t)) {
        style = 'cnPage'
        break
      }
      if (/Page\s+PAGE/i.test(text) || /Page\s+\d+\s+of/i.test(text)) {
        style = /of/i.test(text) ? 'pageOf' : 'pagePrefix'
        break
      }
      if (/[-–—]\s*(PAGE|\d+)\s*[-–—]/.test(text)) {
        style = 'dashed'
        break
      }
      if (/PAGE/i.test(t)) style = 'plain'
    }

    return { bodyStart, format, style }
  } catch {
    return defaults
  }
}

function isPageNumFormat(v: string): v is PageNumFormat {
  return ['decimal', 'upperRoman', 'lowerRoman', 'upperLetter', 'lowerLetter'].includes(v)
}

export function formatPageNumber(n: number, format: PageNumFormat = 'decimal'): string {
  if (n <= 0) return ''
  switch (format) {
    case 'upperRoman':
      return toRoman(n).toUpperCase()
    case 'lowerRoman':
      return toRoman(n).toLowerCase()
    case 'upperLetter':
      return toLetters(n).toUpperCase()
    case 'lowerLetter':
      return toLetters(n).toLowerCase()
    default:
      return String(n)
  }
}

function toRoman(num: number): string {
  const map: [number, string][] = [
    [1000, 'M'], [900, 'CM'], [500, 'D'], [400, 'CD'],
    [100, 'C'], [90, 'XC'], [50, 'L'], [40, 'XL'],
    [10, 'X'], [9, 'IX'], [5, 'V'], [4, 'IV'], [1, 'I']
  ]
  let n = num
  let out = ''
  for (const [v, s] of map) {
    while (n >= v) {
      out += s
      n -= v
    }
  }
  return out
}

function toLetters(num: number): string {
  let n = num
  let out = ''
  while (n > 0) {
    n--
    out = String.fromCharCode(97 + (n % 26)) + out
    n = Math.floor(n / 26)
  }
  return out
}

export function buildPageLabel(
  style: DocPageNumberSettings['style'],
  pageText: string,
  totalText: string
): string {
  switch (style) {
    case 'cnPage':
      return `第${pageText}页`
    case 'pageOf':
      return `Page ${pageText} of ${totalText}`
    case 'pagePrefix':
      return `Page ${pageText}`
    case 'dashed':
      return `- ${pageText} -`
    default:
      return pageText
  }
}

/** 判断页眉/页脚是否携带 Word 页码域（含缓存结果） */
export function areaHasPageField(el: HTMLElement): boolean {
  const text = (el.textContent || '').replace(/\s+/g, '')
  const html = el.innerHTML || ''
  if (/第\d+页/.test(text)) return true
  if (/PAGE/i.test(html)) return true
  // 仅有独立数字且区域很短，也视为页码区
  if (/^\d+$/.test(text) && text.length <= 4) return true
  if (/^[-–—]\d+[-–—]$/.test(text)) return true
  if (/^Page\d+/i.test(text)) return true
  return false
}

/**
 * 把页眉/页脚里的 PAGE 缓存值替换为真实页码。
 * 典型：`本科毕业论文(设计) 第 15 页` → `... 第 3 页`
 */
export function replacePageNumberInArea(
  el: HTMLElement,
  pageNum: number,
  format: PageNumFormat,
  style: DocPageNumberSettings['style'],
  totalNum: number
): void {
  const pageText = formatPageNumber(pageNum, format)
  const totalText = formatPageNumber(totalNum, format)
  const label = buildPageLabel(style, pageText, totalText)

  const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT)
  const nodes: Text[] = []
  let n: Node | null
  while ((n = walker.nextNode())) {
    if ((n.textContent || '').trim()) nodes.push(n as Text)
  }

  // 1) 优先替换「第」「数字」「页」拆开的中间数字节点（最常见）
  for (let i = 0; i < nodes.length; i++) {
    const cur = (nodes[i].textContent || '').trim()
    if (!/^\d+$/.test(cur) && !/^[ivxlcdmIVXLCDM]+$/.test(cur)) continue
    const prev = (nodes[i - 1]?.textContent || '').replace(/\s+/g, '')
    const next = (nodes[i + 1]?.textContent || '').replace(/\s+/g, '')
    if (prev.endsWith('第') || next.startsWith('页') || prev.includes('第') || next.includes('页')) {
      nodes[i].textContent = pageText
      return
    }
    // 页脚里单独的 PAGE 结果
    if (nodes.length <= 3) {
      nodes[i].textContent = pageText
      return
    }
  }

  // 2) 整段匹配替换
  for (const tn of nodes) {
    const raw = tn.textContent || ''
    if (/第\s*\d+\s*页/.test(raw)) {
      tn.textContent = raw.replace(/第\s*\d+\s*页/, label)
      return
    }
    if (/Page\s+\d+\s+of\s+\d+/i.test(raw)) {
      tn.textContent = raw.replace(/Page\s+\d+\s+of\s+\d+/i, label)
      return
    }
    if (/Page\s+\d+/i.test(raw)) {
      tn.textContent = raw.replace(/Page\s+\d+/i, `Page ${pageText}`)
      return
    }
    if (/[-–—]\s*\d+\s*[-–—]/.test(raw)) {
      tn.textContent = raw.replace(/[-–—]\s*\d+\s*[-–—]/, label)
      return
    }
  }

  // 3) 仍找不到：在区域末尾写入（避免空白）
  let slot = el.querySelector('.preview-word-page-no') as HTMLElement | null
  if (!slot) {
    slot = document.createElement('span')
    slot.className = 'preview-word-page-no'
    el.appendChild(document.createTextNode(' '))
    el.appendChild(slot)
  }
  slot.textContent = style === 'cnPage' ? label : pageText
}
