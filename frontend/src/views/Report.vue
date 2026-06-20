<!--
  报表中心页面（报告生成、批量生成、Excel/PDF导出、筛选）
-->
<template>
  <div>
    <!-- 成果列表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>成果列表</span>
          <div>
            <el-button 
              type="success" 
              @click="batchGenerateSelected" 
              :disabled="generating || selectedResults.length === 0"
            >
              批量生成选中 ({{ selectedResults.length }})
            </el-button>
            <el-button @click="clearSelection">清空选择</el-button>
          </div>
        </div>
      </template>
      
      <el-alert 
        title="勾选要生成报告的成果，点击'批量生成选中'按钮" 
        type="info" 
        :closable="false" 
        style="margin-bottom: 15px" 
      />
      
      <!-- 筛选条件 -->
      <div style="margin-bottom: 15px; display: flex; gap: 15px; flex-wrap: wrap">
        <el-input 
          v-model="searchText" 
          placeholder="输入文件名关键词搜索" 
          clearable 
          style="width: 250px"
          @keyup.enter="() => { resultPageNum = 1; loadResults() }"
          @clear="() => { resultPageNum = 1; loadResults() }"
          @input="debounceSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select 
          v-model="filterStudent" 
          placeholder="选择学生" 
          clearable 
          style="width: 150px"
          @change="() => { resultPageNum = 1; loadResults() }"
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
          @change="() => { resultPageNum = 1; loadResults() }"
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
          style="width: 120px"
          @change="() => { resultPageNum = 1; loadResults() }"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="已核查" :value="1" />
          <el-option label="已评价" :value="2" />
        </el-select>
        <el-button type="primary" @click="resetFilter">重置筛选</el-button>
      </div>
      
      <!-- 成果列表 -->
      <el-table 
        ref="resultTableRef"
        :data="resultList" 
        border 
        stripe 
        v-loading="loadingResults"
        :row-class-name="tableRowClassName"
        @selection-change="handleSelectionChange"
      >
        <!-- 多选框 -->
        <el-table-column type="selection" width="55" align="center" :selectable="canSelect" />
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (resultPageNum - 1) * resultPageSize + i + 1" />
        
        <el-table-column prop="fileName" label="文件名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="90" />
        <el-table-column prop="taskTitle" label="实训任务" min-width="140" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info" size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning" size="small">已核查</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" size="small">已评价</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.hasReport === 1" type="success" size="small" effect="dark">
              <el-icon style="margin-right: 2px"><Check /></el-icon>已生成
            </el-tag>
            <el-tag v-else type="warning" size="small" effect="plain">
              <el-icon style="margin-right: 2px"><Clock /></el-icon>待生成
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" sortable />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button 
              v-if="row.hasReport !== 1" 
              size="small" 
              type="primary" 
              @click="generateSingleReport(row.id)" 
              :loading="generating"
            >
              生成报告
            </el-button>
            <el-button 
              v-else 
              size="small" 
              type="info" 
              @click="viewReportDetail(row.reportId)"
            >
              查看报告
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

    <!-- 已生成报告列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>报告列表</span>
          <el-button size="small" @click="loadReports">刷新</el-button>
        </div>
      </template>
      
      <!-- 报告列表筛选 -->
      <div style="margin-bottom: 15px; display: flex; gap: 15px; flex-wrap: wrap; align-items: center">
        <el-select 
          v-model="reportFilterStudent" 
          placeholder="选择学生" 
          clearable 
          style="width: 150px"
          @change="() => { reportPageNum = 1; loadFilteredReports() }"
        >
          <el-option 
            v-for="student in studentList" 
            :key="student.id" 
            :label="student.realName" 
            :value="student.id" 
          />
        </el-select>
        <el-select 
          v-model="reportFilterTask" 
          placeholder="选择实训任务" 
          clearable 
          style="width: 200px"
          @change="() => { reportPageNum = 1; loadFilteredReports() }"
        >
          <el-option 
            v-for="task in taskList" 
            :key="task.id" 
            :label="task.title" 
            :value="task.id" 
          />
        </el-select>
        <el-button @click="resetReportFilter">重置筛选</el-button>
      </div>
      
      <el-table :data="reportList" border stripe v-loading="loading">
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (reportPageNum - 1) * reportPageSize + i + 1" />
        
        <el-table-column prop="taskTitle" label="实训任务" min-width="160" show-overflow-tooltip />
        <el-table-column prop="fileName" label="成果文件" min-width="180" show-overflow-tooltip />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
              {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="180" sortable />
        <el-table-column label="操作" width="320" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" type="success" @click="exportExcel(row.id)">导出Excel</el-button>
            <el-button size="small" type="warning" @click="exportPdf(row.id)">导出PDF</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="reportPageNum"
        v-model:page-size="reportPageSize"
        :page-sizes="[5, 10, 20]"
        :total="reportTotal"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="handleReportPageChange"
        @current-change="handleReportPageChange"
      />
      <el-empty v-if="reportList.length === 0 && !loading" description="暂无报告数据，请先生成报告" />
    </el-card>

    <!-- 报告详情对话框 -->
    <el-dialog v-model="detailVisible" title="评价报告详情" width="900px" top="5vh">
      <el-alert :title="'实训任务：' + (currentReport.taskTitle || '')" type="info" :closable="false" style="margin-bottom: 15px" />
      
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="学生姓名">{{ currentReport.studentName }}</el-descriptions-item>
        <el-descriptions-item label="总分">
          <el-tag :type="currentReport.totalScore >= 80 ? 'success' : currentReport.totalScore >= 60 ? 'warning' : 'danger'">
            {{ currentReport.totalScore ? currentReport.totalScore.toFixed(1) : '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ currentReport.createTime }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>各指标评价详情</el-divider>
      
      <div v-if="parsedReportData" style="background: #f5f7fa; padding: 20px; border-radius: 4px; max-height: 500px; overflow-y: auto">
        <el-table :data="parsedReportData.indicators || []" border stripe>
          <el-table-column prop="indicatorName" label="评价指标" width="120" />
          <el-table-column prop="aiScore" label="AI评分" width="80" align="center" />
          <el-table-column prop="teacherScore" label="教师评分" width="90" align="center" />
          <el-table-column prop="finalScore" label="最终得分" width="90" align="center" />
          <el-table-column prop="aiComment" label="AI评语" show-overflow-tooltip />
          <el-table-column prop="teacherComment" label="教师评语" show-overflow-tooltip />
        </el-table>
      </div>
      <el-empty v-else description="报告数据解析失败" />

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="success" @click="exportExcel(currentReport.id)">导出Excel</el-button>
        <el-button type="warning" @click="exportPdf(currentReport.id)">导出PDF</el-button>
      </template>
    </el-dialog>

    <!-- 批量生成对话框 -->
    <el-dialog v-model="showBatchGenerateDialog" title="批量生成报告" width="500px">
      <el-alert title="选择实训任务，将为该任务下所有已评价的成果生成报告" type="warning" :closable="false" style="margin-bottom: 15px" />
      <el-form>
        <el-form-item label="选择任务">
          <el-select v-model="selectedTaskId" placeholder="请选择任务" style="width: 100%">
            <el-option 
              v-for="task in taskList" 
              :key="task.id" 
              :label="task.title" 
              :value="task.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBatchGenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="generateBatchReports" :loading="generating">批量生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Search, Check, Clock } from '@element-plus/icons-vue'

const reportList = ref([])
const resultList = ref([])
const taskList = ref([])
const studentList = ref([])
const detailVisible = ref(false)
const showBatchGenerateDialog = ref(false)
const currentReport = ref({})
const selectedTaskId = ref(null)
const loading = ref(false)
const loadingResults = ref(false)
const generating = ref(false)
// 多选相关
const resultTableRef = ref(null)
const selectedResults = ref([])
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 报告列表分页
const reportPageNum = ref(1)
const reportPageSize = ref(10)
const reportTotal = ref(0)
// 筛选条件(上方生成报告区)
const searchText = ref('')
const filterStudent = ref('')
const filterTask = ref('')
const filterStatus = ref(null)
// 篮选条件(下方报告列表区)
const reportFilterStudent = ref('')
const reportFilterTask = ref('')

// 防抖搜索
let searchTimer = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    resultPageNum.value = 1
    loadResults()
  }, 500)
}

// 重置筛选
const resetFilter = () => {
  searchText.value = ''
  filterStudent.value = ''
  filterTask.value = ''
  filterStatus.value = null
  resultPageNum.value = 1
  loadResults()
}

// 重置报告列表筛选
const resetReportFilter = () => {
  reportFilterStudent.value = ''
  reportFilterTask.value = ''
  reportPageNum.value = 1
  loadFilteredReports()
}

// 报告列表筛选加载
const loadFilteredReports = () => {
  // 如果有筛选条件，用全量数据+前端分页
  if (reportFilterStudent.value || reportFilterTask.value) {
    loadFilteredReportList()
  } else {
    loadReports()
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

// 解析报告数据
const parsedReportData = computed(() => {
  if (!currentReport.value.reportData) return null
  try {
    return JSON.parse(currentReport.value.reportData)
  } catch (e) {
    return null
  }
})

// 加载报告列表(分页接口)
const loadReports = async () => {
  loading.value = true
  try {
    const res = await request.get('/report/page', {
      params: {
        pageNum: reportPageNum.value,
        pageSize: reportPageSize.value
      }
    })
    reportList.value = res.data.list || []
    reportTotal.value = res.data.total || 0
  } catch (e) {
    console.error('加载报告失败:', e)
  } finally {
    loading.value = false
  }
}

// 篮选条件下的报告加载(全量+前端分页)
const allFilteredReports = ref([])
const loadFilteredReportList = async () => {
  loading.value = true
  try {
    const res = await request.get('/report/list')
    let list = res.data || []
    // 按筛选条件过滤
    if (reportFilterStudent.value) {
      list = list.filter(r => r.studentId === reportFilterStudent.value)
    }
    if (reportFilterTask.value) {
      list = list.filter(r => r.taskId === reportFilterTask.value)
    }
    allFilteredReports.value = list
    reportTotal.value = list.length
    // 前端分页
    const start = (reportPageNum.value - 1) * reportPageSize.value
    const end = Math.min(start + reportPageSize.value, list.length)
    reportList.value = start < list.length ? list.slice(start, end) : []
  } catch (e) {
    console.error('加载筛选报告失败:', e)
  } finally {
    loading.value = false
  }
}

// 加载成果列表
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
        fileName: searchText.value || undefined
      }
    })
    
    let results = res.data.list || []
    resultTotal.value = res.data.total || 0
    
    // 加载所有报告列表，判断哪些成果已生成报告
    const reportRes = await request.get('/report/list')
    const allReports = reportRes.data || []
    
    // 创建报告映射：key = "taskId_studentId", value = reportId
    const reportMap = new Map()
    allReports.forEach(report => {
      const key = `${report.taskId}_${report.studentId}`
      reportMap.set(key, report.id)
    })
    
    // 为每个成果添加报告状态
    results = results.map(result => {
      const key = `${result.taskId}_${result.studentId}`
      return {
        ...result,
        hasReport: reportMap.has(key) ? 1 : 0,
        reportId: reportMap.get(key) || null
      }
    })
    
    // 排序：未生成的在前，已生成的在后
    results.sort((a, b) => a.hasReport - b.hasReport)
    
    resultList.value = results
  } catch (e) {
    console.error('加载成果失败:', e)
  } finally {
    loadingResults.value = false
  }
}

// 加载任务列表
const loadTasks = async () => {
  try {
    const res = await request.get('/task/list')
    taskList.value = res.data || []
  } catch (e) {
    console.error('加载任务失败:', e)
  }
}

// 生成单个报告
const generateSingleReport = async (resultId) => {
  if (!resultId) {
    ElMessage.warning('请选择成果')
    return
  }
  
  generating.value = true
  try {
    // 1. 生成报告
    await request.post(`/report/generate/${resultId}`)
    ElMessage.success('报告生成成功！')
    
    // 2. 刷新列表（独立错误处理，不影响生成成功的提示）
    reportPageNum.value = 1
    try {
      await loadReports()
      await loadResults()
    } catch (refreshError) {
      console.warn('刷新列表失败，但报告已生成:', refreshError)
      // 不显示错误提示，避免用户困惑
    }
  } catch (e) {
    ElMessage.error('生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

// 批量生成报告
const generateBatchReports = async () => {
  if (!selectedTaskId.value) {
    ElMessage.warning('请选择任务')
    return
  }
  
  generating.value = true
  const loadingMsg = ElMessage({
    message: '正在批量生成报告，请稍候...',
    type: 'info',
    duration: 0  // 不自动关闭
  })
  
  try {
    // 1. 批量生成
    const res = await request.post(`/report/generate/task/${selectedTaskId.value}`)
    loadingMsg.close()
    ElMessage.success(`成功生成 ${res.data.successCount} 份报告`)
    showBatchGenerateDialog.value = false
    
    // 2. 刷新列表（独立错误处理）
    reportPageNum.value = 1
    try {
      await loadReports()
      await loadResults()
    } catch (refreshError) {
      console.warn('刷新列表失败，但报告已生成:', refreshError)
    }
  } catch (e) {
    loadingMsg.close()
    ElMessage.error('批量生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

// 查看详情 - 从后端获取完整报告数据(含reportData)
const viewDetail = async (row) => {
  try {
    const res = await request.get(`/report/${row.id}`)
    currentReport.value = res.data || row
    detailVisible.value = true
  } catch (e) {
    // 如果接口不存在，降级使用row数据
    currentReport.value = row
    detailVisible.value = true
  }
}

// 导出Excel
const exportExcel = async (reportId) => {
  try {
    const res = await request.get(`/report/export/excel/${reportId}`, {
      responseType: 'blob'
    })
    
    // 检查是否是错误响应(Blob可能包含错误信息)
    if (res.data.type === 'application/json') {
      const text = await res.data.text()
      const errorData = JSON.parse(text)
      ElMessage.error('导出失败: ' + (errorData.message || '未知错误'))
      return
    }
    
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `评价报告_${reportId}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出Excel失败:', e)
    ElMessage.error('导出失败: ' + (e.message || '文件生成失败'))
  }
}

// 导出PDF
const exportPdf = async (reportId) => {
  try {
    const res = await request.get(`/report/export/pdf/${reportId}`, {
      responseType: 'blob'
    })
    
    // 检查是否是错误响应(Blob可能包含错误信息)
    if (res.data.type === 'application/json') {
      const text = await res.data.text()
      const errorData = JSON.parse(text)
      ElMessage.error('导出失败: ' + (errorData.message || '未知错误'))
      return
    }
    
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `评价报告_${reportId}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出PDF失败:', e)
    ElMessage.error('导出失败: ' + (e.message || '文件生成失败'))
  }
}

// 报告分页切换
const handleReportPageChange = () => {
  if (reportFilterStudent.value || reportFilterTask.value) {
    loadFilteredReportList()
  } else {
    loadReports()
  }
}

// 多选处理
const handleSelectionChange = (selection) => {
  selectedResults.value = selection
}

// 判断是否可以勾选（只有已评价的才能生成报告）
const canSelect = (row) => {
  return row.status === 2 && row.hasReport !== 1  // 已评价且未生成报告
}

// 清空选择
const clearSelection = () => {
  if (resultTableRef.value) {
    resultTableRef.value.clearSelection()
  }
}

// 批量生成选中的报告
const batchGenerateSelected = async () => {
  if (selectedResults.value.length === 0) {
    ElMessage.warning('请选择要生成的成果')
    return
  }
  
  generating.value = true
  const loadingMsg = ElMessage({
    message: `正在批量生成 ${selectedResults.value.length} 份报告，请稍候...`,
    type: 'info',
    duration: 0
  })
  
  try {
    // 1. 并行生成所有选中的报告
    const promises = selectedResults.value.map(result => 
      request.post(`/report/generate/${result.id}`)
    )
    const results = await Promise.allSettled(promises)
    
    // 2. 统计成功和失败数量
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount = results.filter(r => r.status === 'rejected').length
    
    loadingMsg.close()
    
    if (failCount === 0) {
      ElMessage.success(`成功生成 ${successCount} 份报告`)
    } else {
      ElMessage.warning(`成功 ${successCount} 份，失败 ${failCount} 份`)
    }
    
    // 3. 清空选择并刷新列表
    clearSelection()
    reportPageNum.value = 1
    try {
      await loadReports()
      await loadResults()
    } catch (refreshError) {
      console.warn('刷新列表失败，但报告已生成:', refreshError)
    }
  } catch (e) {
    loadingMsg.close()
    ElMessage.error('批量生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

// 查看已生成报告的详情
const viewReportDetail = async (reportId) => {
  if (!reportId) {
    ElMessage.warning('报告ID不存在')
    return
  }
  
  try {
    const res = await request.get(`/report/${reportId}`)
    currentReport.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取报告详情失败')
  }
}

// 表格行样式：已生成的行置灰
const tableRowClassName = ({ row }) => {
  if (row.hasReport === 1) {
    return 'report-generated-row'
  }
  return ''
}

onMounted(() => {
  loadReports()
  loadResults()
  loadTasks()
  loadStudents()
})
</script>

<style scoped>
/* 已生成报告的行样式：置灰 */
:deep(.report-generated-row) {
  background-color: #f5f7fa !important;
  opacity: 0.7;
}

:deep(.report-generated-row:hover) {
  opacity: 1;
}

:deep(.report-generated-row td) {
  color: #909399 !important;
}
</style>
