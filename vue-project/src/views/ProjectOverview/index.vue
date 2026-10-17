<template>
  <div class="overview-page" v-loading="loading">
    <header class="hero">
      <div>
        <p class="hero-kicker">Innovation Dashboard</p>
        <h1>全校创新创业成果总览</h1>
        <p class="hero-insight">{{ headline }}</p>
      </div>
      <div class="hero-stamp">
        <strong>{{ yearRange }}</strong>
        <span>五年成果窗口</span>
      </div>
    </header>

    <section class="kpi-row">
      <article v-for="item in kpis" :key="item.label" class="kpi-card" :class="item.tone">
        <div class="kpi-label">{{ item.label }}</div>
        <div class="kpi-value">
          {{ item.value }}<small v-if="item.suffix">{{ item.suffix }}</small>
        </div>
        <div class="kpi-sub" :class="{ up: item.delta >= 0, down: item.delta < 0 }">
          {{ item.hint }}
        </div>
      </article>
    </section>

    <section class="main-grid">
      <el-card shadow="never" class="panel hero-chart">
        <div class="panel-head">
          <div>
            <h3>五年成果增长曲线</h3>
            <p>立项数量与获奖数量对照，看学校创新创业是否在爬坡</p>
          </div>
        </div>
        <div ref="chartTrendRef" class="chart chart-lg" />
      </el-card>

      <el-card shadow="never" class="panel">
        <div class="panel-head">
          <div>
            <h3>学院成果榜</h3>
            <p>按获奖数排序，一眼看出领先学院</p>
          </div>
        </div>
        <div ref="chartRankRef" class="chart chart-lg" />
      </el-card>
    </section>

    <section class="sub-grid">
      <el-card shadow="never" class="panel">
        <div class="panel-head">
          <h3>项目流转漏斗</h3>
          <p>申报到入驻的转化路径</p>
        </div>
        <div ref="chartFunnelRef" class="chart" />
      </el-card>

      <el-card shadow="never" class="panel">
        <div class="panel-head">
          <h3>项目类型结构</h3>
          <p>各赛道在册项目占比</p>
        </div>
        <div ref="chartTypeRef" class="chart" />
      </el-card>

      <el-card shadow="never" class="panel">
        <div class="panel-head">
          <h3>奖项等级构成</h3>
          <p>金奖到优秀奖的成果结构</p>
        </div>
        <div ref="chartAwardRef" class="chart" />
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getOverviewData, type Award, type Project } from '@/api/project'

const PALETTE = ['#4f6ef7', '#10b981', '#f59e0b', '#06b6d4', '#8b5cf6', '#ef4444', '#14b8a6', '#fb7185']

const loading = ref(false)
const headline = ref('正在汇总全校创新创业成果…')
const kpis = ref<Array<{ label: string; value: string; suffix?: string; hint: string; delta: number; tone: string }>>([])

const chartTrendRef = ref<HTMLDivElement | null>(null)
const chartRankRef = ref<HTMLDivElement | null>(null)
const chartFunnelRef = ref<HTMLDivElement | null>(null)
const chartTypeRef = ref<HTMLDivElement | null>(null)
const chartAwardRef = ref<HTMLDivElement | null>(null)

let chartTrend: echarts.ECharts | null = null
let chartRank: echarts.ECharts | null = null
let chartFunnel: echarts.ECharts | null = null
let chartType: echarts.ECharts | null = null
let chartAward: echarts.ECharts | null = null

const getLastYears = () => {
  const y = new Date().getFullYear()
  return Array.from({ length: 5 }, (_, i) => y - 4 + i)
}

const yearRange = computed(() => {
  const years = getLastYears()
  return `${years[0]} – ${years[years.length - 1]}`
})

const safeYear = (dt?: string) => {
  if (!dt) return undefined
  const d = new Date(dt.replace(' ', 'T'))
  if (Number.isNaN(d.getTime())) return undefined
  return d.getFullYear()
}

const disposeAll = () => {
  ;[chartTrend, chartRank, chartFunnel, chartType, chartAward].forEach((c) => c?.dispose())
  chartTrend = chartRank = chartFunnel = chartType = chartAward = null
}

const resizeAll = () => {
  chartTrend?.resize()
  chartRank?.resize()
  chartFunnel?.resize()
  chartType?.resize()
  chartAward?.resize()
}

const ensureCharts = () => {
  if (chartTrendRef.value && !chartTrend) chartTrend = echarts.init(chartTrendRef.value)
  if (chartRankRef.value && !chartRank) chartRank = echarts.init(chartRankRef.value)
  if (chartFunnelRef.value && !chartFunnel) chartFunnel = echarts.init(chartFunnelRef.value)
  if (chartTypeRef.value && !chartType) chartType = echarts.init(chartTypeRef.value)
  if (chartAwardRef.value && !chartAward) chartAward = echarts.init(chartAwardRef.value)
}

const renderDashboard = (projects: Project[], awards: Award[]) => {
  const years = getLastYears()
  const thisYear = years[years.length - 1]
  const lastYear = years[years.length - 2]

  const projectMeta = new Map<number, { college: string; type: string }>()
  projects.forEach((p) => {
    if (!p.id) return
    projectMeta.set(p.id, {
      college: p.applicantCollege || '未知学院',
      type: p.projectType || '未分类'
    })
  })

  const approvedLike = (s?: string) => s === 'approved' || s === 'settled'
  const approvedCount = projects.filter((p) => approvedLike(p.status)).length
  const settledCount = projects.filter((p) => p.status === 'settled').length
  const decided = projects.filter((p) => ['approved', 'rejected', 'settled'].includes(p.status || '')).length
  const passRate = decided === 0 ? 0 : Math.round(((approvedCount) / decided) * 100)

  const hasStatus = awards.some((a) => a.status !== undefined)
  const filteredAwards = hasStatus ? awards.filter((a) => a.status === 'approved') : awards

  const collegeSet = new Set(projects.map((p) => p.applicantCollege || '未知学院'))
  const awardsThisYear = filteredAwards.filter((a) => safeYear(a.createTime) === thisYear).length
  const awardsLastYear = filteredAwards.filter((a) => safeYear(a.createTime) === lastYear).length
  const awardDelta = awardsThisYear - awardsLastYear

  kpis.value = [
    {
      label: '在册项目',
      value: String(projects.length),
      hint: `${collegeSet.size} 个学院共同参与`,
      delta: 1,
      tone: 'blue'
    },
    {
      label: '获批 / 入驻',
      value: String(approvedCount),
      hint: `其中已入驻 ${settledCount} 项`,
      delta: 1,
      tone: 'green'
    },
    {
      label: '认定获奖',
      value: String(filteredAwards.length),
      hint: awardDelta >= 0 ? `今年比去年多 ${awardDelta} 项` : `今年比去年少 ${Math.abs(awardDelta)} 项`,
      delta: awardDelta,
      tone: 'gold'
    },
    {
      label: '审批通过率',
      value: String(passRate),
      suffix: '%',
      hint: decided ? `基于 ${decided} 个已审项目` : '暂无已审项目',
      delta: passRate >= 60 ? 1 : -1,
      tone: 'cyan'
    }
  ]

  headline.value =
    filteredAwards.length > 0
      ? `近五年累计产出 ${filteredAwards.length} 项认定成果，覆盖 ${collegeSet.size} 个学院；今年新增 ${awardsThisYear} 项获奖。`
      : `当前在册项目 ${projects.length} 项，覆盖 ${collegeSet.size} 个学院，成果正在持续积累。`

  const projectYear = new Map<number, number>()
  const awardYear = new Map<number, number>()
  years.forEach((y) => {
    projectYear.set(y, 0)
    awardYear.set(y, 0)
  })
  projects.forEach((p) => {
    const y = safeYear(p.submitTime || p.createTime)
    if (y && projectYear.has(y)) projectYear.set(y, (projectYear.get(y) || 0) + 1)
  })
  filteredAwards.forEach((a) => {
    const y = safeYear(a.createTime)
    if (y && awardYear.has(y)) awardYear.set(y, (awardYear.get(y) || 0) + 1)
  })

  const collegeAwards = new Map<string, number>()
  const collegeProjects = new Map<string, number>()
  projects.forEach((p) => {
    const c = p.applicantCollege || '未知学院'
    collegeProjects.set(c, (collegeProjects.get(c) || 0) + 1)
  })
  filteredAwards.forEach((a) => {
    const meta = a.projectId ? projectMeta.get(a.projectId) : undefined
    const c = meta?.college || '未知学院'
    collegeAwards.set(c, (collegeAwards.get(c) || 0) + 1)
  })
  const rank = Array.from(collegeSet)
    .map((college) => ({
      college,
      awards: collegeAwards.get(college) || 0,
      projects: collegeProjects.get(college) || 0
    }))
    .sort((a, b) => b.awards - a.awards || b.projects - a.projects)
    .slice(0, 8)

  const funnelOrder: Array<{ key: string; name: string }> = [
    { key: 'submitted', name: '已申报' },
    { key: 'approved', name: '已审批' },
    { key: 'settled', name: '已入驻' }
  ]
  const funnelData = funnelOrder.map((item, index) => ({
    name: item.name,
    value: projects.filter((p) => {
      if (item.key === 'submitted') return ['submitted', 'approved', 'settled', 'rejected'].includes(p.status || '')
      if (item.key === 'approved') return ['approved', 'settled'].includes(p.status || '')
      return p.status === 'settled'
    }).length,
    itemStyle: { color: PALETTE[index] }
  }))

  const typeCount = new Map<string, number>()
  projects.forEach((p) => {
    const t = p.projectType || '未分类'
    typeCount.set(t, (typeCount.get(t) || 0) + 1)
  })

  const awardLevelCount = new Map<string, number>()
  filteredAwards.forEach((a) => {
    const lv = a.awardLevel || a.competitionLevel || '未分级'
    awardLevelCount.set(lv, (awardLevelCount.get(lv) || 0) + 1)
  })

  ensureCharts()

  chartTrend?.setOption({
    color: ['#4f6ef7', '#f59e0b'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0, right: 0, icon: 'circle' },
    grid: { left: 40, right: 18, top: 36, bottom: 28 },
    xAxis: {
      type: 'category',
      data: years.map(String),
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { type: 'dashed', color: '#eef2f7' } }
    },
    series: [
      {
        name: '立项数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(79,110,247,0.28)' },
            { offset: 1, color: 'rgba(79,110,247,0.02)' }
          ])
        },
        data: years.map((y) => projectYear.get(y) || 0)
      },
      {
        name: '获奖数',
        type: 'line',
        smooth: true,
        symbol: 'diamond',
        symbolSize: 8,
        lineStyle: { width: 3 },
        data: years.map((y) => awardYear.get(y) || 0)
      }
    ]
  })

  chartRank?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 96, right: 24, top: 12, bottom: 24 },
    xAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { type: 'dashed', color: '#eef2f7' } } },
    yAxis: {
      type: 'category',
      data: rank.map((i) => i.college).reverse(),
      axisLabel: { color: '#374151' }
    },
    series: [
      {
        name: '获奖',
        type: 'bar',
        barMaxWidth: 16,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#fbbf24' },
            { offset: 1, color: '#f59e0b' }
          ])
        },
        data: rank.map((i) => i.awards).reverse()
      }
    ]
  })

  chartFunnel?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}：{c}' },
    series: [
      {
        type: 'funnel',
        left: '12%',
        width: '76%',
        minSize: '18%',
        gap: 8,
        label: { formatter: '{b}\n{c}', color: '#111827', fontWeight: 600 },
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        data: funnelData
      }
    ]
  })

  chartType?.setOption({
    color: PALETTE,
    tooltip: { trigger: 'item', formatter: '{b}：{c}（{d}%）' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '46%'],
        roseType: 'area',
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{d}%' },
        data: Array.from(typeCount.entries()).map(([name, value]) => ({ name, value }))
      }
    ]
  })

  chartAward?.setOption({
    color: ['#f59e0b', '#4f6ef7', '#10b981', '#8b5cf6', '#06b6d4', '#ef4444'],
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['50%', '72%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 3 },
        label: { formatter: '{b}\n{c}项' },
        data: Array.from(awardLevelCount.entries()).map(([name, value]) => ({ name, value }))
      }
    ]
  })

  resizeAll()
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getOverviewData()
    await nextTick()
    renderDashboard(data?.projects ?? [], data?.awards ?? [])
  } catch (e: any) {
    ElMessage.error(e?.message || '加载项目一览数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchData()
  window.addEventListener('resize', resizeAll)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeAll)
  disposeAll()
})
</script>

<style scoped>
.overview-page {
  padding: 8px 4px 24px;
  min-height: calc(100vh - 72px);
  background:
    radial-gradient(1200px 420px at 0% -10%, rgba(79, 110, 247, 0.1), transparent 55%),
    radial-gradient(900px 380px at 100% 0%, rgba(245, 158, 11, 0.1), transparent 50%),
    #f6f8fc;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 18px;
  padding: 8px 4px 0;
}

.hero-kicker {
  margin: 0 0 4px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #4f6ef7;
  font-weight: 700;
}

.hero h1 {
  margin: 0;
  font-size: 26px;
  color: #111827;
  letter-spacing: -0.02em;
}

.hero-insight {
  margin: 8px 0 0;
  max-width: 720px;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
}

.hero-stamp {
  min-width: 132px;
  padding: 10px 14px;
  border-radius: 14px;
  background: linear-gradient(135deg, #111827, #312e81);
  color: #fff;
  text-align: right;
  box-shadow: 0 12px 28px rgba(17, 24, 39, 0.18);
}

.hero-stamp strong {
  display: block;
  font-size: 16px;
}

.hero-stamp span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.kpi-card {
  padding: 16px 18px 14px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eef2f7;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.kpi-card.gold {
  background: linear-gradient(180deg, #fffbeb, #fff);
}

.kpi-label {
  font-size: 13px;
  color: #6b7280;
}

.kpi-value {
  margin-top: 6px;
  font-size: 32px;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.04em;
  line-height: 1.1;
}

.kpi-value small {
  margin-left: 2px;
  font-size: 16px;
}

.kpi-sub {
  margin-top: 8px;
  font-size: 12px;
  color: #9ca3af;
}

.kpi-sub.up {
  color: #059669;
}

.kpi-sub.down {
  color: #dc2626;
}

.main-grid,
.sub-grid {
  display: grid;
  gap: 14px;
  margin-bottom: 14px;
}

.main-grid {
  grid-template-columns: 1.4fr 1fr;
}

.sub-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.panel {
  border: 1px solid #e8eef6;
  border-radius: 16px;
  overflow: hidden;
}

.panel-head {
  padding: 4px 4px 0;
}

.panel-head h3 {
  margin: 0;
  font-size: 15px;
  color: #111827;
}

.panel-head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.chart {
  height: 280px;
  width: 100%;
}

.chart-lg {
  height: 340px;
}

@media (max-width: 1200px) {
  .kpi-row,
  .main-grid,
  .sub-grid {
    grid-template-columns: 1fr;
  }
}
</style>
