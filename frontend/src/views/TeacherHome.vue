<!--
  教师端首页（教学统计、图表展示、待办事项）
-->
<template>
  <div class="dashboard-container">
    <!-- 顶部轮播大屏 -->
    <BannerCarousel />

    <!-- 欢迎信息卡片 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>👋 欢迎, {{ userStore.userInfo.realName || '老师' }}!</h2>
          <p>您今天有 <span class="highlight">{{ stats.pendingTasks || 0 }}</span> 个任务待批改</p>
        </div>
        <div class="welcome-avatar">
          <el-avatar :size="80" :src="userStore.userInfo.avatar || ''" icon="UserFilled" />
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
            <div class="stat-label">教授课程</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            👥
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            ✅
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingTasks || 0 }}</div>
            <div class="stat-label">待批改任务</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            📊
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.averageScore || 0 }}<span class="unit">分</span></div>
            <div class="stat-label">平均成绩</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ECharts图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 各课程学生人数(柱状图) -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📊 各课程学生人数</span>
            </div>
          </template>
          <div ref="studentChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>

      <!-- 任务提交趋势(折线图) -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>📈 近7天任务提交趋势</span>
            </div>
          </template>
          <div ref="taskChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 成绩分布(饼图) -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>🥧 学生成绩分布</span>
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
            <el-col :span="12">
              <el-button type="primary" plain @click="$router.push('/course')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📚</div>
                  <div>课程管理</div>
                </div>
              </el-button>
            </el-col>
            <el-col :span="12">
              <el-button type="success" plain @click="$router.push('/task')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">✅</div>
                  <div>实训任务</div>
                </div>
              </el-button>
            </el-col>
          </el-row>
          <el-row :gutter="15" style="margin-top: 15px">
            <el-col :span="8">
              <el-button type="info" plain @click="$router.push('/evaluate')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📝</div>
                  <div>评价管理</div>
                </div>
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button type="danger" plain @click="$router.push('/report')" style="width: 100%; height: 80px">
                <div style="text-align: center">
                  <div style="font-size: 24px">📊</div>
                  <div>报表中心</div>
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
  studentCount: 0,
  pendingTasks: 0,
  averageScore: 0
})

// 图表DOM引用
const studentChartRef = ref(null)
const taskChartRef = ref(null)
const scoreChartRef = ref(null)

// 图表实例
let studentChart = null
let taskChart = null
let scoreChart = null

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await request.get('/dashboard/teacher/stats')
    Object.assign(stats, res.data)
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

// 加载学生人数数据
const loadStudentData = async () => {
  try {
    const res = await request.get('/dashboard/teacher/students')
    return res.data
  } catch (e) {
    console.error('加载学生数据失败:', e)
    return []
  }
}

// 加载任务提交数据
const loadTaskData = async () => {
  try {
    const res = await request.get('/dashboard/teacher/tasks')
    return res.data
  } catch (e) {
    console.error('加载任务数据失败:', e)
    return []
  }
}

// 加载成绩分布数据
const loadScoreData = async () => {
  try {
    const res = await request.get('/dashboard/teacher/scores')
    return res.data
  } catch (e) {
    console.error('加载成绩数据失败:', e)
    return []
  }
}

// 初始化图表
const initCharts = async () => {
  // 等待DOM更新
  await nextTick()

  // 1. 各课程学生人数(柱状图)
  if (studentChartRef.value) {
    studentChart = echarts.init(studentChartRef.value)
    const studentData = await loadStudentData()
    
    studentChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>学生人数: {c}'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: studentData.map(item => item.course),
        axisLabel: {
          interval: 0,
          rotate: 30
        }
      },
      yAxis: {
        type: 'value',
        name: '学生人数'
      },
      series: [{
        name: '学生人数',
        type: 'bar',
        data: studentData.map(item => item.students),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        },
        barWidth: '50%'
      }]
    })
  }

  // 2. 任务提交趋势(折线图)
  if (taskChartRef.value) {
    taskChart = echarts.init(taskChartRef.value)
    const taskData = await loadTaskData()
    
    taskChart.setOption({
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>提交数量: {c}'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: taskData.map(item => item.date)
      },
      yAxis: {
        type: 'value',
        name: '提交数量'
      },
      series: [{
        name: '提交数量',
        type: 'line',
        smooth: true,
        data: taskData.map(item => item.submissions),
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

  // 3. 成绩分布(饼图)
  if (scoreChartRef.value) {
    scoreChart = echarts.init(scoreChartRef.value)
    const scoreData = await loadScoreData()
    
    scoreChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        data: scoreData.map(item => item.name)
      },
      series: [{
        name: '成绩分布',
        type: 'pie',
        radius: '60%',
        data: scoreData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          formatter: '{b}\n{c}人'
        }
      }]
    })
  }
}

// 组件挂载时执行
onMounted(async () => {
  await loadStats()
  await initCharts()
  
  // 窗口大小变化时重新调整图表
  window.addEventListener('resize', () => {
    studentChart?.resize()
    taskChart?.resize()
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
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
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
