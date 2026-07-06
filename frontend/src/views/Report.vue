<!--
  报表中心页面
  - 管理员: 统计仪表盘视角(全局报告统计、图表对比、筛选导出)
  - 教师: 操作视角(生成报告、调整评分、导出)
-->
<template>
  <div>
    <!-- ==================== 管理员统计仪表盘 ==================== -->
    <div v-if="userRole === 'admin'">
      <!-- 顶部统计卡片 -->
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reportStats.totalReports || 0 }}</div>
              <div class="stat-label">报告总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reportStats.avgFinalScore || 0 }}</div>
              <div class="stat-label">平均最终分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ reportStats.teacherScoredRate || 0 }}<span style="font-size:14px">%</span></div>
              <div class="stat-label">教师已评分率</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
              <el-icon :size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ scoreDistribution.excellent + scoreDistribution.good + scoreDistribution.pass + scoreDistribution.fail }}</div>
              <div class="stat-label">已评分报告</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区第一行 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="14">
          <el-card shadow="hover">
            <template #header><span class="card-title">各任务平均分对比</span></template>
            <div ref="taskScoreChartRef" style="height: 300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card shadow="hover">
            <template #header><span class="card-title">成绩等级分布</span></template>
            <div ref="gradeChartRef" style="height: 300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区第二行 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header><span class="card-title">各教师评分统计对比</span></template>
            <div ref="teacherScoreChartRef" style="height: 300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 底部报告列表 -->
      <el-row style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span class="card-title">全部报告列表</span>
                <el-button size="small" @click="loadAdminReports">刷新</el-button>
              </div>
            </template>
            <!-- 筛选 -->
            <div style="margin-bottom: 15px; display: flex; gap: 15px; flex-wrap: wrap">
              <el-select v-model="adminFilterTeacher" placeholder="选择教师" clearable style="width: 150px" @change="loadAdminReports">
                <el-option v-for="t in teacherList" :key="t.id" :label="t.realName" :value="t.id" />
              </el-select>
              <el-select v-model="adminFilterTask" placeholder="选择任务" clearable style="width: 200px" @change="loadAdminReports">
                <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
              </el-select>
              <el-button @click="adminFilterTeacher = ''; adminFilterTask = ''; loadAdminReports()">重置</el-button>
            </div>
            <el-table :data="adminReportList" border stripe v-loading="reportLoading">
              <el-table-column type="index" label="序号" width="60" align="center"
                :index="(i) => (adminReportPage - 1) * adminReportPageSize + i + 1" />
              <el-table-column prop="taskTitle" label="实训任务" min-width="160" show-overflow-tooltip />
              <el-table-column prop="studentName" label="学生" width="100" />
              <el-table-column label="AI评分" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'" size="small">
                    {{ row.totalScore ? Number(row.totalScore).toFixed(1) : '-' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="教师评分" width="100" align="center">
                <template #default="{ row }">
                  <span v-if="row.teacherScore">{{ Number(row.teacherScore).toFixed(1) }}</span>
                  <el-tag v-else type="info" size="small">待评分</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="生成时间" width="170" sortable />
              <el-table-column label="操作" width="180" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="viewAdminReport(row)">详情</el-button>
                  <el-button size="small" type="info" @click="exportExcel(row.id)">导出</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="adminReportPage"
              v-model:page-size="adminReportPageSize"
              :page-sizes="[10, 20, 50]"
              :total="adminReportTotal"
              layout="total, sizes, prev, pager, next"
              style="margin-top: 15px; justify-content: flex-end"
              @size-change="loadAdminReports"
              @current-change="loadAdminReports"
            />
            <el-empty v-if="adminReportList.length === 0 && !reportLoading" description="暂无报告数据" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 报告详情对话框 -->
      <el-dialog v-model="adminDetailVisible" title="评价报告详情" width="900px" top="5vh">
        <el-alert :title="'实训任务：' + (currentAdminReport.taskTitle || '')" type="info" :closable="false" style="margin-bottom: 15px" />
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="学生姓名">{{ currentAdminReport.studentName }}</el-descriptions-item>
          <el-descriptions-item label="总分">
            <el-tag :type="currentAdminReport.totalScore >= 80 ? 'success' : currentAdminReport.totalScore >= 60 ? 'warning' : 'danger'">
              {{ currentAdminReport.totalScore ? Number(currentAdminReport.totalScore).toFixed(1) : '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentAdminReport.createTime }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>各指标评价详情</el-divider>
        <div v-if="parsedAdminReportData" style="background: #f5f7fa; padding: 20px; border-radius: 4px; max-height: 400px; overflow-y: auto">
          <el-table :data="parsedAdminReportData.indicators || parsedAdminReportData.records || []" border stripe>
            <el-table-column prop="indicatorName" label="评价指标" width="120" />
            <el-table-column prop="aiScore" label="AI评分" width="80" align="center" />
            <el-table-column prop="aiComment" label="AI评语" show-overflow-tooltip />
          </el-table>
        </div>
        <el-empty v-else description="报告数据解析失败" />
        <template #footer>
          <el-button @click="adminDetailVisible = false">关闭</el-button>
          <el-button type="info" @click="exportExcel(currentAdminReport.id)">导出Excel</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- ==================== 教师操作视图(保持原有) ==================== -->
    <div v-else>
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
          <el-input v-model="searchText" placeholder="输入文件名关键词搜索" clearable style="width: 250px"
            @keyup.enter="() => { resultPageNum = 1; loadResults() }" @clear="() => { resultPageNum = 1; loadResults() }" @input="debounceSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterStudent" placeholder="选择学生" clearable style="width: 150px" @change="() => { resultPageNum = 1; loadResults() }">
            <el-option v-for="s in studentList" :key="s.id" :label="s.realName" :value="s.id" />
          </el-select>
          <el-select v-model="filterTask" placeholder="选择实训任务" clearable style="width: 200px" @change="() => { resultPageNum = 1; loadResults() }">
            <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
          <el-button type="primary" @click="resetFilter">重置筛选</el-button>
        </div>
        <!-- AI评分成果列表 -->
        <el-table :data="resultList" border stripe v-loading="loadingResults">
          <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (resultPageNum - 1) * resultPageSize + i + 1" />
          <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
          <el-table-column prop="studentName" label="提交人" width="90" />
          <el-table-column prop="taskTitle" label="实训任务" min-width="140" show-overflow-tooltip />
          <el-table-column label="AI评分" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.aiScore" :type="row.aiScore >= 80 ? 'success' : row.aiScore >= 60 ? 'warning' : 'danger'">{{ row.aiScore.toFixed(1) }}</el-tag>
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
                <el-slider v-model="row.teacherScoreRatio" :min="0" :max="10" :step="1" :show-tooltip="false" style="flex: 1;" @change="handleRatioChange(row)" />
                <span style="font-size: 12px; color: #67c23a; min-width: 40px;">教师 {{ row.teacherScoreRatio ?? 0 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="最终得分" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="calcFinalScore(row) >= 80 ? 'success' : calcFinalScore(row) >= 60 ? 'warning' : 'danger'">{{ calcFinalScore(row).toFixed(1) }}</el-tag>
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
        <el-pagination v-model:current-page="resultPageNum" v-model:page-size="resultPageSize" :page-sizes="[5, 10, 20]" :total="resultTotal"
          layout="total, sizes, prev, pager, next" style="margin-top: 20px; justify-content: flex-end" @size-change="loadResults" @current-change="loadResults" />
      </el-card>

      <!-- 已生成报告列表 -->
      <el-card style="margin-top: 20px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>报告列表</span>
            <el-button size="small" @click="loadReports">刷新</el-button>
          </div>
        </template>
        <div style="margin-bottom: 15px; display: flex; gap: 15px; flex-wrap: wrap; align-items: center">
          <el-select v-model="reportFilterStudent" placeholder="选择学生" clearable style="width: 150px" @change="() => { reportPageNum = 1; loadFilteredReports() }">
            <el-option v-for="s in studentList" :key="s.id" :label="s.realName" :value="s.id" />
          </el-select>
          <el-select v-model="reportFilterTask" placeholder="选择实训任务" clearable style="width: 200px" @change="() => { reportPageNum = 1; loadFilteredReports() }">
            <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
          <el-button @click="resetReportFilter">重置筛选</el-button>
        </div>
        <el-table :data="reportList" border stripe v-loading="loading">
          <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (reportPageNum - 1) * reportPageSize + i + 1" />
          <el-table-column prop="taskTitle" label="实训任务" min-width="160" show-overflow-tooltip />
          <el-table-column prop="fileName" label="成果文件" min-width="180" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column label="AI评分" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">{{ row.totalScore ? row.totalScore.toFixed(1) : '-' }}</el-tag>
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
              <el-tag v-if="row.finalScore" :type="row.finalScore >= 80 ? 'success' : row.finalScore >= 60 ? 'warning' : 'danger'">{{ row.finalScore.toFixed(1) }}</el-tag>
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
        <el-pagination v-model:current-page="reportPageNum" v-model:page-size="reportPageSize" :page-sizes="[5, 10, 20]" :total="reportTotal"
          layout="total, sizes, prev, pager, next" style="margin-top: 20px; justify-content: flex-end" @size-change="handleReportPageChange" @current-change="handleReportPageChange" />
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
              <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showBatchGenerateDialog = false">取消</el-button>
          <el-button type="primary" @click="generateBatchReports" :loading="generating">批量生成</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Search, Document, CircleCheck, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const router = useRouter()

const userRole = ref('')

// ============ 管理员仪表盘相关 ============
const reportStats = reactive({ totalReports: 0, avgFinalScore: 0, teacherScoredRate: 0 })
const scoreDistribution = reactive({ excellent: 0, good: 0, pass: 0, fail: 0 })
const chartLoading = ref(false)
const taskScoreChartRef = ref(null)
const gradeChartRef = ref(null)
const teacherScoreChartRef = ref(null)
let taskScoreChart = null, gradeChart = null, teacherScoreChart = null

// 管理员报告列表
const adminReportList = ref([])
const adminReportPage = ref(1)
const adminReportPageSize = ref(10)
const adminReportTotal = ref(0)
const adminFilterTeacher = ref('')
const adminFilterTask = ref('')
const reportLoading = ref(false)
const adminDetailVisible = ref(false)
const currentAdminReport = ref({})
const teacherList = ref([])
const taskList = ref([])

const parsedAdminReportData = computed(() => {
  if (!currentAdminReport.value.reportData) return null
  try { return JSON.parse(currentAdminReport.value.reportData) } catch { return null }
})

const loadAdminReportStats = async () => {
  try {
    const res = await request.get('/dashboard/admin/report/stats')
    const data = res.data || {}
    Object.assign(reportStats, {
      totalReports: data.totalReports || 0,
      avgFinalScore: data.avgFinalScore || 0,
      teacherScoredRate: data.teacherScoredRate || 0
    })
  } catch (e) { console.error('加载报表统计失败:', e) }
}

const loadAdminReports = async () => {
  reportLoading.value = true
  try {
    const res = await request.get('/report/page', {
      params: { pageNum: adminReportPage.value, pageSize: adminReportPageSize.value }
    })
    let list = res.data.list || []
    adminReportTotal.value = res.data.total || 0
    // 前端筛选教师/任务
    if (adminFilterTeacher.value || adminFilterTask.value) {
      // 需要获取全量数据进行筛选
      const allRes = await request.get('/report/list')
      let allList = allRes.data || []
      if (adminFilterTeacher.value) {
        // 通过报告关联的教师过滤 - 这里简单按task关联
        // 由于report没有直接的teacherId，暂不做教师筛选
      }
      if (adminFilterTask.value) {
        allList = allList.filter(r => r.taskId === adminFilterTask.value)
      }
      adminReportTotal.value = allList.length
      const start = (adminReportPage.value - 1) * adminReportPageSize.value
      adminReportList.value = allList.slice(start, start + adminReportPageSize.value)
    } else {
      adminReportList.value = list
    }
  } catch (e) { console.error('加载报告列表失败:', e) }
  finally { reportLoading.value = false }
}

const viewAdminReport = async (row) => {
  try {
    const res = await request.get(`/report/${row.id}`)
    currentAdminReport.value = res.data || row
    adminDetailVisible.value = true
  } catch (e) { ElMessage.error('获取报告详情失败') }
}

const initAdminReportCharts = async () => {
  await nextTick()
  chartLoading.value = true
  try {
    const [taskRes, teacherRes] = await Promise.all([
      request.get('/dashboard/admin/report/by-task'),
      request.get('/dashboard/admin/report/by-teacher')
    ])
    const taskData = taskRes.data || []
    const teacherData = teacherRes.data || []

    // 1. 各任务平均分对比(柱状图)
    if (taskScoreChartRef.value) {
      taskScoreChart = echarts.init(taskScoreChartRef.value)
      taskScoreChart.setOption({
        tooltip: { trigger: 'axis', formatter: '{b}<br/>平均分: {c}' },
        grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', data: taskData.map(t => t.taskName), axisLabel: { interval: 0, rotate: 30, width: 80, overflow: 'truncate' } },
        yAxis: { type: 'value', name: '平均分', max: 100 },
        series: [{
          type: 'bar',
          data: taskData.map(t => Number(t.avgScore) || 0),
          itemStyle: {
            color: (params) => {
              const v = params.value
              if (v >= 80) return '#67c23a'
              if (v >= 60) return '#e6a23c'
              return '#f56c6c'
            }
          },
          barWidth: '45%',
          label: { show: true, position: 'top', formatter: '{c}' }
        }]
      })
    }

    // 2. 成绩等级分布(饼图)
    if (gradeChartRef.value) {
      gradeChart = echarts.init(gradeChartRef.value)
      // 从报告列表计算等级分布
      const allReports = await request.get('/report/list')
      const reports = allReports.data || []
      let excellent = 0, good = 0, pass = 0, fail = 0
      reports.forEach(r => {
        const score = Number(r.totalScore || 0)
        if (score >= 90) excellent++
        else if (score >= 80) good++
        else if (score >= 60) pass++
        else if (score > 0) fail++
      })
      Object.assign(scoreDistribution, { excellent, good, pass, fail })
      gradeChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}份 ({d}%)' },
        legend: { bottom: 0 },
        color: ['#67c23a', '#409eff', '#e6a23c', '#f56c6c'],
        series: [{
          type: 'pie', radius: ['40%', '70%'],
          data: [
            { name: '优秀(90+)', value: excellent },
            { name: '良好(80-89)', value: good },
            { name: '及格(60-79)', value: pass },
            { name: '不及格(<60)', value: fail }
          ],
          emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
          label: { formatter: '{b}\n{d}%' }
        }]
      })
    }

    // 3. 各教师评分统计对比(横向柱状图)
    if (teacherScoreChartRef.value) {
      teacherScoreChart = echarts.init(teacherScoreChartRef.value)
      teacherScoreChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['平均分', '报告数'], top: 0 },
        grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
        xAxis: [
          { type: 'value', name: '平均分', max: 100 },
          { type: 'value', name: '报告数', position: 'top' }
        ],
        yAxis: { type: 'category', data: teacherData.map(t => t.teacherName).reverse(), axisLabel: { width: 80, overflow: 'truncate' } },
        series: [
          {
            name: '平均分', type: 'bar',
            data: teacherData.map(t => Number(t.avgScore) || 0).reverse(),
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                { offset: 0, color: '#43e97b' }, { offset: 1, color: '#38f9d7' }
              ])
            },
            barWidth: '40%',
            label: { show: true, position: 'right', formatter: '{c}' }
          },
          {
            name: '报告数', type: 'bar', xAxisIndex: 1,
            data: teacherData.map(t => Number(t.reportCount) || 0).reverse(),
            itemStyle: { color: '#c0c4cc' },
            barWidth: '25%',
            label: { show: true, position: 'right', formatter: '{c}份' }
          }
        ]
      })
    }
  } catch (e) { console.error('图表初始化失败:', e) }
  finally { chartLoading.value = false }
}

// ============ 教师视图相关(保持原有逻辑) ============
const reportList = ref([])
const resultList = ref([])
const detailVisible = ref(false)
const showBatchGenerateDialog = ref(false)
const currentReport = ref({})
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
// 筛选条件
const searchText = ref('')
const filterStudent = ref('')
const filterTask = ref('')
const filterStatus = ref(null)
const studentList = ref([])
const reportFilterStudent = ref('')
const reportFilterTask = ref('')

const parsedReportData = computed(() => {
  if (!currentReport.value.reportData) return null
  try { return JSON.parse(currentReport.value.reportData) } catch { return null }
})

let searchTimer = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { resultPageNum.value = 1; loadResults() }, 500)
}

const resetFilter = () => {
  searchText.value = ''; filterStudent.value = ''; filterTask.value = ''
  filterStatus.value = null; resultPageNum.value = 1; loadResults()
}
const resetReportFilter = () => {
  reportFilterStudent.value = ''; reportFilterTask.value = ''; reportPageNum.value = 1; loadFilteredReports()
}
const loadFilteredReports = () => {
  if (reportFilterStudent.value || reportFilterTask.value) loadFilteredReportList()
  else loadReports()
}

const loadStudents = async () => {
  try { const res = await request.get('/user/student/list'); studentList.value = res.data || [] } catch (e) {}
}
const loadTeachers = async () => {
  try { const res = await request.get('/user/teacher/list'); teacherList.value = res.data || [] } catch (e) {}
}
const loadTasks = async () => {
  try { const res = await request.get('/task/list'); taskList.value = res.data || [] } catch (e) {}
}

const loadReports = async () => {
  loading.value = true
  try {
    const res = await request.get('/report/page', { params: { pageNum: reportPageNum.value, pageSize: reportPageSize.value } })
    const list = res.data.list || []
    for (const report of list) {
      if (report.id) {
        try {
          const detailRes = await request.get(`/report/detail/${report.id}`)
          if (detailRes.data) {
            report.finalScore = detailRes.data.finalScore
            report.teacherScore = detailRes.data.teacherScore
            report.aiScore = detailRes.data.aiScore
          }
        } catch (e) {}
      }
    }
    reportList.value = list
    reportTotal.value = res.data.total || 0
  } catch (e) { console.error('加载报告失败:', e) }
  finally { loading.value = false }
}

const loadFilteredReportList = async () => {
  loading.value = true
  try {
    const res = await request.get('/report/list')
    let list = res.data || []
    if (reportFilterStudent.value) list = list.filter(r => r.studentId === reportFilterStudent.value)
    if (reportFilterTask.value) list = list.filter(r => r.taskId === reportFilterTask.value)
    reportTotal.value = list.length
    const start = (reportPageNum.value - 1) * reportPageSize.value
    reportList.value = list.slice(start, start + reportPageSize.value)
  } catch (e) { console.error('加载筛选报告失败:', e) }
  finally { loading.value = false }
}

const loadResults = async () => {
  loadingResults.value = true
  try {
    const res = await request.get('/result/page', {
      params: { pageNum: resultPageNum.value, pageSize: resultPageSize.value, status: 2,
        studentId: filterStudent.value || undefined, taskId: filterTask.value || undefined,
        fileName: searchText.value || undefined }
    })
    let results = res.data.list || []
    resultTotal.value = res.data.total || 0
    const reportRes = await request.get('/report/list')
    const allReports = reportRes.data || []
    const reportMap = new Map()
    allReports.forEach(report => { reportMap.set(`${report.taskId}_${report.studentId}`, report) })
    const evalRes = await request.get('/evaluate/records/page', { params: { pageNum: 1, pageSize: 10000 } })
    const allRecords = evalRes.data?.list || []
    const recordsByResult = new Map()
    allRecords.forEach(record => {
      if (!recordsByResult.has(record.resultId)) recordsByResult.set(record.resultId, [])
      recordsByResult.get(record.resultId).push(record)
    })
    results = results.map(result => {
      const records = recordsByResult.get(result.id) || []
      let aiScore = 0
      if (records.length > 0) {
        let totalWeight = 0, weightedSum = 0
        records.forEach(record => {
          const weight = record.indicatorWeight || 1
          weightedSum += (record.aiScore || 0) * weight; totalWeight += weight
        })
        aiScore = totalWeight > 0 ? weightedSum / totalWeight : 0
      }
      const report = reportMap.get(`${result.taskId}_${result.studentId}`)
      return { ...result, aiScore, teacherScore: report?.teacherScore || null,
        teacherScoreRatio: report?.teacherScoreRatio != null ? report.teacherScoreRatio * 10 : 0,
        reportId: report?.id || null, reportCreateTime: report?.createTime || null, finalScore: null }
    })
    results.sort((a, b) => {
      const aE = a.teacherScore ? 1 : 0, bE = b.teacherScore ? 1 : 0
      if (aE !== bE) return aE - bE
      if (aE && bE) return new Date(b.reportCreateTime || b.createTime).getTime() - new Date(a.reportCreateTime || a.createTime).getTime()
      return new Date(b.createTime).getTime() - new Date(a.createTime).getTime()
    })
    resultList.value = results
  } catch (e) { console.error('加载成果失败:', e) }
  finally { loadingResults.value = false }
}

const calcFinalScore = (row) => {
  const aiScore = row.aiScore || 0, teacherScore = row.teacherScore || 0
  const teacherRatio = row.teacherScoreRatio ?? 0
  return aiScore * ((10 - teacherRatio) / 10) + teacherScore * (teacherRatio / 10)
}

const handleRatioChange = async (row) => {
  if (row.reportId) {
    try {
      await request.put('/report/teacher-score', {
        reportId: row.reportId, teacherScore: row.teacherScore || 0, ratio: (row.teacherScoreRatio ?? 0) / 10
      })
    } catch (e) { console.error('保存比例失败:', e) }
  }
}

const generateReport = (row) => generateSingleReport(row.id)
const generateSingleReport = async (resultId) => {
  if (!resultId) { ElMessage.warning('请选择成果'); return }
  generating.value = true
  try {
    await request.post(`/report/generate/${resultId}`)
    ElMessage.success('报告生成成功！')
    reportPageNum.value = 1
    try { await loadReports(); await loadResults() } catch (e) {}
  } catch (e) { ElMessage.error('生成失败：' + (e.response?.data?.message || e.message)) }
  finally { generating.value = false }
}

const generateBatchReports = async () => {
  if (!selectedTaskId.value) { ElMessage.warning('请选择任务'); return }
  generating.value = true
  const loadingMsg = ElMessage({ message: '正在批量生成报告，请稍候...', type: 'info', duration: 0 })
  try {
    const res = await request.post(`/report/generate/task/${selectedTaskId.value}`)
    loadingMsg.close(); ElMessage.success(`成功生成 ${res.data.successCount} 份报告`)
    showBatchGenerateDialog.value = false
    reportPageNum.value = 1
    try { await loadReports(); await loadResults() } catch (e) {}
  } catch (e) { loadingMsg.close(); ElMessage.error('批量生成失败') }
  finally { generating.value = false }
}

const generateReportForStudent = async (row) => {
  if (!row.taskId || !row.studentId) { ElMessage.warning('缺少任务或学生信息'); return }
  generating.value = true
  try {
    await request.post('/report/generate-by-task-student', { taskId: row.taskId, studentId: row.studentId })
    ElMessage.success('报告已生成并提交给学生')
    await loadReports(); await loadResults()
  } catch (e) { ElMessage.error('生成失败') }
  finally { generating.value = false }
}

const viewDetail = async (row) => {
  try { const res = await request.get(`/report/${row.id}`); currentReport.value = res.data || row; detailVisible.value = true }
  catch (e) { currentReport.value = row; detailVisible.value = true }
}

const viewReportDetail = async (reportId, resultId = null) => {
  if (!reportId) { ElMessage.warning('报告ID不存在'); return }
  try {
    const res = await request.get(`/report/${reportId}`)
    currentReport.value = res.data || {}
    if (resultId) currentReport.value.resultId = resultId
    detailVisible.value = true
  } catch (e) { ElMessage.error('获取报告详情失败') }
}

const regenerateReport = (report) => {
  if (report.resultId) generateSingleReport(report.resultId)
  else ElMessage.warning('无法重新生成：缺少成果ID')
}

const exportExcel = async (reportId) => {
  try {
    const res = await request.get(`/report/export/excel/${reportId}`, { responseType: 'blob' })
    if (res.data.type === 'application/json') {
      const text = await res.data.text(); ElMessage.error('导出失败: ' + (JSON.parse(text).message || '未知错误')); return
    }
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a'); link.href = url
    link.setAttribute('download', `评价报告_${reportId}.xlsx`)
    document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出Excel失败: ' + e.message) }
}

const exportPdf = async (reportId) => {
  try {
    const res = await request.get(`/report/export/pdf/${reportId}`, { responseType: 'blob' })
    if (res.data.type === 'application/json') {
      const text = await res.data.text(); ElMessage.error('导出失败: ' + (JSON.parse(text).message || '未知错误')); return
    }
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a'); link.href = url
    link.setAttribute('download', `评价报告_${reportId}.pdf`)
    document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) { ElMessage.error('导出PDF失败: ' + e.message) }
}

const handleReportPageChange = () => {
  if (reportFilterStudent.value || reportFilterTask.value) loadFilteredReportList()
  else loadReports()
}

// 管理员仪表盘自动刷新定时器（每30秒）
let adminRefreshTimer = null
const ADMIN_REFRESH_INTERVAL = 30000

const refreshAdminData = async () => {
  await Promise.all([loadAdminReportStats(), loadAdminReports()])
  // 重新初始化图表（含数据刷新）
  taskScoreChart?.dispose(); gradeChart?.dispose(); teacherScoreChart?.dispose()
  await initAdminReportCharts()
}

onMounted(async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') {
    // 管理员的报表统计已合并到“AI评分”页面，重定向过去
    router.push('/evaluate')
    return
  }
  loadReports(); loadResults(); loadTasks(); loadStudents()
})

onBeforeUnmount(() => {
  // 清除定时刷新
  if (adminRefreshTimer) clearInterval(adminRefreshTimer)
  taskScoreChart?.dispose(); gradeChart?.dispose(); teacherScoreChart?.dispose()
})
</script>

<style scoped>
.stat-card { text-align: center; border-radius: 12px; transition: all 0.3s; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
.stat-card :deep(.el-card__body) { padding: 25px 20px; display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 14px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center; color: white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.stat-info { text-align: left; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; line-height: 1; }
.stat-label { font-size: 13px; color: #909399; margin-top: 6px; }
.card-title { font-weight: bold; font-size: 15px; color: #303133; }
:deep(.el-card) { border-radius: 12px; }
</style>
