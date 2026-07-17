<!--
  管理员端首页（系统总览、全局数据统计）
-->
<template>
  <div class="dashboard-container">
    <!-- 欢迎信息 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.userInfo.realName || '管理员' }}!</h2>
          <p>智能实训评价系统 · 数据总览</p>
        </div>
        <div class="welcome-time">
          <div class="time-text">{{ currentTime }}</div>
          <div class="date-text">{{ currentDate }}</div>
        </div>
      </div>
    </el-card>

    <!-- 核心统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount || 0 }}</div>
            <div class="stat-label">注册学生</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <el-icon :size="32"><Avatar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.teacherCount || 0 }}</div>
            <div class="stat-label">在职教师</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <el-icon :size="32"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">开设课程</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card--warn">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value text-warn">{{ stats.pendingResults || 0 }}</div>
            <div class="stat-label">待评价成果</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第一行 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 提交趋势 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span class="card-title">📈 成果提交趋势（近30天）</span></template>
          <div ref="progressChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <!-- 成果状态分布 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span class="card-title">🔄 成果评价状态</span></template>
          <div ref="statusChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第二行 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 课程选课人数 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">📚 各课程选课人数</span></template>
          <div ref="enrollmentChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <!-- 成绩分布 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">📊 全系统成绩分布</span></template>
          <div ref="scoreChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span class="card-title">🕐 最近提交动态</span>
              <el-tag type="info" size="small">最近 10 条</el-tag>
            </div>
          </template>
          <el-table :data="recentList" stripe size="small" v-loading="recentLoading" max-height="320">
            <el-table-column prop="studentName" label="学生" width="100" />
            <el-table-column prop="courseName" label="课程" width="140" show-overflow-tooltip />
            <el-table-column prop="taskTitle" label="任务" min-width="180" show-overflow-tooltip />
            <el-table-column prop="fileName" label="文件名" width="160" show-overflow-tooltip />
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.statusText)" size="small">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="uploadTime" label="提交时间" width="170" align="center" />
          </el-table>
          <el-empty v-if="recentList.length === 0 && !recentLoading" description="暂无提交记录" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" style="margin-top: 20px; margin-bottom: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span class="card-title">⚡ 快捷管理</span></template>
          <el-row :gutter="15">
            <el-col :span="4" v-for="item in quickLinks" :key="item.path">
              <el-button plain @click="$router.push(item.path)" style="width: 100%; height: 80px; border-radius: 8px;">
                <div style="text-align: center">
                  <div style="font-size: 24px; margin-bottom: 4px">{{ item.icon }}</div>
                  <div style="font-size: 14px">{{ item.label }}</div>
                </div>
              </el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import * as echarts from 'echarts'
import { User, Avatar, Reading, Clock } from '@element-plus/icons-vue'

const userStore = useUserStore()

// ============ 时间显示 ============
const currentTime = ref('')
const currentDate = ref('')
let timer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

// ============ 统计数据 ============
const stats = reactive({ studentCount: 0, teacherCount: 0, courseCount: 0, taskCount: 0 })
const recentList = ref([])
const recentLoading = ref(false)
const chartLoading = ref(false)

// ============ 快捷入口 ============
const quickLinks = [
  { path: '/user-manage', icon: '', label: '用户管理' },
  { path: '/course', icon: '📚', label: '课程管理' },
  { path: '/task', icon: '📝', label: '实训任务' },
  { path: '/evaluate', icon: '📊', label: '评分与报表' },
  { path: '/schedule-manage', icon: '📅', label: '课程表管理' }
]

// ============ 图表 ============
const progressChartRef = ref(null)
const statusChartRef = ref(null)
const enrollmentChartRef = ref(null)
const scoreChartRef = ref(null)
let progressChart = null, statusChart = null, enrollmentChart = null, scoreChart = null

// ============ 数据加载 ============
const loadStats = async () => {
  try {
    const res = await request.get('/dashboard/admin/stats')
    Object.assign(stats, res.data)
  } catch (e) { console.error('加载统计失败:', e) }
}

const loadRecent = async () => {
  recentLoading.value = true
  try {
    const res = await request.get('/dashboard/admin/recent')
    recentList.value = res.data || []
  } catch (e) { console.error('加载最近记录失败:', e) } finally { recentLoading.value = false }
}

const statusTagType = (text) => {
  if (text === '待评价') return 'warning'
  if (text === '已评价') return 'success'
  return ''
}

// ============ 图表初始化 ============
const initCharts = async () => {
  await nextTick()

  try {
    // 1. 提交趋势（折线图）
    if (progressChartRef.value) {
      const res = await request.get('/dashboard/admin/progress')
      const data = res.data || []
      progressChart = echarts.init(progressChartRef.value)
      progressChart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>提交: {c} 份' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: data.map(i => i.date), axisLabel: { interval: 4, rotate: 45 } },
        yAxis: { type: 'value', name: '提交量(份)', minInterval: 1 },
        series: [{
          type: 'line', smooth: true,
          data: data.map(i => i.submissions),
          areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(79,172,254,0.3)' }, { offset: 1, color: 'rgba(79,172,254,0.02)' }]) },
          lineStyle: { color: '#4facfe', width: 3 },
          itemStyle: { color: '#4facfe' }
        }]
      })
    }

    // 2. 成果状态分布（饼图）
    if (statusChartRef.value) {
      const res = await request.get('/dashboard/admin/result-status')
      const data = (res.data || []).map(i => ({ name: i.name, value: i.value }))
      statusChart = echarts.init(statusChartRef.value)
      statusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}份 ({d}%)' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['40%', '70%'],
          data: data,
          emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
          label: { formatter: '{b}\n{d}%' }
        }]
      })
    }

    // 3. 课程选课人数（横向柱状图）
    if (enrollmentChartRef.value) {
      const res = await request.get('/dashboard/admin/enrollment')
      const data = res.data || []
      enrollmentChart = echarts.init(enrollmentChartRef.value)
      enrollmentChart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}: {c}人' },
        grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
        xAxis: { type: 'value', name: '人数', minInterval: 1 },
        yAxis: { type: 'category', data: data.map(i => i.course).reverse(), axisLabel: { width: 100, overflow: 'truncate' } },
        series: [{
          type: 'bar',
          data: data.map(i => i.students).reverse(),
          itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }]) },
          barWidth: '50%'
        }]
      })
    }

    // 4. 成绩分布（柱状图）
    if (scoreChartRef.value) {
      const res = await request.get('/dashboard/admin/scores')
      const ranges = res.data?.ranges || []
      const counts = res.data?.counts || []
      scoreChart = echarts.init(scoreChartRef.value)
      scoreChart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>{c}份' },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: ranges, axisLabel: { interval: 0, rotate: 20 } },
        yAxis: { type: 'value', name: '作业数' },
        series: [{
          type: 'bar',
          data: counts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f093fb' }, { offset: 1, color: '#f5576c' }])
          },
          barWidth: '50%'
        }]
      })
    }
  } catch (e) {
    console.error('图表初始化失败:', e)
  }
}

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  await loadStats()
  await Promise.all([loadRecent(), initCharts()])

  window.addEventListener('resize', () => {
    progressChart?.resize()
    statusChart?.resize()
    enrollmentChart?.resize()
    scoreChart?.resize()
  })
})

onBeforeUnmount(() => {
  clearInterval(timer)
  progressChart?.dispose()
  statusChart?.dispose()
  enrollmentChart?.dispose()
  scoreChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { max-width: 1400px; margin: 0 auto; }

.welcome-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: white; border: none; border-radius: 16px;
  box-shadow: 0 8px 24px rgba(15, 52, 96, 0.3);
}
.welcome-content { display: flex; justify-content: space-between; align-items: center; }
.welcome-text h2 { margin: 0 0 8px 0; font-size: 26px; color: white; font-weight: 600; }
.welcome-text p { margin: 0; font-size: 15px; color: rgba(255,255,255,0.7); }
.welcome-time { text-align: right; }
.time-text { font-size: 32px; font-weight: 300; color: rgba(255,255,255,0.95); font-variant-numeric: tabular-nums; }
.date-text { font-size: 14px; color: rgba(255,255,255,0.6); margin-top: 4px; }

.stat-card { text-align: center; border-radius: 12px; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
.stat-card--warn { position: relative; }
.stat-card--warn::after {
  content: ''; position: absolute; top: 0; right: 0;
  width: 8px; height: 8px; border-radius: 50%;
  background: #f56c6c; margin: 12px;
}
.stat-card :deep(.el-card__body) { padding: 25px 20px; }
.stat-icon {
  width: 64px; height: 64px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 12px; color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.stat-info { margin-top: 8px; }
.stat-value { font-size: 36px; font-weight: bold; color: #303133; line-height: 1; }
.stat-value.text-warn { color: #f56c6c; }
.stat-label { font-size: 14px; color: #909399; margin-top: 10px; font-weight: 500; }

.card-title { font-weight: bold; font-size: 16px; color: #303133; }

:deep(.el-card) { border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
:deep(.el-card__header) { padding: 18px 24px; border-bottom: 1px solid #ebeef5; }
:deep(.el-button) { border-radius: 8px; transition: all 0.3s; }
:deep(.el-button:hover) { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.12); }

@media (max-width: 768px) {
  .stat-card { margin-bottom: 20px; }
  .welcome-text h2 { font-size: 20px; }
  .stat-value { font-size: 28px; }
  .time-text { font-size: 24px; }
}
</style>
