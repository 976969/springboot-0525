<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: bold; font-size: 16px;">我的成绩</span>
        <el-tag type="info">共 {{ taskScores.length }} 个任务，{{ totalResults }} 个作业</el-tag>
      </div>
    </template>

    <!-- 按任务分组展示 -->
    <div v-for="task in taskScores" :key="task.taskId" class="task-group">
      <div class="task-header">
        <el-icon :size="18"><Folder /></el-icon>
        <span class="task-title">{{ task.taskTitle }}</span>
        <el-tag size="small" type="primary">{{ task.results.length }} 个作业</el-tag>
        <el-tag size="small" :type="task.avgScore >= 60 ? 'success' : 'danger'" style="margin-left: 8px;">
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

    <el-empty v-if="taskScores.length === 0" description="暂无成绩数据" />
  </el-card>

  <!-- 评价详情对话框 -->
  <el-dialog v-model="detailVisible" :title="currentResult?.fileName + ' - 评价详情'" width="800px">
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
      <el-table-column prop="aiComment" label="AI评语" min-width="200" show-overflow-tooltip />
      <el-table-column prop="teacherComment" label="教师评语" min-width="200" show-overflow-tooltip />
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Folder } from '@element-plus/icons-vue'

const userStore = useUserStore()
const results = ref([])  // 所有成果
const reports = ref([])  // 所有报告
const detailRecords = ref([])
const detailVisible = ref(false)
const currentResult = ref(null)

// 按任务分组的数据
const taskScores = computed(() => {
  const taskMap = new Map()
  
  // 遍历所有成果，按任务分组
  results.value.forEach(result => {
    const taskId = result.taskId
    
    if (!taskMap.has(taskId)) {
      taskMap.set(taskId, {
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
    const statusMap = {
      0: '待核查',
      1: '已核查',
      2: '已评分'
    }
    
    taskMap.get(taskId).results.push({
      ...result,
      totalScore: report ? report.totalScore : null,
      reportId: report ? report.id : null,
      statusText: statusMap[result.status] || '未知'
    })
  })
  
  // 转换为数组，并计算每个任务的平均分
  return Array.from(taskMap.values()).map(task => {
    const scoredResults = task.results.filter(r => r.totalScore !== null)
    const avgScore = scoredResults.length > 0
      ? (scoredResults.reduce((sum, r) => sum + parseFloat(r.totalScore), 0) / scoredResults.length).toFixed(1)
      : '-'
    
    return {
      ...task,
      avgScore
    }
  })
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
    // 使用 resultId 查询评价记录
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

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

onMounted(loadScores)
</script>

<style scoped>
.task-group {
  margin-bottom: 24px;
}

.task-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background-color: #f5f7fa;
  border-left: 3px solid #409eff;
  border-radius: 4px;
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.task-title {
  flex: 1;
}

.result-table {
  margin-top: 0;
}

.result-table :deep(.el-table__header th) {
  background-color: #fafafa;
}
</style>
