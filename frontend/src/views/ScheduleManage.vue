<!--
  管理员端课程表管理（选择教师/学生、查看课表、增删改）
-->
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>选课管理</span>
          <el-button type="primary" @click="openAddSchedule" :disabled="!selectedUser">添加课程安排</el-button>
        </div>
      </template>

      <!-- 顶部选择器 -->
      <div class="selector-area">
        <div class="selector-row">
          <span class="selector-label">查看身份：</span>
          <el-radio-group v-model="viewRole" @change="handleRoleChange" size="default">
            <el-radio-button value="teacher">教师</el-radio-button>
            <el-radio-button value="student">学生</el-radio-button>
          </el-radio-group>
        </div>
        <div class="selector-row">
          <span class="selector-label">选择{{ viewRole === 'teacher' ? '教师' : '学生' }}：</span>
          <el-select
            v-model="selectedUser"
            :placeholder="`请选择${viewRole === 'teacher' ? '教师' : '学生'}`"
            filterable
            style="width: 280px;"
            @change="loadScheduleList"
          >
            <el-option
              v-for="u in userList"
              :key="u.id"
              :label="`${u.realName}（${u.username}）`"
              :value="u.id"
            />
          </el-select>
          <el-tag v-if="selectedUser" type="info" style="margin-left: 12px;">
            共 {{ scheduleList.length }} 条课程安排
          </el-tag>
        </div>
      </div>

      <!-- 课程表 -->
      <div class="timetable-container" v-loading="scheduleLoading">
        <table class="timetable" v-if="selectedUser">
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
                  <div class="class-location" v-if="getSchedule(day.value, slot).location">
                    <el-icon><Location /></el-icon>
                    {{ getSchedule(day.value, slot).location }}
                  </div>
                  <div class="has-remark" v-if="getSchedule(day.value, slot).remark">
                    <el-icon><InfoFilled /></el-icon>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <el-empty v-if="!selectedUser" description="请先选择一位教师或学生查看课程表" :image-size="100" />
        <el-empty v-if="selectedUser && scheduleList.length === 0 && !scheduleLoading" description="该用户暂无课程安排" :image-size="80" />
      </div>
    </el-card>

    <!-- 课程安排对话框 -->
    <el-dialog v-model="scheduleDialogVisible" :title="isViewSchedule ? '课程详情' : (isEditSchedule ? '编辑课程安排' : '添加课程安排')" width="550px">
      <!-- 查看详情 -->
      <div v-if="isViewSchedule" class="remark-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程">{{ currentSchedule?.courseName }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ days.find(d => d.value === currentSchedule?.dayOfWeek)?.label }} {{ currentSchedule?.startTime }} - {{ currentSchedule?.endTime }}</el-descriptions-item>
          <el-descriptions-item label="地点">{{ currentSchedule?.location || '未设置' }}</el-descriptions-item>
          <el-descriptions-item label="备注">
            <div v-if="currentSchedule?.remark" style="white-space: pre-wrap;">{{ currentSchedule.remark }}</div>
            <el-text v-else type="info">暂无备注</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <!-- 编辑/添加表单 -->
      <el-form v-else :model="scheduleForm" label-width="80px">
        <!-- 学生模式：先选课程名，再选教师 -->
        <template v-if="viewRole === 'student'">
          <el-form-item label="选择课程">
            <el-select v-model="selectedCourseName" placeholder="请选择课程" style="width: 100%" @change="handleCourseNameChange" :disabled="isEditSchedule">
              <el-option v-for="name in uniqueCourseNames" :key="name" :label="name" :value="name" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择教师" v-if="selectedCourseName">
            <el-select v-model="scheduleForm.courseId" placeholder="请选择教师班次" style="width: 100%" :disabled="isEditSchedule">
              <el-option v-for="c in courseSectionsByTeacher" :key="c.id" :label="c.teacherName || '未知教师'" :value="c.id" />
            </el-select>
          </el-form-item>
        </template>
        <!-- 教师模式：直接选课程（含教师） -->
        <el-form-item v-else label="课程">
          <el-select v-model="scheduleForm.courseId" placeholder="请选择课程" style="width: 100%" :disabled="isEditSchedule">
            <el-option v-for="c in filteredCourseOptions" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="星期">
          <el-select v-model="scheduleForm.dayOfWeek" placeholder="请选择" style="width: 100%">
            <el-option v-for="day in days" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="scheduleForm.startTime" format="HH:mm" value-format="HH:mm" placeholder="选择开始时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="scheduleForm.endTime" format="HH:mm" value-format="HH:mm" placeholder="选择结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="地点">
          <el-input v-model="scheduleForm.location" placeholder="如: 教学楼A-301" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="3" placeholder="如: 收作业、实验课" />
        </el-form-item>
      </el-form>
      <template #footer>
        <template v-if="isViewSchedule">
          <el-button @click="scheduleDialogVisible = false">关闭</el-button>
          <el-button type="warning" @click="editFromView">编辑</el-button>
          <el-button type="danger" @click="handleDeleteSchedule(currentSchedule?.id)">删除</el-button>
        </template>
        <template v-else>
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSchedule">{{ isEditSchedule ? '更新' : '添加' }}</el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, InfoFilled } from '@element-plus/icons-vue'

// ============ 选择器 ============
const viewRole = ref('teacher')
const selectedUser = ref(null)
const userList = ref([])
const teacherList = ref([])
const studentList = ref([])

const days = [
  { value: 1, label: '周一' }, { value: 2, label: '周二' }, { value: 3, label: '周三' },
  { value: 4, label: '周四' }, { value: 5, label: '周五' }, { value: 6, label: '周六' }, { value: 7, label: '周日' }
]
const timeSlots = [
  '第1-2节 (08:00-09:40)', '第3-4节 (10:00-11:40)', '第5-6节 (14:00-15:40)',
  '第7-8节 (16:00-17:40)', '第9-10节 (19:00-20:40)'
]

// ============ 课程表 ============
const scheduleList = ref([])
const scheduleLoading = ref(false)
const allCourses = ref([]) // 所有课程（含教师信息）

// 根据选中用户过滤课程选项
const filteredCourseOptions = computed(() => {
  if (!selectedUser.value) return allCourses.value
  if (viewRole.value === 'teacher') {
    // 选中教师时，只显示该教师的课程
    return allCourses.value.filter(c => c.teacherId === selectedUser.value)
  }
  // 选中学生时，显示所有课程（管理员可任选）
  return allCourses.value
})

// 学生模式：去重后的课程名列表
const uniqueCourseNames = computed(() => {
  const names = new Set()
  allCourses.value.forEach(c => names.add(c.name))
  return [...names]
})

// 学生模式：根据选中的课程名，过滤出该课程的教师班次
const courseSectionsByTeacher = computed(() => {
  if (!selectedCourseName.value) return []
  return allCourses.value.filter(c => c.name === selectedCourseName.value)
})

// 学生模式：选中课程名时，重置 courseId
const selectedCourseName = ref('')
const handleCourseNameChange = () => {
  scheduleForm.courseId = ''
}

// ============ 对话框 ============
const scheduleDialogVisible = ref(false)
const isViewSchedule = ref(false)
const isEditSchedule = ref(false)
const currentSchedule = ref(null)
const scheduleForm = reactive({ id: null, courseId: '', dayOfWeek: '', startTime: '', endTime: '', location: '', remark: '' })

// 加载教师列表
const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) { console.error('加载教师列表失败:', e) }
}

// 加载学生列表
const loadStudents = async () => {
  try {
    const res = await request.get('/user/student/list')
    studentList.value = res.data || []
  } catch (e) { console.error('加载学生列表失败:', e) }
}

// 切换角色时重置选择
const handleRoleChange = () => {
  selectedUser.value = null
  scheduleList.value = []
  if (viewRole.value === 'teacher') {
    userList.value = teacherList.value
  } else {
    userList.value = studentList.value
  }
}

// 加载课程表
const loadScheduleList = async () => {
  if (!selectedUser.value) {
    scheduleList.value = []
    return
  }
  scheduleLoading.value = true
  try {
    const res = await request.get('/schedule/user', {
      params: { role: viewRole.value, userId: selectedUser.value }
    })
    scheduleList.value = res.data || []
  } catch (e) {
    console.error('加载课程表失败:', e)
  } finally {
    scheduleLoading.value = false
  }
}

// 加载课程选项（附带教师名）
const loadCourseOptions = async () => {
  try {
    const res = await request.get('/course/list')
    allCourses.value = (res.data || []).map(c => ({
      ...c,
      label: `${c.name}（${c.teacherName || '未知教师'}）`
    }))
  } catch (e) { console.error('加载课程选项失败:', e) }
}

// ============ 课程表操作 ============
const parseTimeSlot = (slot) => {
  const match = slot.match(/\((\d{2}:\d{2})-(\d{2}:\d{2})\)/)
  return match ? { start: match[1], end: match[2] } : null
}

const getSchedule = (dayOfWeek, timeSlot) => {
  const parsed = parseTimeSlot(timeSlot)
  if (!parsed) return null
  return scheduleList.value.find(s => s.dayOfWeek === dayOfWeek && s.startTime === parsed.start && s.endTime === parsed.end)
}

const handleCellClick = (dayOfWeek, timeSlot) => {
  const schedule = getSchedule(dayOfWeek, timeSlot)
  if (schedule) {
    currentSchedule.value = schedule
    isViewSchedule.value = true
    isEditSchedule.value = false
    scheduleDialogVisible.value = true
  } else {
    const parsed = parseTimeSlot(timeSlot)
    openAddSchedule(dayOfWeek, parsed?.start, parsed?.end)
  }
}

const openAddSchedule = (dayOfWeek = null, startTime = null, endTime = null) => {
  if (!selectedUser.value) {
    ElMessage.warning('请先选择一位教师或学生')
    return
  }
  const available = filteredCourseOptions.value
  if (viewRole.value === 'teacher' && available.length === 0) {
    ElMessage.warning('该教师暂无负责的课程，请先在课程管理中为该教师创建课程')
    return
  }
  isEditSchedule.value = false
  isViewSchedule.value = false
  currentSchedule.value = null
  // 学生模式：重置课程名选择，不自动选课程
  if (viewRole.value === 'student') {
    selectedCourseName.value = ''
    Object.assign(scheduleForm, {
      id: null,
      courseId: '',
      dayOfWeek: dayOfWeek || 1,
      startTime: startTime || '08:00',
      endTime: endTime || '09:40',
      location: '',
      remark: ''
    })
  } else {
    // 教师模式：自动选择第一个可用课程
    Object.assign(scheduleForm, {
      id: null,
      courseId: available[0]?.id || '',
      dayOfWeek: dayOfWeek || 1,
      startTime: startTime || '08:00',
      endTime: endTime || '09:40',
      location: '',
      remark: ''
    })
  }
  scheduleDialogVisible.value = true
}

const editFromView = () => {
  if (currentSchedule.value) {
    isEditSchedule.value = true
    isViewSchedule.value = false
    Object.assign(scheduleForm, {
      id: currentSchedule.value.id,
      courseId: currentSchedule.value.courseId,
      dayOfWeek: currentSchedule.value.dayOfWeek,
      startTime: currentSchedule.value.startTime,
      endTime: currentSchedule.value.endTime,
      location: currentSchedule.value.location,
      remark: currentSchedule.value.remark
    })
  }
}

const handleSubmitSchedule = async () => {
  if (!scheduleForm.courseId) { ElMessage.warning('请选择课程'); return }
  if (!scheduleForm.dayOfWeek) { ElMessage.warning('请选择星期'); return }
  if (!scheduleForm.startTime || !scheduleForm.endTime) { ElMessage.warning('请选择上课时间'); return }
  try {
    if (isEditSchedule.value) {
      await request.put('/schedule', scheduleForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/schedule', scheduleForm)
      ElMessage.success('添加成功')
    }
    scheduleDialogVisible.value = false
    loadScheduleList()
  } catch (e) {
    console.error('保存失败:', e)
    ElMessage.error('保存失败')
  }
}

const handleDeleteSchedule = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该课程安排？', '提示', { type: 'warning' })
    await request.delete(`/schedule/${id}`)
    ElMessage.success('删除成功')
    scheduleDialogVisible.value = false
    loadScheduleList()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
  }
}

onMounted(async () => {
  await Promise.all([loadTeachers(), loadStudents(), loadCourseOptions()])
  userList.value = teacherList.value
})
</script>

<style scoped>
.selector-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 8px;
}
.selector-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.selector-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  min-width: 90px;
}
.timetable-container { overflow-x: auto; }
.timetable { width: 100%; border-collapse: collapse; min-width: 800px; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #e4e7ed; padding: 4px; text-align: center; vertical-align: middle; }
.timetable th { background-color: #f5f7fa; font-weight: 600; color: #303133; height: 44px; }
.timetable tr { height: 90px; }
.timetable td { height: 90px; max-height: 90px; }
.timetable .time-col { width: 140px; background-color: #f5f7fa; font-size: 13px; color: #606266; }
.schedule-cell { height: 90px; max-height: 90px; cursor: pointer; transition: background-color 0.2s; padding: 4px; overflow: hidden; box-sizing: border-box; }
.schedule-cell:hover { background-color: #f5f7fa; }
.schedule-cell.has-class { background-color: #ecf5ff; cursor: pointer; }
.schedule-cell.has-class:hover { background-color: #d9ecff; }
.class-info { display: flex; flex-direction: column; gap: 2px; font-size: 12px; line-height: 1.4; }
.course-name { font-weight: 600; color: #409eff; font-size: 14px; }
.class-time { color: #909399; font-size: 12px; }
.class-location { display: flex; align-items: center; justify-content: center; gap: 4px; color: #67c23a; font-size: 12px; }
.has-remark { color: #e6a23c; font-size: 12px; }
.remark-detail { padding: 10px 0; }
</style>
