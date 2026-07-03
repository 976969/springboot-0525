<!--
  学生端我的课程表（只读展示、显示已选课程的上课时间）
-->
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的课程表</span>
          <el-tag type="info" size="small">共 {{ scheduleList.length }} 门课程安排</el-tag>
        </div>
      </template>
      <div class="timetable-container" v-loading="loading">
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
        <el-empty v-if="scheduleList.length === 0 && !loading" description="暂无课程安排" :image-size="100" />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { User, Location } from '@element-plus/icons-vue'

const loading = ref(false)
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
  loading.value = true
  try {
    const res = await request.get('/schedule/list')
    scheduleList.value = res.data || []
  } catch (e) {
    console.error('加载课程表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadSchedule)
</script>

<style scoped>
.timetable-container { overflow-x: auto; }
.timetable { width: 100%; border-collapse: collapse; min-width: 800px; }
.timetable th, .timetable td { border: 1px solid #e4e7ed; padding: 8px; text-align: center; vertical-align: middle; }
.timetable th { background-color: #f5f7fa; font-weight: 600; color: #303133; height: 50px; }
.timetable .time-col { width: 140px; background-color: #f5f7fa; font-size: 13px; color: #606266; }
.schedule-cell { min-height: 80px; padding: 6px; transition: background-color 0.2s; }
.schedule-cell.has-class { background-color: #ecf5ff; cursor: pointer; }
.schedule-cell.has-class:hover { background-color: #d9ecff; }
.class-info { display: flex; flex-direction: column; gap: 4px; font-size: 13px; }
.course-name { font-weight: 600; color: #409eff; font-size: 14px; }
.class-time { color: #909399; font-size: 12px; }
.class-teacher { display: flex; align-items: center; justify-content: center; gap: 4px; color: #67c23a; font-size: 12px; }
.class-location { display: flex; align-items: center; justify-content: center; gap: 4px; color: #e6a23c; font-size: 12px; }
</style>
