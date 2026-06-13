<template>
  <div>
    <!-- 生成报告区域 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>生成评价报告</span>
          <div>
            <el-button type="primary" @click="showGenerateDialog = true" :disabled="generating">生成报告</el-button>
            <el-button type="success" @click="showBatchGenerateDialog = true" :disabled="generating">批量生成</el-button>
          </div>
        </div>
      </template>
      
      <el-alert title="选择已评价的成果生成报告，报告可用于导出Excel/PDF" type="info" :closable="false" style="margin-bottom: 15px" />
      
      <!-- 成果列表 -->
      <el-table :data="resultList" border stripe v-loading="loadingResults">
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
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="generateSingleReport(row.id)" :loading="generating">
              生成报告
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
      <el-table :data="reportList" border stripe v-loading="loading">
        <el-table-column prop="taskTitle" label="实训任务" min-width="160" />
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="totalScore" label="总分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
              {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="180" />
        <el-table-column label="操作" width="220" align="center">
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
        @size-change="loadReports"
        @current-change="loadReports"
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

    <!-- 生成报告对话框（单个） -->
    <el-dialog v-model="showGenerateDialog" title="生成评价报告" width="500px">
      <el-alert title="选择已评价的成果生成报告" type="info" :closable="false" style="margin-bottom: 15px" />
      <el-form>
        <el-form-item label="选择成果">
          <el-select v-model="selectedResultId" placeholder="请选择成果" style="width: 100%">
            <el-option 
              v-for="item in evaluatedResults" 
              :key="item.id" 
              :label="`${item.fileName} - ${item.studentName} - ${item.taskTitle}`" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="generateSingleReport(selectedResultId)" :loading="generating">生成</el-button>
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

const reportList = ref([])
const resultList = ref([])
const taskList = ref([])
const detailVisible = ref(false)
const showGenerateDialog = ref(false)
const showBatchGenerateDialog = ref(false)
const currentReport = ref({})
const selectedResultId = ref(null)
const selectedTaskId = ref(null)
const loading = ref(false)
const loadingResults = ref(false)
const generating = ref(false)
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 报告列表分页
const reportPageNum = ref(1)
const reportPageSize = ref(10)
const reportTotal = ref(0)

// 解析报告数据
const parsedReportData = computed(() => {
  if (!currentReport.value.reportData) return null
  try {
    return JSON.parse(currentReport.value.reportData)
  } catch (e) {
    return null
  }
})

// 已评价的成果（status === 2）
const evaluatedResults = computed(() => {
  return resultList.value.filter(r => r.status === 2)
})

// 加载报告列表
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

// 加载成果列表
const loadResults = async () => {
  loadingResults.value = true
  try {
    const res = await request.get('/result/page', {
      params: {
        pageNum: resultPageNum.value,
        pageSize: resultPageSize.value
      }
    })
    resultList.value = res.data.list || []
    resultTotal.value = res.data.total || 0
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
    await request.post(`/report/generate/${resultId}`)
    ElMessage.success('报告生成成功！')
    showGenerateDialog.value = false
    loadReports()
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
  try {
    const res = await request.post(`/report/generate/task/${selectedTaskId.value}`)
    ElMessage.success(`成功生成 ${res.data.successCount} 份报告`)
    showBatchGenerateDialog.value = false
    loadReports()
  } catch (e) {
    ElMessage.error('批量生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
  }
}

// 查看详情
const viewDetail = (row) => {
  currentReport.value = row
  detailVisible.value = true
}

// 导出Excel
const exportExcel = async (reportId) => {
  try {
    const res = await request.get(`/report/export/excel/${reportId}`, {
      responseType: 'blob'
    })
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
    ElMessage.error('导出失败')
  }
}

// 导出PDF
const exportPdf = async (reportId) => {
  try {
    const res = await request.get(`/report/export/pdf/${reportId}`, {
      responseType: 'blob'
    })
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
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadReports()
  loadResults()
  loadTasks()
})
</script>
