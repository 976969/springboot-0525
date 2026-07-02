<!--
  评价管理页面（AI评分、评分记录查看）
-->
<template>
  <div>
    <!-- 评价操作区 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>AI自动评分</span>
          <el-alert title="点击操作栏的'AI评分'按钮可对该成果进行AI自动评分，评分后教师可修改分数" type="info" :closable="false" style="width: auto; margin: 0" />
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap; align-items: center">
        <el-input 
          v-model="searchText" 
          placeholder="输入文件名关键词搜索" 
          clearable 
          style="width: 250px"
          @keyup.enter="loadResults"
          @clear="loadResults"
          @input="debounceSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select 
          v-if="userRole === 'admin'"
          v-model="filterTeacher" 
          placeholder="选择教师" 
          clearable 
          style="width: 150px"
          @change="loadResults"
        >
          <el-option 
            v-for="teacher in teacherList" 
            :key="teacher.id" 
            :label="teacher.realName" 
            :value="teacher.id" 
          />
        </el-select>
        <el-select 
          v-model="filterStudent" 
          placeholder="选择学生" 
          clearable 
          style="width: 150px"
          @change="loadResults"
        >
          <el-option 
            v-for="student in studentList" 
            :key="student.id" 
            :label="student.realName" 
            :value="student.id" 
          />
        </el-select>
        <el-select 
          v-model="filterTask" 
          placeholder="选择实训任务" 
          clearable 
          style="width: 200px"
          @change="loadResults"
        >
          <el-option 
            v-for="task in taskList" 
            :key="task.id" 
            :label="task.title" 
            :value="task.id" 
          />
        </el-select>
        <el-select 
          v-model="filterStatus" 
          placeholder="选择状态" 
          clearable 
          style="width: 150px"
          @change="loadResults"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="已AI评分" :value="1" />
          <el-option label="已AI评分(全部)" :value="2" />
        </el-select>
        <el-button type="primary" @click="resetFilter">重置筛选</el-button>
        <div style="flex-grow: 1"></div>
        <el-button type="warning" @click="openIndicatorChart">查看评分标准</el-button>
      </div>
      
      <!-- 成果列表，显示提交人信息 -->
      <el-table :data="resultList" border stripe v-loading="loadingResults">
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (resultPageNum - 1) * resultPageSize + i + 1" />
        
        <el-table-column prop="fileName" label="文件名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="90" />
        <el-table-column prop="taskTitle" label="实训任务" min-width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="100" v-if="userRole === 'admin'" />
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info" size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning" size="small">已AI评分(部分)</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" size="small">已AI评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" sortable />
        <el-table-column label="操作" width="260" align="center">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="viewContent(row)">预览</el-button>
            <el-button size="small" @click="downloadSourceFile(row)">下载源文件</el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="runEvaluateDirect(row)" 
              :loading="row.evaluating"
              :disabled="row.evaluating"
            >
              AI评分
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="resultPageNum"
        v-model:page-size="resultPageSize"
        :page-sizes="[5, 10, 20]"
        :total="resultTotal"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadResults"
        @current-change="loadResults"
      />
    </el-card>

    <!-- AI评分记录查看区 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>AI评分记录</span>
          <el-alert title="AI已自动完成评分，教师可在报表中心查看并调整最终分数" type="info" :closable="false" style="width: auto; margin: 0" />
        </div>
      </template>
      <el-table :data="paginatedEvaluateList" border stripe v-loading="loading" @sort-change="handleSortChange">
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (evaluatePageNum - 1) * evaluatePageSize + i + 1" />
        
        <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="100" sortable="custom" />
        <el-table-column label="评价指标" min-width="180">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap">
              <el-tag v-for="record in row.records" :key="record.id" type="info" size="small">
                {{ record.indicatorName }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="AI评分" width="90" align="center">
          <template #default="{ row }">
            {{ row.avgAiScore ? row.avgAiScore.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="evalTime" label="评价时间" width="160" sortable="custom" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewEvaluateDetail(row)" style="margin-left: 0">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="evaluatePageNum"
        v-model:page-size="evaluatePageSize"
        :page-sizes="[5, 10, 20]"
        :total="sortedEvaluateList.length"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadEvaluations"
        @current-change="loadEvaluations"
      />
      <el-empty v-if="sortedEvaluateList.length === 0" description="暂无评价记录" />
    </el-card>
    <!-- 评价详情对话框 -->
    <el-dialog v-model="showEvaluateDetailDialog" title="评价详情" width="1000px" top="5vh">
      <el-alert :title="'成果文件：' + (currentEvaluateDetail.fileName || '')" type="info" :closable="false" style="margin-bottom: 20px" />
      
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="提交学生">{{ currentEvaluateDetail.studentName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="实训任务">{{ currentEvaluateDetail.taskTitle || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="评价时间">{{ currentEvaluateDetail.evalTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="currentEvaluateDetail.records || []" border stripe>
        <el-table-column prop="indicatorName" label="评价指标" width="120" />
        <el-table-column prop="aiScore" label="AI评分" width="100" />
        <el-table-column prop="aiComment" label="AI评语" show-overflow-tooltip />
      </el-table>

      <template #footer>
        <el-button @click="showEvaluateDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 查看作业内容对话框 -->
    <el-dialog v-model="showContentDialog" :title="contentTitle" width="800px" top="5vh">
      <!-- 图片类型直接预览 -->
      <div v-if="currentResult.fileType && ['jpg','jpeg','png','gif','bmp','webp'].includes(currentResult.fileType.toLowerCase())" style="text-align: center">
        <img :src="'/' + currentResult.filePath" style="max-width: 100%; max-height: 70vh" alt="作业图片" />
      </div>
      <!-- 文档类型显示解析内容 -->
      <div v-else>
        <el-alert :title="'文件：' + (currentResult.fileName || '')" type="info" :closable="false" style="margin-bottom: 10px" />
        <div v-if="currentResult.parsedContent" style="background: #f5f7fa; padding: 20px; border-radius: 4px; max-height: 60vh; overflow-y: auto; white-space: pre-wrap; line-height: 1.8; font-size: 14px">
          {{ currentResult.parsedContent }}
        </div>
        <el-empty v-else description="该文件暂无可显示的文本内容" />
      </div>
      <template #footer>
        <el-button @click="showContentDialog = false">关闭</el-button>
        <el-button type="primary" @click="downloadFile">下载文件</el-button>
      </template>
    </el-dialog>

    <!-- 评分标准弹框 -->
    <el-dialog v-model="showIndicatorChart" title="评分标准 - 评价指标权重占比" width="650px">
      <div ref="indicatorChartRef" style="width: 100%; height: 360px;"></div>
      <div style="margin-top: 16px;">
        <el-table :data="indicatorList" border stripe size="small">
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
      <template #footer>
        <el-button @click="showIndicatorChart = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, onBeforeUnmount } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const userRole = ref('')
const teacherList = ref([])
const filterTeacher = ref('')
const evaluateList = ref([])
const groupedEvaluateList = ref([])
const resultList = ref([])
const selectedResultId = ref(null)
const showEvaluateDialog = ref(false)
const showContentDialog = ref(false)
const showEvaluateDetailDialog = ref(false)
const currentResult = ref({})
const currentEvaluateDetail = ref({})
const contentTitle = ref('')
const loading = ref(false)
const loadingResults = ref(false)
const evaluating = ref(false)
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 评价记录分页
const evaluatePageNum = ref(1)
const evaluatePageSize = ref(10)
const evaluateTotal = ref(0)
// 筛选条件
const searchText = ref('')
const filterStudent = ref('')
const filterTask = ref('')
const filterStatus = ref(null)
const studentList = ref([])
const taskList = ref([])

// 评分标准弹框相关
const showIndicatorChart = ref(false)
const indicatorList = ref([])
const indicatorChartRef = ref(null)
let indicatorChartInstance = null

const indicatorTotalWeight = computed(() => {
  return indicatorList.value.reduce((sum, item) => sum + (Number(item.defaultWeight) || 0), 0)
})

const getIndicatorPercent = (weight) => {
  const tw = indicatorTotalWeight.value
  if (!tw || tw === 0) return 0
  return Math.round((Number(weight) / tw) * 100)
}

const renderIndicatorChart = () => {
  if (!indicatorChartRef.value) return
  if (indicatorChartInstance) indicatorChartInstance.dispose()
  indicatorChartInstance = echarts.init(indicatorChartRef.value)
  const data = indicatorList.value.map(item => ({
    name: item.name,
    value: Number(item.defaultWeight) || 0
  }))
  indicatorChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const pct = ((params.value / indicatorTotalWeight.value) * 100).toFixed(1)
        return `${params.name}<br/>权重: ${params.value} | 占比: ${pct}%`
      }
    },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemGap: 12 },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c', '#3498db'],
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: (params) => {
          const pct = ((params.value / indicatorTotalWeight.value) * 100).toFixed(1)
          return `${params.name}\n${pct}%`
        },
        fontSize: 13,
        lineHeight: 18
      },
      emphasis: {
        label: { show: true, fontSize: 15, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' }
      },
      data: data
    }]
  })
}

const openIndicatorChart = async () => {
  try {
    const res = await request.get('/indicator/list')
    indicatorList.value = res.data || []
    showIndicatorChart.value = true
    await nextTick()
    renderIndicatorChart()
  } catch (e) {
    console.error('加载指标失败:', e)
    ElMessage.error('加载评分标准失败')
  }
}

onBeforeUnmount(() => {
  indicatorChartInstance?.dispose()
})

// 防抖搜索(输入时自动触发,延迟500ms)
let searchTimer = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    resultPageNum.value = 1  // 搜索时重置到第1页
    loadResults()
  }, 500)
}

// 查看评价详情
const viewEvaluateDetail = (row) => {
  currentEvaluateDetail.value = row
  showEvaluateDetailDialog.value = true
}

// 查看作业内容
const viewContent = async (row) => {
  try {
    const res = await request.get(`/result/${row.id}`)
    currentResult.value = res.data || row
    contentTitle.value = `查看作业 - ${currentResult.value.studentName || '未知'} - ${currentResult.value.fileName || ''}`
    showContentDialog.value = true
  } catch (e) {
    ElMessage.error('加载作业内容失败')
  }
}

// 下载文件（弹框内用）
const downloadFile = () => {
  if (!currentResult.value.filePath) return
  const link = document.createElement('a')
  link.href = '/' + currentResult.value.filePath
  link.download = currentResult.value.fileName || 'download'
  link.click()
}

// 下载学生提交的源文件（表格操作栏用）
const downloadSourceFile = (row) => {
  if (!row.filePath) {
    ElMessage.warning('文件路径不存在')
    return
  }
  const link = document.createElement('a')
  link.href = '/' + row.filePath
  link.download = row.fileName || 'download'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 加载实训成果列表
const loadResults = async () => {
  loadingResults.value = true
  try {
    const res = await request.get('/result/page', {
      params: {
        pageNum: resultPageNum.value,
        pageSize: resultPageSize.value,
        studentId: filterStudent.value || undefined,
        taskId: filterTask.value || undefined,
        status: filterStatus.value !== null ? filterStatus.value : undefined,
        fileName: searchText.value || undefined,
        teacherId: filterTeacher.value || undefined
      }
    })
    // 为每行添加evaluating状态，并按状态排序：待处理(0) > 已核查(1) > 已评价(2)
    resultList.value = (res.data.list || []).map(item => ({
      ...item,
      evaluating: false
    })).sort((a, b) => (a.status ?? 99) - (b.status ?? 99))
    resultTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载成果列表失败:', e)
  } finally {
    loadingResults.value = false
  }
}

// 加载学生列表
const loadStudents = async () => {
  try {
    const res = await request.get('/user/student/list')
    studentList.value = res.data || []
  } catch (e) {
    console.error('加载学生列表失败:', e)
  }
}

// 加载教师列表
const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) {
    console.error('加载教师列表失败:', e)
  }
}

// 加载实训任务列表
const loadTasks = async () => {
  try {
    const res = await request.get('/task/page', {
      params: {
        pageNum: 1,
        pageSize: 100
      }
    })
    taskList.value = res.data.list || []
  } catch (e) {
    console.error('加载任务列表失败:', e)
  }
}

// 重置筛选
const resetFilter = () => {
  searchText.value = ''
  filterStudent.value = ''
  filterTask.value = ''
  filterStatus.value = null
  filterTeacher.value = ''
  resultPageNum.value = 1
  loadResults()
}

// 排序状态：教师未评分始终在最前，其次按选中的列排序
const sortField = ref('')
const sortOrder = ref('')  // 'ascending' | 'descending' | ''

// 处理表头排序变化
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop || ''
  sortOrder.value = order || ''
}

// 排序后的评价列表（教师未评分始终前置）
const sortedEvaluateList = computed(() => {
  const list = [...groupedEvaluateList.value]
  list.sort((a, b) => {
    // 第一优先级：教师未评分(avgTeacherScore为null)排在前面
    const aUnscored = !a.avgTeacherScore
    const bUnscored = !b.avgTeacherScore
    if (aUnscored && !bUnscored) return -1
    if (!aUnscored && bUnscored) return 1

    // 第二优先级：按选中列排序
    if (sortField.value && sortOrder.value) {
      const asc = sortOrder.value === 'ascending'
      let cmp = 0
      if (sortField.value === 'studentName') {
        const aName = a.studentName || ''
        const bName = b.studentName || ''
        cmp = aName.localeCompare(bName, 'zh-CN')
      } else if (sortField.value === 'evalTime') {
        const aTime = a.evalTime ? new Date(a.evalTime).getTime() : 0
        const bTime = b.evalTime ? new Date(b.evalTime).getTime() : 0
        cmp = aTime - bTime
      }
      return asc ? cmp : -cmp
    }

    // 默认：未评分组内按评价时间倒序，已评分组内按评价时间倒序
    const aTime = a.evalTime ? new Date(a.evalTime).getTime() : 0
    const bTime = b.evalTime ? new Date(b.evalTime).getTime() : 0
    return bTime - aTime
  })
  return list
})

// 分页后的评价记录列表(计算属性)
const paginatedEvaluateList = computed(() => {
  const start = (evaluatePageNum.value - 1) * evaluatePageSize.value
  const end = start + evaluatePageSize.value
  return sortedEvaluateList.value.slice(start, end)
})

// 加载评价记录（按成果分组）
const loadEvaluations = async () => {
  loading.value = true
  try {
    const results = await request.get('/result/list')
    const groupedEvaluations = []
    
    for (const result of (results.data || [])) {
      const res = await request.get(`/evaluate/records/${result.id}`)
      if (res.data && res.data.length > 0) {
        const records = res.data
        
        // 计算平均分
        const aiScores = records.map(r => parseFloat(r.aiScore || 0)).filter(s => !isNaN(s))
        const teacherScores = records.map(r => parseFloat(r.teacherScore || 0)).filter(s => !isNaN(s) && s > 0)
        const finalScores = records.map(r => parseFloat(r.finalScore || 0)).filter(s => !isNaN(s))
        
        const evalGroup = {
          resultId: result.id,
          fileName: result.fileName,
          studentName: result.studentName,
          taskTitle: result.taskTitle,
          evalTime: records[0].createTime,
          records: records,
          scoreRatio: records[0].scoreRatio != null ? Number(records[0].scoreRatio) : 5,
          avgAiScore: aiScores.length > 0 ? aiScores.reduce((a, b) => a + b, 0) / aiScores.length : null,
          avgTeacherScore: teacherScores.length > 0 ? teacherScores.reduce((a, b) => a + b, 0) / teacherScores.length : null,
          avgFinalScore: finalScores.length > 0 ? finalScores.reduce((a, b) => a + b, 0) / finalScores.length : null
        }
        
        groupedEvaluations.push(evalGroup)
      }
    }
    
    groupedEvaluateList.value = groupedEvaluations
  } catch (e) {
    console.error('加载评价记录失败:', e)
  } finally {
    loading.value = false
  }
}

// 执行AI评分
const runAiEvaluate = async () => {
  if (!selectedResultId.value) {
    ElMessage.warning('请选择要评价的成果')
    return
  }
  
  evaluating.value = true
  try {
    const res = await request.post(`/evaluate/ai/${selectedResultId.value}`)
    const data = res.data || {}
    const failCount = data.failCount || 0
    if (failCount > 0) {
      ElMessage.warning(`AI评分完成，${data.successCount}个指标成功，${failCount}个指标失败`)
    } else {
      ElMessage.success('AI评分完成！')
    }
    showEvaluateDialog.value = false
    loadEvaluations()
    loadResults()
  } catch (e) {
    console.error('AI评分失败:', e)
    ElMessage.error('评分失败：' + (e.response?.data?.msg || e.message))
  } finally {
    evaluating.value = false
  }
}

// 直接评分当前行 (新方法)
const runEvaluateDirect = async (row) => {
  if (!row.id) {
    ElMessage.warning('成果ID不存在')
    return
  }
  
  // 已评价时，弹出确认对话框
  if (row.status === 2) {
    try {
      await ElMessageBox.confirm(
        '该文件已评价，确定要再次核查或评分吗？这将覆盖之前的评价记录。',
        '确认操作',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return  // 用户取消
    }
  }
  
  // 设置当前行的评分状态为进行中
  row.evaluating = true
  
  try {
    ElMessage.info('正在进行AI自动评分，请稍候（约需10-30秒）...')
    const res = await request.post(`/evaluate/ai/${row.id}`)
    const data = res.data || {}
    const failCount = data.failCount || 0
    if (failCount > 0) {
      ElMessage.warning(`AI评分完成，${data.successCount}个指标成功，${failCount}个指标失败`)
    } else {
      ElMessage.success('AI评分完成！')
    }
    
    // 刷新列表和评价记录
    await loadEvaluations()
    await loadResults()
  } catch (e) {
    console.error('AI评分失败:', e)
    ElMessage.error('评分失败：' + (e.response?.data?.msg || e.message))
  } finally {
    row.evaluating = false
  }
}

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') {
    loadTeachers()
  }
  loadResults()
  loadEvaluations()
  loadStudents()
  loadTasks()
})
</script>
