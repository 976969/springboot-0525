<!--
  课程管理页面（管理员/教师共用，管理员视角更丰富）
-->
<template>
  <div>
    <!-- 顶部统计卡片（管理员专属） -->
    <div class="stat-row" v-if="userRole === 'admin'">
      <div class="stat-card stat-blue">
        <div class="stat-icon"><el-icon :size="28"><Reading /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ total }}</div>
          <div class="stat-label">课程总数</div>
        </div>
      </div>
      <div class="stat-card stat-green">
        <div class="stat-icon"><el-icon :size="28"><User /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ teacherList.length }}</div>
          <div class="stat-label">教师人数</div>
        </div>
      </div>
      <div class="stat-card stat-orange">
        <div class="stat-icon"><el-icon :size="28"><UserFilled /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ totalStudentCount }}</div>
          <div class="stat-label">选课总人次</div>
        </div>
      </div>
      <div class="stat-card stat-purple">
        <div class="stat-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-num">{{ tableData.filter(c => c.status === 1).length }}</div>
          <div class="stat-label">启用课程</div>
        </div>
      </div>
    </div>

    <!-- 课程列表 -->
    <el-card class="course-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon><Reading /></el-icon>
            <span>课程列表</span>
          </div>
          <div class="card-actions">
            <el-input v-if="userRole === 'admin'" v-model="courseSearch" placeholder="搜索课程" clearable class="filter-input" @input="debounceSearch">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-select v-if="userRole === 'admin'" v-model="filterTeacherId" placeholder="全部教师" clearable class="filter-select" @change="loadCourseList">
              <template #prefix><el-icon><User /></el-icon></template>
              <el-option v-for="t in teacherList" :key="t.id" :label="t.realName" :value="t.id" />
            </el-select>
            <el-button type="primary" @click="openAdd()">
              <el-icon><Plus /></el-icon> 新增课程
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe v-loading="courseLoading" :header-cell-style="{ background: '#fafafa', color: '#303133', fontWeight: 600 }">
        <el-table-column type="index" label="#" width="50" align="center" :index="(i) => (pageNum - 1) * pageSize + i + 1" />
        <el-table-column prop="name" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="course-name-cell">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="desc-cell">{{ row.description || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="teacherName" label="授课教师" width="110" align="center">
          <template #default="{ row }">
            <div class="teacher-cell" v-if="row.teacherName">
              <el-avatar :size="24" :src="row.teacherAvatar || ''" icon="UserFilled" />
              <span>{{ row.teacherName }}</span>
            </div>
            <el-tag v-else type="info" size="small" round>未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选课" width="80" align="center">
          <template #default="{ row }">
            <span class="count-cell">{{ row.studentCount ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" round>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button size="small" type="success" link @click="openStudentManage(row)">
              <el-icon><User /></el-icon> 学生
            </el-button>
            <el-popconfirm title="确定删除该课程？" @confirm="handleDeleteCourse(row.id)" width="200">
              <template #reference>
                <el-button size="small" type="danger" link>
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadCourseList"
          @current-change="loadCourseList"
        />
      </div>
    </el-card>

    <!-- 课程表 -->
    <el-card class="schedule-card">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon><Calendar /></el-icon>
            <span>课程表</span>
          </div>
          <div class="card-actions">
            <el-select v-if="userRole === 'admin'" v-model="scheduleTeacherFilter" placeholder="按教师筛选" clearable class="filter-select" @change="loadScheduleList">
              <template #prefix><el-icon><User /></el-icon></template>
              <el-option v-for="t in teacherList" :key="t.id" :label="t.realName" :value="t.id" />
            </el-select>
            <el-button type="primary" @click="openAddSchedule()">
              <el-icon><Plus /></el-icon> 添加安排
            </el-button>
          </div>
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
                  <div class="class-course-name">{{ getSchedule(day.value, slot).courseName }}</div>
                  <div class="class-teacher">
                    <el-icon :size="12"><User /></el-icon>
                    {{ getSchedule(day.value, slot).teacherName || '未分配教师' }}
                  </div>
                  <div class="class-time">{{ getSchedule(day.value, slot).startTime }} - {{ getSchedule(day.value, slot).endTime }}</div>
                  <div class="class-location" v-if="getSchedule(day.value, slot).location">
                    <el-icon :size="12"><Location /></el-icon>
                    {{ getSchedule(day.value, slot).location }}
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <el-empty v-if="scheduleList.length === 0 && !scheduleLoading" description="暂无课程安排，点击上方按钮添加" :image-size="80" />
      </div>
    </el-card>

    <!-- 课程对话框 -->
    <el-dialog v-model="courseDialogVisible" :title="isEditCourse ? '编辑课程' : '新增课程'" width="500px" destroy-on-close>
      <el-form :model="courseForm" label-width="80px">
        <el-form-item label="课程名称">
          <el-input v-model="courseForm.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程描述">
          <el-input v-model="courseForm.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
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
    <el-dialog v-model="scheduleDialogVisible" :title="isViewSchedule ? '课程详情' : (isEditSchedule ? '编辑课程安排' : '添加课程安排')" width="550px" destroy-on-close>
      <div v-if="isViewSchedule" class="schedule-detail">
        <div class="detail-header">
          <div class="detail-course-name">{{ currentSchedule?.courseName }}</div>
          <el-tag type="primary" round size="small">{{ days.find(d => d.value === currentSchedule?.dayOfWeek)?.label }}</el-tag>
        </div>
        <div class="detail-body">
          <div class="detail-item">
            <el-icon class="detail-icon"><User /></el-icon>
            <div class="detail-content">
              <div class="detail-label">授课教师</div>
              <div class="detail-value">{{ currentSchedule?.teacherName || '未分配' }}</div>
            </div>
          </div>
          <div class="detail-item">
            <el-icon class="detail-icon"><Clock /></el-icon>
            <div class="detail-content">
              <div class="detail-label">上课时间</div>
              <div class="detail-value">{{ currentSchedule?.startTime }} - {{ currentSchedule?.endTime }}</div>
            </div>
          </div>
          <div class="detail-item">
            <el-icon class="detail-icon"><Location /></el-icon>
            <div class="detail-content">
              <div class="detail-label">上课地点</div>
              <div class="detail-value">{{ currentSchedule?.location || '未设置' }}</div>
            </div>
          </div>
          <div class="detail-item" v-if="currentSchedule?.remark">
            <el-icon class="detail-icon"><InfoFilled /></el-icon>
            <div class="detail-content">
              <div class="detail-label">备注</div>
              <div class="detail-value" style="white-space: pre-wrap;">{{ currentSchedule.remark }}</div>
            </div>
          </div>
        </div>
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
          <el-popconfirm title="确定删除该安排？" @confirm="handleDeleteSchedule(currentSchedule?.id)" width="200">
            <template #reference>
              <el-button type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
        <template v-else>
          <el-button @click="scheduleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitSchedule">{{ isEditSchedule ? '更新' : '添加' }}</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog v-model="studentDialogVisible" width="850px" top="5vh" destroy-on-close>
      <template #header>
        <div class="student-dialog-header">
          <div class="student-dialog-title">
            <el-icon :size="20"><User /></el-icon>
            <span>学生管理</span>
          </div>
          <div class="student-dialog-course">{{ currentCourseName }}</div>
        </div>
      </template>
      <div class="student-transfer">
        <!-- 左侧：已选学生 -->
        <div class="transfer-panel">
          <div class="panel-header panel-header-green">
            <span>已选学生</span>
            <el-tag type="success" round size="small">{{ courseStudentIds.size }} 人</el-tag>
          </div>
          <div class="panel-body" v-loading="studentLoading">
            <div v-for="group in courseStudentGroups" :key="group.className" class="student-group">
              <div class="group-title">{{ group.className || '未分班' }} <span class="group-count">{{ group.students.length }}人</span></div>
              <div v-for="s in group.students" :key="s.studentId" class="student-row">
                <div class="student-info">
                  <el-avatar :size="28" :src="s.studentAvatar || ''" icon="UserFilled" />
                  <span class="student-name">{{ s.studentName }}</span>
                  <span class="student-username">{{ s.studentUsername }}</span>
                </div>
                <el-popconfirm title="确定移除该学生？" @confirm="removeStudent(s.studentId)" width="180">
                  <template #reference>
                    <el-button type="danger" size="small" link>移除</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
            <el-empty v-if="courseStudentGroups.length === 0" description="暂无学生" :image-size="60" />
          </div>
        </div>
        <!-- 中间箭头 -->
        <div class="transfer-arrow">
          <el-icon :size="24"><Right /></el-icon>
        </div>
        <!-- 右侧：全部学生 -->
        <div class="transfer-panel">
          <div class="panel-header panel-header-blue">
            <span>可选学生</span>
            <el-tag type="primary" round size="small">{{ availableStudentGroups.reduce((sum, g) => sum + g.students.length, 0) }} 人</el-tag>
          </div>
          <div class="panel-search">
            <el-input v-model="studentSearch" placeholder="搜索姓名/学号" clearable size="small">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
          </div>
          <div class="panel-body">
            <div v-for="group in availableStudentGroups" :key="group.className" class="student-group">
              <div class="group-title">{{ group.className || '未分班' }} <span class="group-count">{{ group.students.length }}人</span></div>
              <div v-for="s in group.students" :key="s.id" class="student-row">
                <div class="student-info">
                  <el-avatar :size="28" :src="s.avatar || ''" icon="UserFilled" />
                  <span class="student-name">{{ s.realName }}</span>
                  <span class="student-username">{{ s.username }}</span>
                </div>
                <el-button type="primary" size="small" link @click="addStudent(s.id)">添加</el-button>
              </div>
            </div>
            <el-empty v-if="availableStudentGroups.length === 0" description="无可选学生" :image-size="60" />
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
import {
  Location, InfoFilled, Reading, User, UserFilled, CircleCheck,
  Search, Plus, Edit, Delete, Calendar, Right, Clock
} from '@element-plus/icons-vue'

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
const courseSearch = ref('')
const filterTeacherId = ref('')
const courseForm = reactive({ id: null, name: '', description: '', teacherId: '', teacherName: '' })

// 统计
const totalStudentCount = computed(() => tableData.value.reduce((sum, c) => sum + (c.studentCount ?? 0), 0))

let searchTimer = null
const debounceSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadCourseList(), 300)
}

const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) { console.error('加载教师列表失败:', e) }
}

const loadCourseList = async () => {
  courseLoading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (courseSearch.value) params.keyword = courseSearch.value
    if (filterTeacherId.value) params.teacherId = filterTeacherId.value
    const res = await request.get('/course/page', { params })
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
    loadCourseOptions()
  } catch (e) { console.error('操作失败:', e) }
}

const handleDeleteCourse = async (id) => {
  try {
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
const scheduleTeacherFilter = ref('')

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
    const params = {}
    if (scheduleTeacherFilter.value) params.teacherId = scheduleTeacherFilter.value
    const res = await request.get('/schedule/list', { params })
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
    await request.delete(`/schedule/${id}`)
    ElMessage.success('删除成功')
    scheduleDialogVisible.value = false
    loadScheduleList()
  } catch (e) { console.error('删除失败:', e) }
}

// ============ 学生管理 ============
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const currentCourseId = ref(null)
const currentCourseName = ref('')
const courseStudents = ref([])
const allStudents = ref([])
const studentSearch = ref('')

const courseStudentIds = computed(() => new Set(courseStudents.value.map(s => s.studentId)))

const courseStudentGroups = computed(() => {
  const groups = {}
  courseStudents.value.forEach(s => {
    const key = s.className || ''
    if (!groups[key]) groups[key] = { className: key, students: [] }
    groups[key].students.push(s)
  })
  return Object.values(groups)
})

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
  studentSearch.value = ''
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
/* ============ 统计卡片 ============ */
.stat-row { display: flex; gap: 16px; margin-bottom: 20px; }
.stat-card {
  flex: 1; display: flex; align-items: center; gap: 14px;
  padding: 18px 20px; border-radius: 10px; color: #fff;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.stat-blue { background: linear-gradient(135deg, #409eff, #66b1ff); }
.stat-green { background: linear-gradient(135deg, #67c23a, #85ce61); }
.stat-orange { background: linear-gradient(135deg, #e6a23c, #f0c78a); }
.stat-purple { background: linear-gradient(135deg, #9254de, #b37feb); }
.stat-icon { opacity: 0.85; }
.stat-num { font-size: 26px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; opacity: 0.85; margin-top: 2px; }

/* ============ 卡片头部 ============ */
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { display: flex; align-items: center; gap: 6px; font-size: 16px; font-weight: 600; }
.card-actions { display: flex; gap: 10px; align-items: center; }

/* 筛选下拉框样式 */
.filter-select {
  width: 160px;
}
.filter-select :deep(.el-select__wrapper) {
  border-radius: 8px;
  background: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid #e4e7ed;
  padding: 4px 12px;
  transition: all 0.2s;
}
.filter-select :deep(.el-select__wrapper:hover) {
  border-color: #409eff;
  background: #fff;
}
.filter-select :deep(.el-select__wrapper.is-focused) {
  background: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1) !important;
}
.filter-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #f5f7fa;
  box-shadow: none !important;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}
.filter-input :deep(.el-input__wrapper:hover) {
  border-color: #409eff;
  background: #fff;
}
.filter-input :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1) !important;
}

/* ============ 表格内容 ============ */
.course-name-cell { font-weight: 600; color: #303133; }
.desc-cell { color: #909399; font-size: 13px; }
.teacher-cell { display: flex; align-items: center; gap: 6px; justify-content: center; }
.count-cell {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 32px; height: 24px; border-radius: 12px;
  background: #ecf5ff; color: #409eff; font-weight: 600; font-size: 13px;
}
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }

/* ============ 课程表 ============ */
.schedule-card { margin-top: 20px; }
.timetable-container { overflow-x: auto; }
.timetable { width: 100%; border-collapse: collapse; min-width: 900px; table-layout: fixed; }
.timetable th, .timetable td { border: 1px solid #e4e7ed; padding: 4px; text-align: center; vertical-align: middle; }
.timetable th { background-color: #f5f7fa; font-weight: 600; color: #303133; height: 44px; }
.timetable tr { height: 90px; }
.timetable td { height: 90px; max-height: 90px; }
.timetable .time-col { width: 150px; min-width: 150px; background-color: #f5f7fa; font-size: 13px; color: #606266; }
.timetable th:not(.time-col) { width: calc((100% - 150px) / 7); }
.schedule-cell { height: 90px; max-height: 90px; cursor: pointer; transition: background-color 0.2s; padding: 4px; overflow: hidden; box-sizing: border-box; }
.schedule-cell:hover { background-color: #f5f7fa; }
.schedule-cell.has-class { background-color: #ecf5ff; cursor: pointer; }
.schedule-cell.has-class:hover { background-color: #d9ecff; }
.class-info { display: flex; flex-direction: column; gap: 2px; font-size: 12px; line-height: 1.4; }
.class-course-name { font-weight: 600; color: #409eff; font-size: 14px; }
.class-teacher { display: flex; align-items: center; gap: 3px; color: #67c23a; font-size: 12px; }
.class-time { color: #909399; font-size: 12px; }
.class-location { display: flex; align-items: center; justify-content: center; gap: 3px; color: #909399; font-size: 12px; }

/* ============ 课程详情对话框 ============ */
.schedule-detail { padding: 4px 0; }
.detail-header {
  display: flex; align-items: center; gap: 12px;
  padding-bottom: 16px; margin-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}
.detail-course-name { font-size: 20px; font-weight: 700; color: #303133; }
.detail-body { display: flex; flex-direction: column; gap: 16px; }
.detail-item { display: flex; align-items: flex-start; gap: 12px; }
.detail-icon { color: #409eff; margin-top: 2px; flex-shrink: 0; }
.detail-content { flex: 1; }
.detail-label { font-size: 12px; color: #909399; margin-bottom: 2px; }
.detail-value { font-size: 15px; color: #303133; font-weight: 500; }

/* ============ 学生管理对话框 ============ */
.student-dialog-header { display: flex; align-items: center; gap: 12px; }
.student-dialog-title { display: flex; align-items: center; gap: 6px; font-size: 16px; font-weight: 600; }
.student-dialog-course {
  padding: 3px 12px; border-radius: 12px;
  background: #ecf5ff; color: #409eff; font-size: 13px; font-weight: 500;
}
.student-transfer { display: flex; gap: 0; align-items: stretch; }
.transfer-panel { flex: 1; border: 1px solid #e4e7ed; border-radius: 8px; overflow: hidden; }
.transfer-arrow { display: flex; align-items: center; padding: 0 16px; color: #c0c4cc; }
.panel-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 14px; font-weight: 600; font-size: 14px;
}
.panel-header-green { background: #f0f9eb; color: #67c23a; border-bottom: 1px solid #e1f3d8; }
.panel-header-blue { background: #ecf5ff; color: #409eff; border-bottom: 1px solid #d9ecff; }
.panel-search { padding: 10px 12px; border-bottom: 1px solid #f0f0f0; }
.panel-body { max-height: 400px; overflow-y: auto; padding: 8px 12px; }
.student-group { margin-bottom: 10px; }
.group-title {
  font-size: 12px; font-weight: 600; color: #909399;
  margin-bottom: 4px; padding-bottom: 4px; border-bottom: 1px dashed #ebeef5;
}
.group-count { font-weight: 400; color: #c0c4cc; margin-left: 4px; }
.student-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 6px 8px; border-radius: 6px; transition: background 0.15s;
}
.student-row:hover { background: #f5f7fa; }
.student-info { display: flex; align-items: center; gap: 8px; }
.student-name { font-size: 14px; color: #303133; font-weight: 500; }
.student-username { font-size: 12px; color: #c0c4cc; }
</style>
