<!--
  学生端我的成绩（课程→任务→成果三级分组）
-->
<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: bold; font-size: 16px;">我的成绩</span>
        <el-tag type="info">共 {{ courseScores.length }} 门课程，{{ totalTasks }} 个任务，{{ totalResults }} 个作业</el-tag>
      </div>
    </template>

    <el-collapse v-model="expandedCourses">
      <el-collapse-item v-for="course in courseScores" :key="course.courseId" :name="course.courseId">
        <template #title>
          <div class="course-header">
            <el-icon :size="18" style="margin-right: 8px; color: #409eff;"><Reading /></el-icon>
            <span class="course-title">{{ course.courseName }}</span>
            <el-tag size="small" type="primary" style="margin-left: 12px;">{{ course.tasks.length }} 个任务</el-tag>
            <el-tag size="small" :type="course.avgScore >= 60 ? 'success' : course.avgScore === '-' ? 'info' : 'danger'" style="margin-left: 8px;">
              平均分: {{ course.avgScore }}
            </el-tag>
          </div>
        </template>

        <!-- 课程内的任务列表 -->
        <div v-for="task in course.tasks" :key="task.taskId" class="task-group">
          <div class="task-header">
            <el-icon :size="16"><Folder /></el-icon>
            <span class="task-title">{{ task.taskTitle }}</span>
            <el-tag size="small" type="primary">{{ task.results.length }} 个作业</el-tag>
            <el-tag size="small" :type="task.avgScore >= 60 ? 'success' : task.avgScore === '-' ? 'info' : 'danger'" style="margin-left: 8px;">
              平均分: {{ task.avgScore }}
            </el-tag>
          </div>

          <el-table :data="task.results" border stripe class="result-table">
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="fileName" label="作业文件" min-width="200" show-overflow-tooltip />
            <el-table-column prop="uploadTime" label="上传时间" width="180" />
            <el-table-column prop="statusText" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalScore" label="总分" width="100">
              <template #default="{ row }">
                <span v-if="row.totalScore" style="font-weight: bold; color: #409eff;">
                  {{ row.totalScore }}
                </span>
                <span v-else style="color: #999;">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="viewDetail(row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-if="course.tasks.length === 0" description="该课程暂无成绩数据" :image-size="60" />
      </el-collapse-item>
    </el-collapse>

    <el-empty v-if="courseScores.length === 0" description="暂无成绩数据" />
  </el-card>

  <!-- 评价详情对话框 -->
  <el-dialog v-model="detailVisible" :title="currentResult?.fileName + ' - 评价详情'" width="1100px" top="5vh">
    <el-descriptions :column="2" border style="margin-bottom: 20px;">
      <el-descriptions-item label="作业文件">{{ currentResult?.fileName }}</el-descriptions-item>
      <el-descriptions-item label="上传时间">{{ currentResult?.uploadTime }}</el-descriptions-item>
      <el-descriptions-item label="总分">
        <el-tag type="primary" size="large">{{ currentResult?.totalScore || '未评分' }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="状态">
        <el-tag :type="getStatusType(currentResult?.status)">{{ currentResult?.statusText }}</el-tag>
      </el-descriptions-item>
    </el-descriptions>

    <el-table :data="detailRecords" border stripe>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="indicatorName" label="评价指标" min-width="150" />
      <el-table-column prop="aiScore" label="AI评分" width="100">
        <template #default="{ row }">
          <span v-if="row.aiScore" style="color: #67c23a; font-weight: bold;">{{ row.aiScore }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="teacherScore" label="教师评分" width="100">
        <template #default="{ row }">
          <span v-if="row.teacherScore" style="color: #e6a23c; font-weight: bold;">{{ row.teacherScore }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="finalScore" label="最终得分" width="100">
        <template #default="{ row }">
          <span v-if="row.finalScore" style="color: #409eff; font-weight: bold;">{{ row.finalScore }}</span>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="AI评语" width="120" align="center">
        <template #default="{ row }">
          <el-button v-if="row.aiComment" size="small" type="primary" text @click="viewComment('AI评语', row.aiComment)">查看评语</el-button>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="教师评语" width="120" align="center">
        <template #default="{ row }">
          <el-button v-if="row.teacherComment" size="small" type="warning" text @click="viewComment('教师评语', row.teacherComment)">查看评语</el-button>
          <span v-else style="color: #999;">-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>

  <!-- 评语详情对话框 -->
  <el-dialog v-model="commentVisible" :title="commentTitle" width="500px">
    <div style="white-space: pre-wrap; line-height: 1.8; font-size: 14px; background: #f5f7fa; padding: 16px; border-radius: 8px; min-height: 80px;">
      {{ commentContent }}
    </div>
    <template #footer>
      <el-button @click="commentVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Folder, Reading } from '@element-plus/icons-vue'

const userStore = useUserStore()
const results = ref([])  // 所有成果
const reports = ref([])  // 所有报告
const detailRecords = ref([])
const detailVisible = ref(false)
const currentResult = ref(null)
const expandedCourses = ref([])  // 展开的课程列表
const commentVisible = ref(false)  // 评语弹窗
const commentTitle = ref('')       // 评语标题
const commentContent = ref('')     // 评语内容

// 按课程→任务分组的数据
const courseScores = computed(() => {
  const courseMap = new Map()
  
  results.value.forEach(result => {
    const courseId = result.courseId || 0
    const taskId = result.taskId
    
    if (!courseMap.has(courseId)) {
      courseMap.set(courseId, {
        courseId,
        courseName: result.courseName || '未知课程',
        tasks: new Map()
      })
    }
    
    const course = courseMap.get(courseId)
    
    if (!course.tasks.has(taskId)) {
      course.tasks.set(taskId, {
        taskId,
        taskTitle: result.taskTitle,
        results: []
      })
    }
    
    // 查找对应的报告
    const report = reports.value.find(
      r => r.taskId === taskId && r.studentId === result.studentId && r.resultId === result.id
    )
    
    // 状态文本映射
    const statusMap = { 0: '待核查', 1: '已核查', 2: '已评分' }
    
    course.tasks.get(taskId).results.push({
      ...result,
      totalScore: report ? report.totalScore : null,
      reportId: report ? report.id : null,
      statusText: statusMap[result.status] || '未知'
    })
  })
  
  // 转换为数组并计算统计
  return Array.from(courseMap.values()).map(course => {
    const tasksArray = Array.from(course.tasks.values()).map(task => {
      const scoredResults = task.results.filter(r => r.totalScore !== null)
      const avgScore = scoredResults.length > 0
        ? (scoredResults.reduce((sum, r) => sum + parseFloat(r.totalScore), 0) / scoredResults.length).toFixed(1)
        : '-'
      return { ...task, avgScore }
    })
    
    // 计算课程平均分
    const allScoredResults = tasksArray.flatMap(t => t.results.filter(r => r.totalScore !== null))
    const courseAvg = allScoredResults.length > 0
      ? (allScoredResults.reduce((sum, r) => sum + parseFloat(r.totalScore), 0) / allScoredResults.length).toFixed(1)
      : '-'
    
    return {
      ...course,
      tasks: tasksArray,
      avgScore: courseAvg
    }
  })
})

// 总任务数
const totalTasks = computed(() => {
  return courseScores.value.reduce((sum, c) => sum + c.tasks.length, 0)
})

// 总作业数
const totalResults = computed(() => results.value.length)

// 加载数据
const loadScores = async () => {
  try {
    const userId = userStore.userInfo.realId || userStore.userInfo.id
    const realId = typeof userId === 'string' && userId.includes(':') 
      ? parseInt(userId.split(':')[1]) 
      : userId
    
    // 获取当前学生的所有成果
    const resultsRes = await request.get('/result/list')
    const allResults = resultsRes.data || []
    results.value = allResults.filter(r => r.studentId === realId)
    
    // 获取所有报告
    const reportsRes = await request.get('/report/list')
    const allReports = reportsRes.data || []
    
    // 为每个成果查找对应的报告，关联 resultId
    reports.value = allReports.map(report => {
      const result = allResults.find(
        r => r.taskId === report.taskId && r.studentId === report.studentId
      )
      return {
        ...report,
        resultId: result ? result.id : null
      }
    })
    
    // 默认展开第一个课程
    if (courseScores.value.length > 0 && expandedCourses.value.length === 0) {
      expandedCourses.value = [courseScores.value[0].courseId]
    }
  } catch (e) {
    console.error('加载成绩失败:', e)
    ElMessage.error('加载成绩失败')
  }
}

// 查看详情
const viewDetail = async (result) => {
  if (!result.id) {
    ElMessage.error('成果信息不完整')
    return
  }
  
  try {
    const res = await request.get(`/evaluate/records/${result.id}`)
    detailRecords.value = res.data || []
    currentResult.value = result
    
    if (detailRecords.value.length === 0) {
      ElMessage.warning('该作业暂无评价记录')
      return
    }
    
    detailVisible.value = true
  } catch (e) {
    console.error('查看详情失败:', e)
    ElMessage.error('加载详情失败')
  }
}

// 查看评语
const viewComment = (title, content) => {
  commentTitle.value = title
  commentContent.value = content || '暂无评语'
  commentVisible.value = true
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = { 0: 'info', 1: 'warning', 2: 'success' }
  return typeMap[status] || 'info'
}

onMounted(loadScores)
</script>

<style scoped>
.course-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}

.course-title {
  flex: 1;
}

.task-group {
  margin: 12px 0 20px 16px;
}

.task-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  background-color: #f5f7fa;
  border-left: 3px solid #67c23a;
  border-radius: 4px;
  font-weight: 500;
  font-size: 13px;
  color: #606266;
}

.task-title {
  flex: 1;
}

.result-table {
  margin-top: 8px;
}

.result-table :deep(.el-table__header th) {
  background-color: #fafafa;
}

:deep(.el-collapse-item__header) {
  height: 56px;
  line-height: 56px;
  font-size: 15px;
  border-bottom: 2px solid #e4e7ed;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid #ebeef5;
}
</style>
