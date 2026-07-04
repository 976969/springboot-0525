<!--
  学生端首页（数据统计、图表展示、学习进度）
-->
<template>
  <div class="dashboard-container">
    <!-- 顶部轮播大屏 -->
    <BannerCarousel />

    <!-- 欢迎信息卡片 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>👋 欢迎回来, {{ userStore.userInfo.realName || '同学' }}!</h2>
          <p>今天是你学习的第 <span class="highlight">{{ studyDays }}</span> 天</p>
        </div>
        <div class="welcome-avatar">
          <el-avatar :size="80" icon="UserFilled" />
        </div>
      </div>
    </el-card>

    <!-- 数字统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            📚
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount || 0 }}</div>
            <div class="stat-label">我的课程</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            ✅
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completedTaskCount || 0 }}</div>
            <div class="stat-label">已完成任务</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            📊
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.averageScore || 0 }}<span class="unit">分</span></div>
            <div class="stat-label">平均成绩</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            🏆
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.certificateCount || 0 }}</div>
            <div class="stat-label">获得证书</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ECharts图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 学习进度趋势(折线图) -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📈 提交记录趋势(最近30天)</span>
            </div>
          </template>
          <div ref="progressChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 课程完成度(饼图) -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🥧 课程完成度</span>
            </div>
          </template>
          <div ref="completionChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 成绩分布(柱状图) -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 成绩分布</span>
            </div>
          </template>
          <div ref="scoreChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 快捷入口 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>⚡ 快捷入口</span>
            </div>
          </template>
          <el-row :gutter="15" style="margin-top: 20px">
            <el-col :span="8">
              <el-button type="primary" plain @click="$router.push('/my-courses')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📚</div>
                  <div>我的课程</div>
                </div>
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button type="warning" plain @click="$router.push('/upload')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📤</div>
                  <div>提交成果</div>
                </div>
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button plain @click="$router.push('/profile')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">👤</div>
                  <div>个人中心</div>
                </div>
              </el-button>
            </el-col>
          </el-row>
          <el-row :gutter="15" style="margin-top: 15px">
            <el-col :span="8">
              <el-button type="info" plain @click="$router.push('/my-scores')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📝</div>
                  <div>评价记录</div>
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import * as echarts from 'echarts'
import BannerCarousel from '../components/BannerCarousel.vue'

const userStore = useUserStore()

// 统计数据
const stats = reactive({
  courseCount: 0,
  completedTaskCount: 0,
  averageScore: 0,
  certificateCount: 0
})

// 学习天数(真实，从首次提交算起)
const studyDays = ref(1)

// 图表DOM引用
const progressChartRef = ref(null)
const completionChartRef = ref(null)
const scoreChartRef = ref(null)

// 图表实例
let progressChart = null
let completionChart = null
let scoreChart = null

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await request.get('/dashboard/student/stats')
    Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

// 加载学习天数
const loadStudyDays = async () => {
  try {
    const res = await request.get('/dashboard/student/study-days')
    studyDays.value = res.data?.studyDays || 1
  } catch (e) {
    console.error('加载学习天数失败:', e)
  }
}

// 加载学习进度数据（最近30天提交数量）
const loadProgressData = async () => {
  try {
    const res = await request.get('/dashboard/student/progress')
    return res.data
  } catch (e) {
    console.error('加载进度数据失败:', e)
    return []
  }
}

// 加载课程完成度数据
const loadCompletionData = async () => {
  try {
    const res = await request.get('/dashboard/student/completion')
    return res.data
  } catch (e) {
    console.error('加载完成度数据失败:', e)
    return []
  }
}

// 加载成绩分布数据
const loadScoreData = async () => {
  try {
    const res = await request.get('/dashboard/student/scores')
    return res.data
  } catch (e) {
    console.error('加载成绩数据失败:', e)
    return { ranges: [], counts: [] }
  }
}

// 初始化图表
const initCharts = async () => {
  // 等待DOM更新
  await nextTick()

  // 1. 学习进度趋势图(折线图)
  if (progressChartRef.value) {
    progressChart = echarts.init(progressChartRef.value)
    const progressData = await loadProgressData()

    progressChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>提交数量: {c} 份'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: progressData.map(item => item.date),
        axisLabel: {
          interval: 5,
          rotate: 45
        }
      },
      yAxis: {
        type: 'value',
        name: '提交数量(份)',
        minInterval: 1
      },
      series: [{
        name: '提交数量',
        type: 'line',
        smooth: true,
        data: progressData.map(item => item.submissions),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#667eea',
          width: 3
        },
        itemStyle: {
          color: '#667eea'
        }
      }]
    })
  }

  // 2. 课程完成度(饼图)
  if (completionChartRef.value) {
    completionChart = echarts.init(completionChartRef.value)
    const completionData = await loadCompletionData()

    completionChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}% ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: completionData.map(item => item.name)
      },
      series: [{
        name: '完成度',
        type: 'pie',
        radius: '60%',
        data: completionData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}\n{c}%'
        }
      }]
    })
  }

  // 3. 成绩分布(柱状图)
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
    const scoreData = await loadScoreData()

    scoreChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}分<br/>人数: {c}'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: scoreData.ranges,
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        name: '人数'
      },
      series: [{
        name: '人数',
        type: 'bar',
        data: scoreData.counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4facfe' },
            { offset: 1, color: '#00f2fe' }
          ])
        },
        barWidth: '50%'
      }]
    })
  }
}

// 组件挂载时执行
onMounted(async () => {
  await Promise.all([loadStats(), loadStudyDays()])
  await initCharts()

  // 窗口大小变化时重新调整图表
  window.addEventListener('resize', () => {
    progressChart?.resize()
    completionChart?.resize()
    scoreChart?.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  color: white;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
}

.highlight {
  font-size: 28px;
  font-weight: bold;
  color: #ffd700;
  margin: 0 5px;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.stat-card :deep(.el-card__body) {
  padding: 25px 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-info {
  margin-top: 12px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-value .unit {
  font-size: 16px;
  font-weight: normal;
  margin-left: 4px;
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 10px;
  font-weight: 500;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 17px;
  color: #303133;
}

/* 图表卡片样式 */
:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  padding: 18px 24px;
  border-bottom: 1px solid #ebeef5;
}

/* 快捷按钮样式 */
:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 响应式布局 */
@media (max-width: 768px) {
  .stat-card {
    margin-bottom: 20px;
  }

  .welcome-text h2 {
    font-size: 22px;
  }

  .stat-value {
    font-size: 28px;
  }
}
</style>