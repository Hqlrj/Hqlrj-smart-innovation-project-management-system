<template>
  <div class="project-overview-container">
    <div class="overview-grid">
      <el-card shadow="never" class="chart-card">
        <div class="card-title">各学院创新创业项目在驻比例</div>
        <div ref="chartCollegeRef" class="chart" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <div class="card-title">各类型项目比例情况</div>
        <div ref="chartTypeRef" class="chart" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <div class="card-title">近五年各学院获奖趋势</div>
        <div ref="chartCollegeTrendRef" class="chart" />
      </el-card>

      <el-card shadow="never" class="chart-card">
        <div class="card-title">近五年各类型项目所获奖项</div>
        <div ref="chartTypeTrendRef" class="chart" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getProjectList, getAwardList, type Project, type Award, type PageResult } from '@/api/project'

const chartCollegeRef = ref<HTMLDivElement | null>(null)
const chartTypeRef = ref<HTMLDivElement | null>(null)
const chartCollegeTrendRef = ref<HTMLDivElement | null>(null)
const chartTypeTrendRef = ref<HTMLDivElement | null>(null)

let chartCollege: echarts.ECharts | null = null
let chartType: echarts.ECharts | null = null
let chartCollegeTrend: echarts.ECharts | null = null
let chartTypeTrend: echarts.ECharts | null = null

const disposeAll = () => {
  chartCollege?.dispose(); chartCollege = null
  chartType?.dispose(); chartType = null
  chartCollegeTrend?.dispose(); chartCollegeTrend = null
  chartTypeTrend?.dispose(); chartTypeTrend = null
}

const resizeAll = () => {
  chartCollege?.resize()
  chartType?.resize()
  chartCollegeTrend?.resize()
  chartTypeTrend?.resize()
}

const safeYear = (dt?: string) => {
  if (!dt) return undefined
  // "yyyy-MM-dd HH:mm:ss" -> "yyyy-MM-ddTHH:mm:ss"
  const isoLike = dt.replace(' ', 'T')
  const d = new Date(isoLike)
  if (Number.isNaN(d.getTime())) return undefined
  return d.getFullYear()
}

const mapToPieData = (m: Map<string, number>) =>
  Array.from(m.entries()).map(([name, value]) => ({ name, value }))

const getLastYears = () => {
  const now = new Date()
  const y = now.getFullYear()
  return Array.from({ length: 5 }, (_, i) => y - 4 + i)
}

const fetchData = async () => {
  try {
    const [projRes, awardRes] = await Promise.all([
      getProjectList({ page: 1, pageSize: 10000 }),
      getAwardList({ page: 1, pageSize: 10000 })
    ])

    const projects = (projRes as unknown as PageResult<Project>)?.list ?? []
    const awards = (awardRes as unknown as PageResult<Award>)?.list ?? []

    renderCharts(projects, awards)
  } catch (e: any) {
    ElMessage.error(e?.message || '加载项目一览数据失败')
  }
}

const renderCharts = (projects: Project[], awards: Award[]) => {
  const years = getLastYears()

  // projectId -> { college, type }
  const projectMeta = new Map<number, { college: string; type: string }>()
  projects.forEach(p => {
    if (!p.id) return
    projectMeta.set(p.id, {
      college: p.applicantCollege || '未知学院',
      type: p.projectType || '未分类'
    })
  })

  // 1) 各学院项目占比（用项目数量做占比，样式做成环形）
  const collegeCount = new Map<string, number>()
  projects.forEach(p => {
    const c = p.applicantCollege || '未知学院'
    collegeCount.set(c, (collegeCount.get(c) || 0) + 1)
  })

  // 2) 各类型项目占比
  const typeCount = new Map<string, number>()
  projects.forEach(p => {
    const t = p.projectType || '未分类'
    typeCount.set(t, (typeCount.get(t) || 0) + 1)
  })

  // 奖项：优先统计 approved；如果没有 status 字段则统计全部
  const hasStatus = awards.some(a => a.status !== undefined)
  const filteredAwards = hasStatus ? awards.filter(a => a.status === 'approved') : awards

  // 3) 近五年各学院获奖趋势（取获奖最多的前6个学院，避免线太多）
  const collegeYearCount = new Map<string, Map<number, number>>()
  filteredAwards.forEach(a => {
    const pid = a.projectId
    if (!pid) return
    const meta = projectMeta.get(pid)
    if (!meta) return
    const y = safeYear(a.createTime)
    if (!y || !years.includes(y)) return

    const cm = collegeYearCount.get(meta.college) || new Map<number, number>()
    cm.set(y, (cm.get(y) || 0) + 1)
    collegeYearCount.set(meta.college, cm)
  })

  const collegeTotal = Array.from(collegeYearCount.entries())
    .map(([college, ym]) => ({
      college,
      total: Array.from(ym.values()).reduce((s, v) => s + v, 0)
    }))
    .sort((a, b) => b.total - a.total)
    .slice(0, 6)

  const topColleges = collegeTotal.map(i => i.college)

  // 4) 近五年各类型项目所获奖项（堆叠柱状）
  const typeYearCount = new Map<string, Map<number, number>>()
  filteredAwards.forEach(a => {
    const pid = a.projectId
    if (!pid) return
    const meta = projectMeta.get(pid)
    if (!meta) return
    const y = safeYear(a.createTime)
    if (!y || !years.includes(y)) return

    const tm = typeYearCount.get(meta.type) || new Map<number, number>()
    tm.set(y, (tm.get(y) || 0) + 1)
    typeYearCount.set(meta.type, tm)
  })

  // 初始化图表
  if (chartCollegeRef.value && !chartCollege) chartCollege = echarts.init(chartCollegeRef.value)
  if (chartTypeRef.value && !chartType) chartType = echarts.init(chartTypeRef.value)
  if (chartCollegeTrendRef.value && !chartCollegeTrend) chartCollegeTrend = echarts.init(chartCollegeTrendRef.value)
  if (chartTypeTrendRef.value && !chartTypeTrend) chartTypeTrend = echarts.init(chartTypeTrendRef.value)

  chartCollege?.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 10, left: 'center', type: 'scroll' },
    series: [
      {
        name: '学院占比',
        type: 'pie',
        radius: ['55%', '75%'],
        center: ['50%', '58%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 600 },
          itemStyle: { shadowBlur: 12, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.15)' }
        },
        data: mapToPieData(collegeCount)
      }
    ]
  })

  chartType?.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: 10, left: 'center' },
    series: [
      {
        name: '类型占比',
        type: 'pie',
        radius: '72%',
        center: ['50%', '58%'],
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { formatter: '{b}: {d}%' },
        data: mapToPieData(typeCount)
      }
    ]
  })

  chartCollegeTrend?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 10, left: 'center' },
    grid: { left: 40, right: 20, top: 50, bottom: 30 },
    xAxis: { type: 'category', data: years.map(String) },
    // 固定刻度：0~6（显示 0,1,2,3,4,5,6）
    yAxis: {
      type: 'value',
      min: 0,
      max: 6,
      interval: 1,
      axisLabel: { formatter: '{value}' }
    },
    series: topColleges.map(college => {
      const ym = collegeYearCount.get(college) || new Map<number, number>()
      return {
        name: college,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: years.map(y => ym.get(y) || 0)
      }
    })
  })

  const typeSeries = Array.from(typeYearCount.keys())
    .map(type => {
      const ym = typeYearCount.get(type) || new Map<number, number>()
      return {
        name: type,
        type: 'bar',
        stack: 'total',
        barMaxWidth: 40,
        emphasis: { focus: 'series' },
        data: years.map(y => ym.get(y) || 0)
      }
    })

  chartTypeTrend?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 10, left: 'center', type: 'scroll' },
    grid: { left: 40, right: 20, top: 50, bottom: 30 },
    xAxis: { type: 'category', data: years.map(String) },
    yAxis: { type: 'value' },
    series: typeSeries
  })

  resizeAll()
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
.project-overview-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.chart-card {
  border: 1px solid #ebeef5;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  padding: 8px 4px 12px;
}

.chart {
  height: 340px;
  width: 100%;
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
