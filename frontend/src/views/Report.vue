<!--
  报表中心页面（报告生成、批量生成、Excel/PDF导出、筛选）
-->
<template>
  <div>
    <!-- 生成报表 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>生成报表</span>
          <el-alert title="调整AI/教师评分占比后，点击'生成报表'按钮，学生即可在成绩中心查看最终成绩" type="info" :closable="false" style="width: auto; margin: 0" />
        </div>
      </template>
      
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
          v-if="userRole === 'admin'"
          v-model="filterTeacher" 
          placeholder="选择教师" 
          clearable 
          style="width: 150px"
          @change="() => { resultPageNum = 1; loadResults() }"
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
        <el-button type="primary" @click="resetFilter">重置筛选</el-button>
      </div>
      
      <!-- AI评分成果列表 -->
      <el-table 
        :data="resultList" 
        border 
        stripe 
        v-loading="loadingResults"
      >
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (resultPageNum - 1) * resultPageSize + i + 1" />
        
        <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="90" />
        <el-table-column prop="taskTitle" label="实训任务" min-width="140" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="100" v-if="userRole === 'admin'" />
        <el-table-column label="AI评分" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.aiScore" :type="row.aiScore >= 80 ? 'success' : row.aiScore >= 60 ? 'warning' : 'danger'">
              {{ row.aiScore.toFixed(1) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="教师评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.teacherScore">{{ row.teacherScore.toFixed(1) }}</span>
            <el-tag v-else type="danger" size="small">待评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI/教师占比" width="220" align="center">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 4px;">
              <span style="font-size: 12px; color: #409eff; min-width: 30px; text-align: right;">AI {{ 10 - (row.teacherScoreRatio ?? 0) }}</span>
              <el-slider
                v-model="row.teacherScoreRatio"
                :min="0"
                :max="10"
                :step="1"
                :show-tooltip="false"
                style="flex: 1;"
                @change="handleRatioChange(row)"
              />
              <span style="font-size: 12px; color: #67c23a; min-width: 40px;">教师 {{ row.teacherScoreRatio ?? 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="最终得分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="calcFinalScore(row) >= 80 ? 'success' : calcFinalScore(row) >= 60 ? 'warning' : 'danger'">
              {{ calcFinalScore(row).toFixed(1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" sortable />
        <el-table-column label="操作" width="300" align="center">
          <template #default="{ row }">
            <el-button v-if="!row.reportId" size="small" type="success" @click="generateReport(row)" :loading="generating">生成报表</el-button>
            <el-button v-else size="small" type="info" @click="viewReportDetail(row.reportId, row.id)">查看报告</el-button>
            <el-button size="small" type="primary" @click="generateReportForStudent(row)" :loading="generating">生成报告</el-button>
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
        <el-table-column label="AI评分" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
              {{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="教师评分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.teacherScore">{{ row.teacherScore.toFixed(1) }}</span>
            <el-tag v-else type="danger" size="small">待评分</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最终得分" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.finalScore" :type="row.finalScore >= 80 ? 'success' : row.finalScore >= 60 ? 'warning' : 'danger'">
              {{ row.finalScore.toFixed(1) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="生成时间" width="180" sortable />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewDetail(row)">查看详情</el-button>
            <el-button size="small" type="info" @click="exportExcel(row.id)">导出Excel</el-button>
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
        <el-button type="success" @click="regenerateReport(currentReport)" :loading="generating">生成报表</el-button>
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

    <!-- 教师评分对话框 -->
    <el-dialog v-model="showTeacherScoreDialog" title="教师评分与占比设置" width="600px">
      <el-alert :title="'成果：' + (teacherScoreForm.fileName || '')" type="info" :closable="false" style="margin-bottom: 15px" />
      
      <el-form :model="teacherScoreForm" label-width="100px">
        <el-form-item label="AI评分">
          <el-tag :type="teacherScoreForm.aiScore >= 80 ? 'success' : teacherScoreForm.aiScore >= 60 ? 'warning' : 'danger'" size="large">
            {{ teacherScoreForm.aiScore ? teacherScoreForm.aiScore.toFixed(1) : '-' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item label="教师评分" required>
          <el-input-number v-model="teacherScoreForm.teacherScore" :min="0" :max="100" :precision="1" style="width: 200px" />
        </el-form-item>
        
        <el-form-item label="AI/教师占比">
          <div style="display: flex; align-items: center; gap: 10px; width: 100%">
            <span style="min-width: 60px">AI {{ teacherScoreForm.ratio * 100 }}%</span>
            <el-slider
              v-model="teacherScoreForm.ratio"
              :min="0"
              :max="1"
              :step="0.05"
              :show-tooltip="false"
              style="flex: 1;"
            />
            <span style="min-width: 80px">教师 {{ ((1 - teacherScoreForm.ratio) * 100).toFixed(0) }}%</span>
          </div>
        </el-form-item>
        
        <el-form-item label="最终得分">
          <el-tag :type="calculatedFinalScore >= 80 ? 'success' : calculatedFinalScore >= 60 ? 'warning' : 'danger'" size="large">
            {{ calculatedFinalScore.toFixed(1) }}
          </el-tag>
          <span style="margin-left: 10px; color: #909399; font-size: 12px">
            = AI评分({{ teacherScoreForm.aiScore?.toFixed(1) }}) × {{ (teacherScoreForm.ratio * 100).toFixed(0) }}% + 教师评分({{ teacherScoreForm.teacherScore?.toFixed(1) }}) × {{ ((1 - teacherScoreForm.ratio) * 100).toFixed(0) }}%
          </span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showTeacherScoreDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTeacherScore" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Search, Check, Clock } from '@element-plus/icons-vue'

const userRole = ref('')
const teacherList = ref([])
const filterTeacher = ref('')
const reportList = ref([])
const resultList = ref([])
const taskList = ref([])
const studentList = ref([])
const detailVisible = ref(false)
const showBatchGenerateDialog = ref(false)
const showTeacherScoreDialog = ref(false)
const currentReport = ref({})
const selectedTaskId = ref(null)
const loading = ref(false)
const loadingResults = ref(false)
const generating = ref(false)
const submitting = ref(false)

// 教师评分表单
const teacherScoreForm = ref({
  reportId: null,
  fileName: '',
  aiScore: 0,
  teacherScore: 0,
  ratio: 1  // 默认AI占100%，教师占0%
})

// 计算最终得分
const calculatedFinalScore = computed(() => {
  const aiScore = teacherScoreForm.value.aiScore || 0
  const teacherScore = teacherScoreForm.value.teacherScore || 0
  const ratio = teacherScoreForm.value.ratio ?? 1
  return aiScore * ratio + teacherScore * (1 - ratio)
})

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
  filterTeacher.value = ''
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

// 加载教师列表
const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) {
    console.error('加载教师列表失败:', e)
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
    const list = res.data.list || []
    
    // 为每个报告获取最终得分数据
    for (const report of list) {
      if (report.id) {
        try {
          const detailRes = await request.get(`/report/detail/${report.id}`)
          if (detailRes.data) {
            report.finalScore = detailRes.data.finalScore
            report.teacherScore = detailRes.data.teacherScore
            report.aiScore = detailRes.data.aiScore
          }
        } catch (e) {
          console.error(`获取报告${report.id}详情失败:`, e)
        }
      }
    }
    
    reportList.value = list
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
        status: 2, // 只显示已评价的成果
        fileName: searchText.value || undefined,
        teacherId: filterTeacher.value || undefined
      }
    })
    
    let results = res.data.list || []
    resultTotal.value = res.data.total || 0
    
    // 一次性加载所有报告，做本地映射
    const reportRes = await request.get('/report/list')
    const allReports = reportRes.data || []
    
    // 创建报告映射：key = "taskId_studentId"
    const reportMap = new Map()
    allReports.forEach(report => {
      const key = `${report.taskId}_${report.studentId}`
      reportMap.set(key, report)
    })
    
    // 一次性加载所有评价记录，做本地映射
    const evalRes = await request.get('/evaluate/records/page', {
      params: { pageNum: 1, pageSize: 10000 }
    })
    const allRecords = evalRes.data?.list || []
    
    // 按resultId分组
    const recordsByResult = new Map()
    allRecords.forEach(record => {
      const rid = record.resultId
      if (!recordsByResult.has(rid)) {
        recordsByResult.set(rid, [])
      }
      recordsByResult.get(rid).push(record)
    })
    
    // 为每个成果计算AI评分和匹配报告数据
    results = results.map(result => {
      // 计算AI加权总分
      const records = recordsByResult.get(result.id) || []
      let aiScore = 0
      if (records.length > 0) {
        let totalWeight = 0
        let weightedSum = 0
        records.forEach(record => {
          const weight = record.indicatorWeight || 1
          weightedSum += (record.aiScore || 0) * weight
          totalWeight += weight
        })
        aiScore = totalWeight > 0 ? weightedSum / totalWeight : 0
      }
      
      // 匹配报告
      const key = `${result.taskId}_${result.studentId}`
      const report = reportMap.get(key)
      
      return {
        ...result,
        aiScore: aiScore,
        teacherScore: report?.teacherScore || null,
        teacherScoreRatio: report?.teacherScoreRatio != null ? report.teacherScoreRatio * 10 : 0, // 默认0=AI占10
        reportId: report?.id || null,
        reportCreateTime: report?.createTime || null,
        finalScore: null // 由前端实时计算
      }
    })
    
    // 排序：未评价在前，已评价按操作时间排序
    results.sort((a, b) => {
      const aEvaluated = a.teacherScore ? 1 : 0
      const bEvaluated = b.teacherScore ? 1 : 0
      // 未评价在前
      if (aEvaluated !== bEvaluated) {
        return aEvaluated - bEvaluated
      }
      // 已评价按操作时间倒序
      if (aEvaluated && bEvaluated) {
        const timeA = new Date(a.reportCreateTime || a.createTime).getTime()
        const timeB = new Date(b.reportCreateTime || b.createTime).getTime()
        return timeB - timeA
      }
      // 都未评价按提交时间倒序
      const timeA = new Date(a.createTime).getTime()
      const timeB = new Date(b.createTime).getTime()
      return timeB - timeA
    })
    
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
// 生成报告（从第一区域调用）
const generateReport = (row) => {
  generateSingleReport(row.id)
}

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

// 计算最终得分：AI分 × AI占比 + 教师分 × 教师占比
// teacherScoreRatio 是 0-10 的值，表示教师占比挡位
// 挡位=0: AI占10/10=100%, 教师占0/10=0%
// 挡位=10: AI占0/10=0%, 教师占10/10=100%
const calcFinalScore = (row) => {
  const aiScore = row.aiScore || 0
  const teacherScore = row.teacherScore || 0
  const teacherRatio = row.teacherScoreRatio ?? 0  // 0-10，默认0表示教师占0/10
  const aiRatio = 10 - teacherRatio
  // 最终分 = AI分 × AI挡位/10 + 教师分 × 教师挡位/10
  return aiScore * (aiRatio / 10) + teacherScore * (teacherRatio / 10)
}

// 拖动杆变化时，实时计算最终得分并自动提交到后端
const handleRatioChange = async (row) => {
  // 实时计算最终得分（前端已响应式更新）
  // 如果有报告ID，自动提交比例到后端
  if (row.reportId) {
    try {
      await request.put('/report/teacher-score', {
        reportId: row.reportId,
        teacherScore: row.teacherScore || 0,
        ratio: (row.teacherScoreRatio ?? 0) / 10  // 转为0-1
      })
    } catch (e) {
      console.error('保存比例失败:', e)
    }
  }
}

// 打开教师评分对话框
const openTeacherScore = (row) => {
  // teacherScoreRatio是0-10的值，对话框中的ratio是0-1
  const ratio01 = (row.teacherScoreRatio ?? 0) / 10
  teacherScoreForm.value = {
    reportId: row.reportId || row.id,
    fileName: row.fileName,
    aiScore: row.aiScore || 0,
    teacherScore: row.teacherScore || 0,
    ratio: ratio01
  }
  showTeacherScoreDialog.value = true
}

// 提交教师评分
const submitTeacherScore = async () => {
  if (!teacherScoreForm.value.teacherScore) {
    ElMessage.warning('请输入教师评分')
    return
  }
  
  submitting.value = true
  try {
    await request.put('/report/teacher-score', {
      reportId: teacherScoreForm.value.reportId,
      teacherScore: teacherScoreForm.value.teacherScore,
      ratio: teacherScoreForm.value.ratio
    })
    ElMessage.success('评分成功')
    showTeacherScoreDialog.value = false
    loadResults()  // 刷新第一部分的AI评分成果列表（显示教师评分）
    loadReports()  // 刷新第二部分的报告列表
  } catch (e) {
    console.error('评分失败:', e)
    ElMessage.error('评分失败：' + (e.response?.data?.msg || e.message))
  } finally {
    submitting.value = false
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
const viewReportDetail = async (reportId, resultId = null) => {
  if (!reportId) {
    ElMessage.warning('报告ID不存在')
    return
  }
  
  try {
    const res = await request.get(`/report/${reportId}`)
    currentReport.value = res.data || {}
    // 保存resultId以便重新生成报表
    if (resultId) {
      currentReport.value.resultId = resultId
    }
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取报告详情失败')
  }
}

// 重新生成报表（从报告详情对话框调用）
const regenerateReport = (report) => {
  if (report.resultId) {
    generateSingleReport(report.resultId)
  } else {
    // 如果没有resultId，尝试通过taskId和studentId查找
    ElMessage.warning('无法重新生成：缺少成果ID')
  }
}

// 生成报告（从生成报表区域调用，提交到学生端）
const generateReportForStudent = async (row) => {
  if (!row.taskId || !row.studentId) {
    ElMessage.warning('缺少任务或学生信息')
    return
  }
  
  generating.value = true
  try {
    await request.post('/report/generate-by-task-student', {
      taskId: row.taskId,
      studentId: row.studentId
    })
    ElMessage.success('报告已生成并提交给学生')
    
    // 刷新列表
    await loadReports()
    await loadResults()
  } catch (e) {
    ElMessage.error('生成失败：' + (e.response?.data?.message || e.message))
  } finally {
    generating.value = false
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
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') {
    loadTeachers()
  }
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
