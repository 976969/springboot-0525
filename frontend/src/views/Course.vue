<!--
  课程管理页面（课程CRUD、上下双区域布局、学生管理）
-->
<template>
  <div>
    <!-- 上半部分: 课程列表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>课程列表</span>
          <el-button type="primary" @click="openAdd()">新增课程</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="courseLoading">
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (pageNum - 1) * pageSize + i + 1" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="description" label="课程描述" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="openStudentManage(row)">学生管理</el-button>
            <el-button size="small" type="danger" @click="handleDeleteCourse(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadCourseList"
        @current-change="loadCourseList"
      />
    </el-card>

    <!-- 下半部分: 课程表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>课程表</span>
          <el-button type="primary" @click="openAddSchedule()">添加课程安排</el-button>
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
        <el-empty v-if="scheduleList.length === 0 && !scheduleLoading" description="暂无课程安排，点击上方按钮添加" />
      </div>
    </el-card>

    <!-- 课程对话框 -->
    <el-dialog v-model="courseDialogVisible" :title="isEditCourse ? '编辑课程' : '新增课程'" width="500px">
      <el-form :model="courseForm" label-width="80px">
        <el-form-item label="课程名称"><el-input v-model="courseForm.name" /></el-form-item>
        <el-form-item label="课程描述"><el-input v-model="courseForm.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="授课教师" v-if="userRole === 'admin'">
          <el-select v-model="courseForm.teacherId" placeholder="请选择授课教师" style="width: 100%">
            <el-option v-for="teacher in teacherList" :key="teacher.id" :label="teacher.realName" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" v-else>
          <el-input :value="courseForm.teacherName || '当前教师'" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCourse">确定</el-button>
      </template>
    </el-dialog>

    <!-- 课程表对话框 -->
    <el-dialog v-model="scheduleDialogVisible" :title="isViewSchedule ? '课程详情' : (isEditSchedule ? '编辑课程安排' : '添加课程安排')" width="550px">
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
      <el-form v-else :model="scheduleForm" label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="scheduleForm.courseId" placeholder="请选择课程" style="width: 100%" :disabled="isEditSchedule">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name" :value="c.id" />
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
          <el-input v-model="scheduleForm.remark" type="textarea" :rows="3" placeholder="如: 收作业、实验课、记得带学生证" />
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

    <!-- 学生管理对话框 -->
    <el-dialog v-model="studentDialogVisible" :title="`学生管理 - ${currentCourseName}`" width="800px" top="5vh">
      <div style="display: flex; gap: 20px;">
        <!-- 左侧：已选学生 -->
        <div style="flex: 1;">
          <div style="font-weight: bold; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
            <span>已选学生 ({{ courseStudentIds.size }}人)</span>
          </div>
          <div class="student-list-container" v-loading="studentLoading">
            <div v-for="group in courseStudentGroups" :key="group.className" style="margin-bottom: 12px;">
              <div class="class-header">{{ group.className || '未分班' }} ({{ group.students.length }}人)</div>
              <div v-for="s in group.students" :key="s.studentId" class="student-item">
                <span>{{ s.studentName }} ({{ s.studentUsername }})</span>
                <el-button size="small" type="danger" text @click="removeStudent(s.studentId)">移除</el-button>
              </div>
              <el-empty v-if="group.students.length === 0" description="无" :image-size="40" />
            </div>
            <el-empty v-if="courseStudentGroups.length === 0" description="暂无学生，请从右侧添加" :image-size="80" />
          </div>
        </div>
        <!-- 右侧：全部学生（按班级分组） -->
        <div style="flex: 1;">
          <div style="font-weight: bold; margin-bottom: 10px;">全部学生</div>
          <el-input v-model="studentSearch" placeholder="搜索姓名/学号" size="small" clearable style="margin-bottom: 10px;" />
          <div class="student-list-container">
            <div v-for="group in availableStudentGroups" :key="group.className" style="margin-bottom: 12px;">
              <div class="class-header">{{ group.className || '未分班' }}</div>
              <div v-for="s in group.students" :key="s.id" class="student-item">
                <span>{{ s.realName }} ({{ s.username }})</span>
                <el-button size="small" type="primary" text @click="addStudent(s.id)">添加</el-button>
              </div>
              <el-empty v-if="group.students.length === 0" description="无" :image-size="40" />
            </div>
            <el-empty v-if="availableStudentGroups.length === 0" description="无可用学生" :image-size="80" />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, InfoFilled } from '@element-plus/icons-vue'

// ============ 共用 ============
const userRole = ref('')

// ============ 课程列表 ============
const courseDialogVisible = ref(false)
const isEditCourse = ref(false)
const tableData = ref([])
const courseLoading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const teacherList = ref([])
const courseForm = reactive({ id: null, name: '', description: '', teacherId: '', teacherName: '' })

const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) { console.error('加载教师列表失败:', e) }
}

const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const res = await request.get('/course/page', { params: { pageNum: pageNum.value, pageSize: pageSize.value } })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { console.error('加载课程失败:', e) }
  finally { courseLoading.value = false }
}

const openAdd = () => {
  isEditCourse.value = false
  Object.assign(courseForm, { id: null, name: '', description: '', teacherId: '', teacherName: '' })
  courseDialogVisible.value = true
}

const openEdit = (row) => {
  isEditCourse.value = true
  Object.assign(courseForm, row)
  courseDialogVisible.value = true
}

const handleSubmitCourse = async () => {
  if (userRole.value === 'admin' && !courseForm.teacherId) {
    ElMessage.warning('请选择授课教师')
    return
  }
  try {
    if (isEditCourse.value) {
      await request.put('/course', courseForm)
      ElMessage.success('更新成功')
    } else {
      await request.post('/course', courseForm)
      ElMessage.success('新增成功')
    }
    courseDialogVisible.value = false
    loadCourseList()
    loadCourseOptions() // 刷新课程表下拉选项
  } catch (e) { console.error('操作失败:', e) }
}

const handleDeleteCourse = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该课程？', '提示', { type: 'warning' })
    await request.delete(`/course/${id}`)
    ElMessage.success('删除成功')
    loadCourseList()
    loadCourseOptions()
  } catch (e) { console.error('删除失败:', e) }
}

// ============ 课程表 ============
const scheduleDialogVisible = ref(false)
const isViewSchedule = ref(false)
const isEditSchedule = ref(false)
const scheduleList = ref([])
const scheduleLoading = ref(false)
const currentSchedule = ref(null)
const courseOptions = ref([])

const days = [
  { value: 1, label: '周一' }, { value: 2, label: '周二' }, { value: 3, label: '周三' },
  { value: 4, label: '周四' }, { value: 5, label: '周五' }, { value: 6, label: '周六' }, { value: 7, label: '周日' }
]
const timeSlots = [
  '第1-2节 (08:00-09:40)', '第3-4节 (10:00-11:40)', '第5-6节 (14:00-15:40)',
  '第7-8节 (16:00-17:40)', '第9-10节 (19:00-20:40)'
]

const scheduleForm = reactive({ id: null, courseId: '', dayOfWeek: '', startTime: '', endTime: '', location: '', remark: '' })

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

const loadScheduleList = async () => {
  scheduleLoading.value = true
  try {
    const res = await request.get('/schedule/list')
    scheduleList.value = res.data || []
  } catch (e) { console.error('加载课程表失败:', e) }
  finally { scheduleLoading.value = false }
}

const loadCourseOptions = async () => {
  try {
    const res = await request.get('/course/list')
    courseOptions.value = res.data || []
  } catch (e) { console.error('加载课程选项失败:', e) }
}

const openAddSchedule = (dayOfWeek = null, startTime = null, endTime = null) => {
  isEditSchedule.value = false
  isViewSchedule.value = false
  currentSchedule.value = null
  Object.assign(scheduleForm, {
    id: null, courseId: courseOptions.value[0]?.id || '', dayOfWeek: dayOfWeek || 1,
    startTime: startTime || '08:00', endTime: endTime || '09:40', location: '', remark: ''
  })
  scheduleDialogVisible.value = true
}

const editFromView = () => {
  if (currentSchedule.value) {
    isEditSchedule.value = true
    isViewSchedule.value = false
    Object.assign(scheduleForm, {
      id: currentSchedule.value.id, courseId: currentSchedule.value.courseId,
      dayOfWeek: currentSchedule.value.dayOfWeek, startTime: currentSchedule.value.startTime,
      endTime: currentSchedule.value.endTime, location: currentSchedule.value.location, remark: currentSchedule.value.remark
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
  } catch (e) { console.error('保存失败:', e); ElMessage.error('保存失败') }
}

const handleDeleteSchedule = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该课程安排？', '提示', { type: 'warning' })
    await request.delete(`/schedule/${id}`)
    ElMessage.success('删除成功')
    scheduleDialogVisible.value = false
    loadScheduleList()
  } catch (e) { if (e !== 'cancel') console.error('删除失败:', e) }
}

// ============ 学生管理 ============
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const currentCourseId = ref(null)
const currentCourseName = ref('')
const courseStudents = ref([])        // 已选学生列表
const allStudents = ref([])            // 全部学生列表
const studentSearch = ref('')          // 搜索关键词

// 已选学生ID集合（用于过滤可用学生）
const courseStudentIds = computed(() => new Set(courseStudents.value.map(s => s.studentId)))

// 已选学生按班级分组
const courseStudentGroups = computed(() => {
  const groups = {}
  courseStudents.value.forEach(s => {
    const key = s.className || ''
    if (!groups[key]) groups[key] = { className: key, students: [] }
    groups[key].students.push(s)
  })
  return Object.values(groups)
})

// 可用学生（未选）按班级分组，支持搜索
const availableStudentGroups = computed(() => {
  const keyword = studentSearch.value.trim().toLowerCase()
  const available = allStudents.value.filter(s => {
    if (courseStudentIds.value.has(s.id)) return false
    if (keyword && !s.realName?.toLowerCase().includes(keyword) && !s.username?.toLowerCase().includes(keyword)) return false
    return true
  })
  const groups = {}
  available.forEach(s => {
    const key = s.className || ''
    if (!groups[key]) groups[key] = { className: key, students: [] }
    groups[key].students.push(s)
  })
  return Object.values(groups)
})

const openStudentManage = async (row) => {
  currentCourseId.value = row.id
  currentCourseName.value = row.name
  studentDialogVisible.value = true
  studentLoading.value = true
  try {
    const [courseRes, allRes] = await Promise.all([
      request.get(`/user/course-students/${row.id}`),
      request.get('/user/student/list')
    ])
    courseStudents.value = courseRes.data || []
    allStudents.value = allRes.data || []
  } catch (e) {
    console.error('加载学生数据失败:', e)
  } finally {
    studentLoading.value = false
  }
}

const addStudent = async (studentId) => {
  try {
    await request.post('/user/course-student', { courseId: currentCourseId.value, studentId })
    ElMessage.success('添加成功')
    // 重新加载已选学生
    const res = await request.get(`/user/course-students/${currentCourseId.value}`)
    courseStudents.value = res.data || []
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const removeStudent = async (studentId) => {
  try {
    await request.delete('/user/course-student', { params: { courseId: currentCourseId.value, studentId } })
    ElMessage.success('移除成功')
    const res = await request.get(`/user/course-students/${currentCourseId.value}`)
    courseStudents.value = res.data || []
  } catch (e) {
    ElMessage.error('移除失败')
  }
}

onMounted(async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') loadTeachers()
  loadCourseList()
  loadCourseOptions()
  loadScheduleList()
})
</script>

<style scoped>
.timetable-container { overflow-x: auto; }
.timetable { width: 100%; border-collapse: collapse; min-width: 800px; }
.timetable th, .timetable td { border: 1px solid #e4e7ed; padding: 8px; text-align: center; vertical-align: middle; }
.timetable th { background-color: #f5f7fa; font-weight: 600; color: #303133; height: 50px; }
.timetable .time-col { width: 140px; background-color: #f5f7fa; font-size: 13px; color: #606266; }
.schedule-cell { min-height: 80px; cursor: pointer; transition: background-color 0.2s; padding: 6px; }
.schedule-cell:hover { background-color: #f5f7fa; }
.schedule-cell.has-class { background-color: #ecf5ff; cursor: pointer; }
.schedule-cell.has-class:hover { background-color: #d9ecff; }
.class-info { display: flex; flex-direction: column; gap: 4px; font-size: 13px; }
.course-name { font-weight: 600; color: #409eff; font-size: 14px; }
.class-time { color: #909399; font-size: 12px; }
.class-location { display: flex; align-items: center; justify-content: center; gap: 4px; color: #67c23a; font-size: 12px; }
.has-remark { color: #e6a23c; font-size: 12px; }
.remark-detail { padding: 10px 0; }
.student-list-container { max-height: 450px; overflow-y: auto; border: 1px solid #e4e7ed; border-radius: 8px; padding: 10px; }
.class-header { font-size: 13px; font-weight: 600; color: #409eff; margin-bottom: 6px; padding-bottom: 4px; border-bottom: 1px solid #ecf5ff; }
.student-item { display: flex; justify-content: space-between; align-items: center; padding: 4px 8px; border-radius: 4px; font-size: 13px; }
.student-item:hover { background-color: #f5f7fa; }
</style>
