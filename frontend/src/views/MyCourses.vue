<!--
  学生端我的课程（课程列表、课程表、任务查看、权重图展示）
-->
<template>
  <div>
    <!-- 课程列表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的课程</span>
          <el-tag type="info" size="small">共 {{ total }} 门课程</el-tag>
        </div>
      </template>
      <el-table :data="courseList" border stripe v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="createTime" label="选课时间" width="180" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewTasks(row.courseId, row.courseName)">查看任务</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadCourses"
        @current-change="loadCourses"
      />
      
      <el-empty v-if="courseList.length === 0 && !loading" description="暂无选课信息" />
    </el-card>

    <!-- 课程表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的课程表</span>
          <el-tag type="info" size="small">共 {{ scheduleList.length }} 门课程安排</el-tag>
        </div>
      </template>
      <div class="timetable-container" v-loading="scheduleLoading">
        <table class="timetable">
          <thead>
            <tr>
              <th class="time-col">时间</th>
              <th v-for="day in days" :key="day.value">{{ day.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot">
              <td class="time-col">{{ slot }}</td>
              <td 
                v-for="day in days" 
                :key="day.value"
                class="schedule-cell"
                :class="{ 'has-class': getSchedule(day.value, slot) }"
                @click="handleCellClick(day.value, slot)"
              >
                <div v-if="getSchedule(day.value, slot)" class="class-info">
                  <div class="course-name">{{ getSchedule(day.value, slot).courseName }}</div>
                  <div class="class-time">{{ getSchedule(day.value, slot).startTime }} - {{ getSchedule(day.value, slot).endTime }}</div>
                  <div class="class-teacher" v-if="getSchedule(day.value, slot).teacherName">
                    <el-icon><User /></el-icon>
                    {{ getSchedule(day.value, slot).teacherName }}
                  </div>
                  <div class="class-location" v-if="getSchedule(day.value, slot).location">
                    <el-icon><Location /></el-icon>
                    {{ getSchedule(day.value, slot).location }}
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <el-empty v-if="scheduleList.length === 0 && !scheduleLoading" description="暂无课程安排" :image-size="100" />
      </div>
    </el-card>

    <!-- 课程详情弹框 -->
    <el-dialog v-model="detailVisible" title="课程详情" width="450px">
      <el-descriptions :column="1" border v-if="currentSchedule">
        <el-descriptions-item label="课程名称">
          <el-tag type="primary">{{ currentSchedule.courseName }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="授课教师">{{ currentSchedule.teacherName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="上课时间">
          {{ days.find(d => d.value === currentSchedule.dayOfWeek)?.label }} 
          {{ currentSchedule.startTime }} - {{ currentSchedule.endTime }}
        </el-descriptions-item>
        <el-descriptions-item label="上课地点">{{ currentSchedule.location || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="备注" v-if="currentSchedule.remark">
          <div style="white-space: pre-wrap;">{{ currentSchedule.remark }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 任务列表弹框 -->
    <el-dialog v-model="taskDialogVisible" :title="'课程任务 - ' + currentCourseName" width="750px" top="5vh">
      <el-table :data="taskList" border stripe v-loading="taskLoading" max-height="400">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="任务名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="description" label="任务描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="截止时间" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.deadline">{{ row.deadline }}</span>
            <el-tag v-else type="info" size="small">无截止</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="isExpired(row.deadline)" type="info" size="small">已截止</el-tag>
            <el-tag v-else type="success" size="small">进行中</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="taskList.length === 0 && !taskLoading" description="该课程暂无实训任务" />
      
      <!-- 评分权重图 -->
      <el-divider content-position="left">课程评分标准</el-divider>
      <div v-loading="indicatorLoading" style="min-height: 300px;">
        <div v-if="filteredIndicatorList.length > 0">
          <div ref="indicatorChartRef" style="width: 100%; height: 300px;"></div>
          <el-table :data="filteredIndicatorList" border stripe size="small" style="margin-top: 16px;">
            <el-table-column prop="name" label="指标名称" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="权重" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ row.defaultWeight }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">
                {{ getIndicatorPercent(row.defaultWeight) }}%
              </template>
            </el-table-column>
            <el-table-column prop="isSystem" label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isSystem === 1 ? 'danger' : 'success'" size="small">
                  {{ row.isSystem === 1 ? '系统' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else-if="!indicatorLoading" description="暂无评分标准" />
      </div>
      
      <template #footer>
        <el-button @click="taskDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import request from '../utils/request'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { User, Location } from '@element-plus/icons-vue'

const courseList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 任务弹框
const taskDialogVisible = ref(false)
const taskList = ref([])
const taskLoading = ref(false)
const currentCourseName = ref('')

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/my-courses/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    courseList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载课程失败:', e)
  } finally {
    loading.value = false
  }
}

const viewTasks = async (courseId, courseName) => {
  currentCourseName.value = courseName || ''
  taskDialogVisible.value = true
  taskLoading.value = true
  indicatorLoading.value = true
  try {
    const res = await request.get(`/task/course/${courseId}`)
    taskList.value = res.data || []
  } catch (e) {
    console.error('加载任务列表失败:', e)
    taskList.value = []
  } finally {
    taskLoading.value = false
  }
  
  // 加载评分标准
  try {
    const res = await request.get('/indicator/list')
    const allIndicators = res.data || []
    // 过滤掉权重为0的指标
    indicatorList.value = allIndicators.filter(item => Number(item.defaultWeight) > 0)
    await nextTick()
    renderIndicatorChart()
  } catch (e) {
    console.error('加载指标失败:', e)
    indicatorList.value = []
  } finally {
    indicatorLoading.value = false
  }
}

const isExpired = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

// 评分标准相关
const indicatorList = ref([])
const indicatorLoading = ref(false)
const indicatorChartRef = ref(null)
let indicatorChartInstance = null

const indicatorTotalWeight = ref(0)

// 过滤后的指标列表（权重>0）
const filteredIndicatorList = computed(() => {
  return indicatorList.value.filter(item => Number(item.defaultWeight) > 0)
})

const getIndicatorPercent = (weight) => {
  if (!indicatorTotalWeight.value || indicatorTotalWeight.value === 0) return '0.0'
  return ((weight / indicatorTotalWeight.value) * 100).toFixed(1)
}

const renderIndicatorChart = () => {
  if (!indicatorChartRef.value) return
  
  // 如果没有有效指标，销毁图表实例
  if (filteredIndicatorList.value.length === 0) {
    if (indicatorChartInstance) {
      indicatorChartInstance.dispose()
      indicatorChartInstance = null
    }
    return
  }
  
  if (indicatorChartInstance) {
    indicatorChartInstance.dispose()
  }
  
  indicatorChartInstance = echarts.init(indicatorChartRef.value)
  
  const data = filteredIndicatorList.value.map(item => ({
    name: item.name,
    value: item.defaultWeight
  }))
  
  indicatorTotalWeight.value = filteredIndicatorList.value.reduce((sum, item) => sum + (Number(item.defaultWeight) || 0), 0)
  
  indicatorChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [{
      name: '评价指标权重',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: true
      },
      data: data
    }]
  })
}



// 课程表相关
const scheduleLoading = ref(false)
const scheduleList = ref([])
const detailVisible = ref(false)
const currentSchedule = ref(null)

const days = [
  { value: 1, label: '周一' }, { value: 2, label: '周二' }, { value: 3, label: '周三' },
  { value: 4, label: '周四' }, { value: 5, label: '周五' }, { value: 6, label: '周六' }, { value: 7, label: '周日' }
]
const timeSlots = [
  '第1-2节 (08:00-09:40)', '第3-4节 (10:00-11:40)', '第5-6节 (14:00-15:40)',
  '第7-8节 (16:00-17:40)', '第9-10节 (19:00-20:40)'
]

const parseTimeSlot = (slot) => {
  const match = slot.match(/\((\d{2}:\d{2})-(\d{2}:\d{2})\)/)
  return match ? { start: match[1], end: match[2] } : null
}

const getSchedule = (dayOfWeek, timeSlot) => {
  const parsed = parseTimeSlot(timeSlot)
  if (!parsed) return null
  return scheduleList.value.find(s =>
    s.dayOfWeek === dayOfWeek &&
    s.startTime === parsed.start &&
    s.endTime === parsed.end
  )
}

const handleCellClick = (dayOfWeek, timeSlot) => {
  const schedule = getSchedule(dayOfWeek, timeSlot)
  if (schedule) {
    currentSchedule.value = schedule
    detailVisible.value = true
  }
}

const loadSchedule = async () => {
  scheduleLoading.value = true
  try {
    const res = await request.get('/schedule/list')
    scheduleList.value = res.data || []
  } catch (e) {
    console.error('加载课程表失败:', e)
  } finally {
    scheduleLoading.value = false
  }
}

onBeforeUnmount(() => {
  indicatorChartInstance?.dispose()
})

onMounted(() => {
  loadCourses()
  loadSchedule()
})
</script>

<style scoped>
.timetable-container { overflow-x: auto; }
.timetable { width: 100%; border-collapse: collapse; min-width: 800px; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #e4e7ed; padding: 4px; text-align: center; vertical-align: middle; }
.timetable th { background-color: #f5f7fa; font-weight: 600; color: #303133; height: 44px; }
.timetable tr { height: 90px; }
.timetable td { height: 90px; max-height: 90px; }
.timetable .time-col { width: 140px; background-color: #f5f7fa; font-size: 13px; color: #606266; }
.schedule-cell { height: 90px; max-height: 90px; padding: 4px; transition: background-color 0.2s; overflow: hidden; box-sizing: border-box; }
.schedule-cell.has-class { background-color: #ecf5ff; cursor: pointer; }
.schedule-cell.has-class:hover { background-color: #d9ecff; }
.class-info { display: flex; flex-direction: column; gap: 2px; font-size: 12px; line-height: 1.4; }
.course-name { font-weight: 600; color: #409eff; font-size: 14px; }
.class-time { color: #909399; font-size: 12px; }
.class-teacher { display: flex; align-items: center; justify-content: center; gap: 4px; color: #67c23a; font-size: 12px; }
.class-location { display: flex; align-items: center; justify-content: center; gap: 4px; color: #e6a23c; font-size: 12px; }
</style>
