<!--
  学生端选课中心（课程概览 + 教师班次选择）
-->
<template>
  <div>
    <!-- 顶部统计概览 -->
    <el-row :gutter="16" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ availableGroups.length }}</div>
            <div class="stat-label">可选课程</div>
          </div>
          <el-icon class="stat-icon" style="color: #409eff;"><Reading /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ totalTeachers }}</div>
            <div class="stat-label">授课教师</div>
          </div>
          <el-icon class="stat-icon" style="color: #67c23a;"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ enrolledCount }}</div>
            <div class="stat-label">已选课程</div>
          </div>
          <el-icon class="stat-icon" style="color: #e6a23c;"><Check /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-info">
            <div class="stat-value">{{ availableGroups.length - enrolledCount }}</div>
            <div class="stat-label">待选课程</div>
          </div>
          <el-icon class="stat-icon" style="color: #f56c6c;"><Bell /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>选课中心</span>
          <div style="display: flex; gap: 12px; align-items: center;">
            <el-radio-group v-model="filterType" size="small">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="available">可选</el-radio-button>
              <el-radio-button value="enrolled">已选</el-radio-button>
            </el-radio-group>
            <el-input v-model="searchText" placeholder="搜索课程名称..." prefix-icon="Search" clearable style="width: 240px;" />
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <!-- 按课程名分组展示 -->
        <div v-for="group in filteredGroups" :key="group.courseName" class="course-group">
          <!-- 课程头部 -->
          <div class="group-header" @click="toggleExpand(group.courseName)">
            <el-icon class="expand-icon" :class="{ expanded: expandedGroups.has(group.courseName) }"><ArrowRight /></el-icon>
            <div class="group-title-area">
              <span class="group-name">{{ group.courseName }}</span>
              <span class="group-desc" v-if="group.description">{{ group.description }}</span>
            </div>
            <div class="group-tags">
              <el-tag size="small" type="info">{{ group.teacherCount }} 位教师</el-tag>
              <el-tag size="small" type="success" v-if="group.scheduleInfo.length > 0">{{ group.scheduleInfo.length }} 节课/周</el-tag>
              <el-tag size="small" :type="isGroupFullyEnrolled(group) ? 'success' : 'warning'">
                {{ isGroupFullyEnrolled(group) ? '已选' : '未选' }}
              </el-tag>
            </div>
          </div>

          <!-- 展开的课程详情 -->
          <div v-show="expandedGroups.has(group.courseName)" class="group-detail">
            <!-- 上课时间总览 -->
            <div v-if="group.scheduleInfo.length > 0" class="schedule-overview">
              <div class="overview-title">
                <el-icon><Clock /></el-icon> 上课安排
              </div>
              <div class="schedule-tags">
                <div v-for="(info, idx) in group.scheduleInfo" :key="idx" class="schedule-tag-item">
                  <el-tag size="small" effect="plain">{{ dayLabels[info.dayOfWeek] || '未知' }}</el-tag>
                  <span class="schedule-time">{{ info.startTime }}-{{ info.endTime }}</span>
                  <span class="schedule-location" v-if="info.location">
                    <el-icon><Location /></el-icon> {{ info.location }}
                  </span>
                  <span class="schedule-teacher" v-if="info.teacherName">
                    <el-icon><User /></el-icon> {{ info.teacherName }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 教师班次选择 -->
            <div class="section-list">
              <div class="section-list-title">
                <el-icon><User /></el-icon> 选择教师班次
              </div>
              <div v-for="section in group.sections" :key="section.id" class="section-card" :class="{ 'enrolled': isEnrolled(section.id) }">
                <div class="section-info">
                  <div class="teacher-name">
                    <el-icon><User /></el-icon>
                    {{ section.teacherName || '未知教师' }}
                  </div>
                  <div class="section-desc" v-if="section.description">{{ section.description }}</div>
                </div>
                <div class="section-action">
                  <el-tag v-if="isEnrolled(section.id)" type="success" size="default">
                    <el-icon><Check /></el-icon> 已选
                  </el-tag>
                  <el-button v-else type="primary" size="small" @click="handleEnroll(section)">选课</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="filteredGroups.length === 0 && !loading" description="暂无可选课程" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, User, Check, Bell, ArrowRight, Clock, Location } from '@element-plus/icons-vue'

const loading = ref(false)
const searchText = ref('')
const filterType = ref('all') // all / available / enrolled
const availableGroups = ref([])
const enrolledCourseIds = ref(new Set())
const expandedGroups = ref(new Set()) // 展开的课程

const dayLabels = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }

// 已选课程数量
const enrolledCount = computed(() => enrolledCourseIds.value.size)

// 去重教师数
const totalTeachers = computed(() => {
  const teachers = new Set()
  availableGroups.value.forEach(g => {
    g.sections.forEach(s => {
      if (s.teacherName) teachers.add(s.teacherName)
    })
  })
  return teachers.size
})

// 判断是否已选
const isEnrolled = (courseId) => enrolledCourseIds.value.has(courseId)

// 判断课程分组是否全部已选
const isGroupFullyEnrolled = (group) => {
  return group.sections.every(s => isEnrolled(s.id))
}

// 切换展开
const toggleExpand = (courseName) => {
  if (expandedGroups.value.has(courseName)) {
    expandedGroups.value.delete(courseName)
  } else {
    expandedGroups.value.add(courseName)
  }
}

// 搜索 + 筛选过滤
const filteredGroups = computed(() => {
  let groups = availableGroups.value

  // 按筛选类型过滤
  if (filterType.value === 'available') {
    groups = groups.filter(g => !isGroupFullyEnrolled(g))
  } else if (filterType.value === 'enrolled') {
    groups = groups.filter(g => isGroupFullyEnrolled(g))
  }

  // 搜索过滤
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    groups = groups.filter(g => g.courseName.toLowerCase().includes(keyword))
  }

  return groups
})

// 加载可选课程
const loadAvailableCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/available-courses')
    availableGroups.value = res.data || []
  } catch (e) {
    console.error('加载可选课程失败:', e)
  } finally {
    loading.value = false
  }
}

// 加载已选课程
const loadEnrolledCourses = async () => {
  try {
    const res = await request.get('/user/my-courses')
    const ids = (res.data || []).map(c => c.courseId)
    enrolledCourseIds.value = new Set(ids)
  } catch (e) {
    console.error('加载已选课程失败:', e)
  }
}

// 选课
const handleEnroll = async (section) => {
  try {
    await ElMessageBox.confirm(
      `确认选择「${section.name}」— ${section.teacherName || '未知教师'} 的课程？`,
      '确认选课',
      { type: 'info' }
    )
    await request.post('/user/enroll-course', { courseId: section.id })
    ElMessage.success('选课成功')
    enrolledCourseIds.value.add(section.id)
  } catch (e) {
    if (e !== 'cancel') {
      console.error('选课失败:', e)
      ElMessage.error('选课失败')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadAvailableCourses(), loadEnrolledCourses()])
})
</script>

<style scoped>
/* 统计卡片 */
.stat-card {
  cursor: default;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}
.stat-info { display: flex; flex-direction: column; }
.stat-value { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.stat-icon { font-size: 36px; opacity: 0.8; }

/* 课程分组 */
.course-group {
  margin-bottom: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background-color 0.2s;
}
.group-header:hover { background: #ecf5ff; }
.expand-icon {
  font-size: 14px;
  color: #909399;
  transition: transform 0.3s;
}
.expand-icon.expanded { transform: rotate(90deg); }
.group-title-area { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.group-name { font-size: 16px; font-weight: 600; color: #303133; }
.group-desc { font-size: 13px; color: #909399; }
.group-tags { display: flex; gap: 8px; align-items: center; }

/* 展开详情 */
.group-detail { padding: 0; }

/* 上课安排总览 */
.schedule-overview {
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
.overview-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}
.schedule-tags { display: flex; flex-wrap: wrap; gap: 10px; }
.schedule-tag-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  font-size: 13px;
}
.schedule-time { color: #409eff; font-weight: 500; }
.schedule-location { color: #e6a23c; }
.schedule-teacher { color: #67c23a; }

/* 教师班次 */
.section-list { padding: 12px 20px 16px; }
.section-list-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}
.section-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: all 0.2s;
}
.section-card:last-child { margin-bottom: 0; }
.section-card:hover { border-color: #409eff; background: #f5f7fa; }
.section-card.enrolled { background: #f0f9eb; border-color: #e1f3d8; }
.section-info { flex: 1; }
.teacher-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}
.section-desc {
  font-size: 13px;
  color: #909399;
}
.section-action {
  margin-left: 16px;
  min-width: 80px;
  text-align: center;
}
</style>
