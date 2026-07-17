<!--
  评价管理页面
  - 管理员: 统计仪表盘视角(全局总览、图表分析、按任务/教师汇总)
  - 教师: 操作视角(AI评分、评分记录)
-->
<template>
  <div>
    <!-- ==================== 管理员统一仪表盘视图 ==================== -->
    <div v-if="userRole === 'admin'">
      <!-- 统计卡片（4个核心指标） -->
      <el-row :gutter="20">
        <el-col :span="6" v-for="card in adminStatCards" :key="card.label">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" :style="{ background: card.gradient }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}<span v-if="card.suffix" style="font-size:14px">{{ card.suffix }}</span></div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表第一行：各任务评分对比 + 评分进度 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="15">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span class="card-title">各任务AI评分对比</span>
                <span style="font-size: 13px; color: #909399">共 {{ combinedTaskStats.length }} 个任务，展示前 10</span>
              </div>
            </template>
            <div ref="taskCompareChartRef" style="height: 300px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
        <el-col :span="9">
          <el-card shadow="hover">
            <template #header><span class="card-title">评分进度</span></template>
            <div style="padding: 10px 0">
              <!-- AI评分进度 -->
              <div style="margin-bottom: 28px">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
                  <span style="font-size: 14px; font-weight: 500">AI评分完成率</span>
                  <span style="font-size: 14px; color: #409eff; font-weight: bold">{{ aiCompletionRate }}%</span>
                </div>
                <el-progress :percentage="aiCompletionRate" :color="'#409eff'" :stroke-width="14" />
                <div style="font-size: 13px; color: #909399; margin-top: 4px">已评分 {{ evalStats.scoredCount || 0 }} / 总成果 {{ evalStats.totalCount || 0 }}</div>
              </div>
              <!-- 报告生成进度 -->
              <div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
                  <span style="font-size: 14px; font-weight: 500">报告生成率</span>
                  <span style="font-size: 14px; color: #67c23a; font-weight: bold">{{ reportRate }}%</span>
                </div>
                <el-progress :percentage="reportRate" :color="'#67c23a'" :stroke-width="14" />
                <div style="font-size: 13px; color: #909399; margin-top: 4px">已生成报告 {{ reportStats.totalReports || 0 }} / 总成果 {{ evalStats.totalCount || 0 }}</div>
              </div>
              <!-- 教师评分进度 -->
              <div style="margin-top: 28px">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px">
                  <span style="font-size: 14px; font-weight: 500">教师评分率</span>
                  <span style="font-size: 14px; color: #e6a23c; font-weight: bold">{{ reportStats.teacherScoredRate || 0 }}%</span>
                </div>
                <el-progress :percentage="Number(reportStats.teacherScoredRate) || 0" :color="'#e6a23c'" :stroke-width="14" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表第二行：各教师评分统计对比 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header><span class="card-title">各教师评分统计对比</span></template>
            <div ref="teacherCompareChartRef" style="height: 320px" v-loading="chartLoading"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 综合任务汇总表 -->
      <el-row style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span class="card-title">综合任务汇总</span>
                <el-button size="small" @click="refreshAdminData">刷新</el-button>
              </div>
            </template>
            <!-- 筛选条件 -->
            <div style="margin-bottom: 15px; display: flex; gap: 12px; flex-wrap: wrap; align-items: center">
              <el-input v-model="taskTableSearch" placeholder="搜索任务名称" clearable style="width: 180px"
                @input="filteredTaskTable = filterTaskTable()" @clear="filteredTaskTable = filterTaskTable()">
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-select v-model="taskTableTeacher" placeholder="授课教师" clearable style="width: 140px" @change="filteredTaskTable = filterTaskTable()">
                <el-option v-for="t in uniqueTeachersInTasks" :key="t" :label="t" :value="t" />
              </el-select>
              <el-select v-model="taskTableScoreRange" placeholder="分数范围" clearable style="width: 140px" @change="filteredTaskTable = filterTaskTable()">
                <el-option label="优秀(80+)" value="high" />
                <el-option label="良好(60-79)" value="mid" />
                <el-option label="不及格(<60)" value="low" />
              </el-select>
              <el-select v-model="taskTableStatus" placeholder="评分状态" clearable style="width: 140px" @change="filteredTaskTable = filterTaskTable()">
                <el-option label="已全部评分" value="done" />
                <el-option label="部分评分" value="partial" />
                <el-option label="未评分" value="none" />
              </el-select>
              <el-button @click="resetTaskTableFilter">重置</el-button>
            </div>
            <el-table :data="filteredTaskTable" border stripe v-loading="chartLoading">
              <el-table-column type="index" label="序号" width="60" align="center" />
              <el-table-column prop="taskName" label="实训任务" min-width="160" show-overflow-tooltip />
              <el-table-column prop="teacherName" label="授课教师" width="100" />
              <el-table-column prop="studentCount" label="提交人数" width="90" align="center" />
              <el-table-column label="AI平均分" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.avgAiScore >= 80 ? 'success' : row.avgAiScore >= 60 ? 'warning' : 'danger'" size="small">
                    {{ row.avgAiScore || '-' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报告平均分" width="110" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.avgReportScore >= 80 ? 'success' : row.avgReportScore >= 60 ? 'warning' : 'danger'" size="small">
                    {{ row.avgReportScore || '-' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="报告数" width="80" align="center">
                <template #default="{ row }">
                  <el-tag type="info" size="small">{{ row.reportCount || 0 }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="评分率" width="100" align="center">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.studentCount > 0 ? Math.round(row.aiScoredCount / row.studentCount * 100) : 0"
                    :color="getProgressColor(row.studentCount > 0 ? row.aiScoredCount / row.studentCount * 100 : 0)"
                    :stroke-width="8" style="width: 70px" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="openTaskDetail(row)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-if="filteredTaskTable.length === 0 && !chartLoading" description="暂无匹配数据" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 任务详情对话框（只读） -->
      <el-dialog v-model="taskDetailVisible" :title="'任务详情 - ' + (currentTaskDetail.taskName || '')" width="950px" top="5vh">
        <el-descriptions :column="4" border style="margin-bottom: 16px">
          <el-descriptions-item label="实训任务">{{ currentTaskDetail.taskName }}</el-descriptions-item>
          <el-descriptions-item label="授课教师">{{ currentTaskDetail.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="AI平均分">
            <el-tag :type="currentTaskDetail.avgAiScore >= 80 ? 'success' : currentTaskDetail.avgAiScore >= 60 ? 'warning' : 'danger'" size="small">
              {{ currentTaskDetail.avgAiScore || '-' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报告平均分">
            <el-tag :type="currentTaskDetail.avgReportScore >= 80 ? 'success' : currentTaskDetail.avgReportScore >= 60 ? 'warning' : 'danger'" size="small">
              {{ currentTaskDetail.avgReportScore || '-' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-table :data="taskDetailStudents" border stripe v-loading="taskDetailLoading" max-height="400">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="studentName" label="学生" width="100" />
          <el-table-column prop="fileName" label="成果文件" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="info" size="small">待评分</el-tag>
              <el-tag v-else-if="row.status === 1" type="warning" size="small">部分评分</el-tag>
              <el-tag v-else-if="row.status === 2" type="success" size="small">已评分</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="AI平均分" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.aiAvgScore != null" :type="row.aiAvgScore >= 80 ? 'success' : row.aiAvgScore >= 60 ? 'warning' : 'danger'" size="small">
                {{ row.aiAvgScore.toFixed(1) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="报告分" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.reportScore" type="success" size="small">{{ Number(row.reportScore).toFixed(1) }}</el-tag>
              <el-tag v-else type="info" size="small">无报告</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="160" />
        </el-table>
        <el-empty v-if="taskDetailStudents.length === 0 && !taskDetailLoading" description="该任务暂无学生提交" />
        <template #footer>
          <el-button @click="taskDetailVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>

    <!-- ==================== 教师操作视图(保持原有) ==================== -->
    <div v-else>
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
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterStudent" placeholder="选择学生" clearable style="width: 150px" @change="loadResults">
            <el-option v-for="s in studentList" :key="s.id" :label="s.realName" :value="s.id" />
          </el-select>
          <el-select v-model="filterTask" placeholder="选择实训任务" clearable style="width: 200px" @change="loadResults">
            <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="选择状态" clearable style="width: 150px" @change="loadResults">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
          <el-button type="primary" @click="resetFilter">重置筛选</el-button>
          <div style="flex-grow: 1"></div>
          <el-button type="warning" @click="openIndicatorChart">查看评分标准</el-button>
        </div>

        <!-- 成果列表 -->
        <el-table :data="resultList" border stripe v-loading="loadingResults">
          <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (resultPageNum - 1) * resultPageSize + i + 1" />
          <el-table-column prop="fileName" label="文件名" min-width="150" show-overflow-tooltip />
          <el-table-column prop="studentName" label="提交人" width="90" />
          <el-table-column prop="taskTitle" label="实训任务" min-width="140" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 0" type="info" size="small">待处理</el-tag>
              <el-tag v-else type="success" size="small">已处理</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="160" sortable />
          <el-table-column label="操作" width="260" align="center">
            <template #default="{ row }">
              <el-button size="small" type="info" @click="viewContent(row)">预览</el-button>
              <el-button size="small" @click="downloadSourceFile(row)">下载源文件</el-button>
              <el-button size="small" type="success" @click="runEvaluateDirect(row)" :loading="row.evaluating" :disabled="row.evaluating">AI评分</el-button>
            </template>
          </el-table-column>
        </el-table>

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
          <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (evaluatePageNum - 1) * evaluatePageSize + i + 1" />
          <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
          <el-table-column prop="studentName" label="提交人" width="100" sortable="custom" />
          <el-table-column label="评价指标" min-width="180">
            <template #default="{ row }">
              <div style="display: flex; gap: 5px; flex-wrap: wrap">
                <el-tag v-for="record in row.records" :key="record.id" type="info" size="small">{{ record.indicatorName }}</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="AI评分" width="90" align="center">
            <template #default="{ row }">{{ row.avgAiScore ? row.avgAiScore.toFixed(1) : '-' }}</template>
          </el-table-column>
          <el-table-column prop="evalTime" label="评价时间" width="160" sortable="custom" />
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="viewEvaluateDetail(row)" style="margin-left: 0">查看详情</el-button>
              <el-button size="small" type="warning" @click="reEvaluate(row)" :loading="reEvaluatingIds.has(row.resultId)">重新评分</el-button>
            </template>
          </el-table-column>
        </el-table>
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
        <template #footer><el-button @click="showEvaluateDetailDialog = false">关闭</el-button></template>
      </el-dialog>

      <!-- 查看作业内容对话框 -->
      <el-dialog v-model="showContentDialog" :title="contentTitle" width="800px" top="5vh">
        <div v-if="currentResult.fileType && ['jpg','jpeg','png','gif','bmp','webp'].includes(currentResult.fileType.toLowerCase())" style="text-align: center">
          <img :src="'/' + currentResult.filePath" style="max-width: 100%; max-height: 70vh" alt="作业图片" />
        </div>
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
              <template #default="{ row }"><el-tag size="small">{{ row.defaultWeight }}</el-tag></template>
            </el-table-column>
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">{{ getIndicatorPercent(row.defaultWeight) }}%</template>
            </el-table-column>
            <el-table-column prop="isSystem" label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isSystem === 1 ? 'danger' : 'success'" size="small">{{ row.isSystem === 1 ? '系统' : '自定义' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <template #footer><el-button @click="showIndicatorChart = false">关闭</el-button></template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, CircleCheck, Clock, TrendCharts, DataAnalysis } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const userRole = ref('')

// ============ 管理员统一仪表盘相关 ============
const evalStats = reactive({ totalCount: 0, scoredCount: 0, partialCount: 0, pendingCount: 0, avgAiScore: 0 })
const reportStats = reactive({ totalReports: 0, avgFinalScore: 0, teacherScoredRate: 0 })
const combinedTaskStats = ref([])
const evaluationByTeacher = ref([])
const chartLoading = ref(false)
const taskCompareChartRef = ref(null)
const teacherCompareChartRef = ref(null)
let taskCompareChart = null, teacherCompareChart = null

// 统计卡片配置（4个核心指标）
const adminStatCards = computed(() => [
  { label: '成果总数', value: evalStats.totalCount || 0, gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', icon: 'Document' },
  { label: '已AI评分', value: evalStats.scoredCount || 0, gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', icon: 'CircleCheck' },
  { label: '报告总数', value: reportStats.totalReports || 0, gradient: 'linear-gradient(135deg, #ff6b6b 0%, #feca57 100%)', icon: 'DataAnalysis' },
  { label: '教师评分率', value: reportStats.teacherScoredRate || 0, suffix: '%', gradient: 'linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%)', icon: 'TrendCharts' }
])

// 进度计算
const aiCompletionRate = computed(() => {
  const total = evalStats.totalCount || 0
  return total > 0 ? Math.round((evalStats.scoredCount || 0) / total * 100) : 0
})
const reportRate = computed(() => {
  const total = evalStats.totalCount || 0
  return total > 0 ? Math.round((reportStats.totalReports || 0) / total * 100) : 0
})

// 表格筛选变量
const taskTableSearch = ref('')
const taskTableTeacher = ref('')
const taskTableScoreRange = ref('')
const taskTableStatus = ref('')
const filteredTaskTable = ref([])

// 教师列表（从任务数据中提取去重）
const uniqueTeachersInTasks = computed(() => {
  const set = new Set(combinedTaskStats.value.map(item => item.teacherName).filter(Boolean))
  return [...set]
})

const filterTaskTable = () => {
  let data = [...combinedTaskStats.value]
  if (taskTableSearch.value) {
    data = data.filter(d => d.taskName && d.taskName.includes(taskTableSearch.value))
  }
  if (taskTableTeacher.value) {
    data = data.filter(d => d.teacherName === taskTableTeacher.value)
  }
  if (taskTableScoreRange.value) {
    if (taskTableScoreRange.value === 'high') data = data.filter(d => d.avgAiScore >= 80)
    else if (taskTableScoreRange.value === 'mid') data = data.filter(d => d.avgAiScore >= 60 && d.avgAiScore < 80)
    else if (taskTableScoreRange.value === 'low') data = data.filter(d => d.avgAiScore < 60 && d.avgAiScore > 0)
  }
  if (taskTableStatus.value) {
    if (taskTableStatus.value === 'done') data = data.filter(d => d.aiScoredCount >= d.studentCount && d.studentCount > 0)
    else if (taskTableStatus.value === 'partial') data = data.filter(d => d.aiScoredCount > 0 && d.aiScoredCount < d.studentCount)
    else if (taskTableStatus.value === 'none') data = data.filter(d => d.aiScoredCount === 0)
  }
  return data
}

const resetTaskTableFilter = () => {
  taskTableSearch.value = ''
  taskTableTeacher.value = ''
  taskTableScoreRange.value = ''
  taskTableStatus.value = ''
  filteredTaskTable.value = filterTaskTable()
}

// 任务详情对话框
const taskDetailVisible = ref(false)
const taskDetailLoading = ref(false)
const currentTaskDetail = ref({})
const taskDetailStudents = ref([])

const openTaskDetail = async (row) => {
  currentTaskDetail.value = row
  taskDetailVisible.value = true
  taskDetailLoading.value = true
  try {
    const res = await request.get('/result/page', {
      params: { taskId: row.taskId, pageNum: 1, pageSize: 100 }
    })
    const list = res.data?.list || res.data?.records || res.data || []
    // 为每个学生查询AI平均分和报告分
    const enriched = await Promise.all(list.map(async (item) => {
      let aiAvgScore = null
      let reportScore = null
      try {
        const recRes = await request.get(`/evaluate/records/${item.id}`)
        const records = recRes.data || []
        if (records.length > 0) {
          const sum = records.reduce((s, r) => s + (r.aiScore || 0), 0)
          aiAvgScore = Math.round(sum / records.length * 10) / 10
        }
      } catch (e) { /* ignore */ }
      try {
        const rptRes = await request.get('/report/by-result', { params: { resultId: item.id } })
        const rpt = rptRes.data
        if (rpt && rpt.finalScore) reportScore = Number(rpt.finalScore)
      } catch (e) { /* ignore */ }
      return { ...item, aiAvgScore, reportScore }
    }))
    taskDetailStudents.value = enriched
  } catch (e) {
    console.error('加载任务详情失败:', e)
  } finally {
    taskDetailLoading.value = false
  }
}

const getProgressColor = (pct) => {
  if (pct >= 80) return '#67c23a'
  if (pct >= 50) return '#e6a23c'
  return '#f56c6c'
}

const loadAdminAllStats = async () => {
  chartLoading.value = true
  try {
    const [evalRes, rptRes, combinedRes, teacherRes] = await Promise.all([
      request.get('/dashboard/admin/evaluation/stats'),
      request.get('/dashboard/admin/report/stats'),
      request.get('/dashboard/admin/combined/task-stats'),
      request.get('/dashboard/admin/evaluation/by-teacher')
    ])
    Object.assign(evalStats, evalRes.data || {})
    Object.assign(reportStats, rptRes.data || {})
    combinedTaskStats.value = combinedRes.data || []
    filteredTaskTable.value = filterTaskTable()
    evaluationByTeacher.value = teacherRes.data || []
  } catch (e) { console.error('加载统计数据失败:', e) }
  finally { chartLoading.value = false }
}

const initAdminCharts = async () => {
  await nextTick()
  try {
    // 过滤掉0提交的任务，按AI分降序，取前10
    const tasks = combinedTaskStats.value
      .filter(t => t.studentCount > 0)
      .sort((a, b) => (Number(b.avgAiScore) || 0) - (Number(a.avgAiScore) || 0))
      .slice(0, 10)
    const teachers = evaluationByTeacher.value

    // 1. 各任务AI评分对比（垂直柱状图，过滤0值，限10个）
    if (taskCompareChartRef.value) {
      taskCompareChart?.dispose()
      taskCompareChart = echarts.init(taskCompareChartRef.value)
      taskCompareChart.setOption({
        tooltip: { trigger: 'axis', formatter: (params) => {
          const p = params[0]
          const task = tasks[p.dataIndex]
          return `${p.name}<br/>AI平均分: ${p.value}<br/>报告平均分: ${task ? (task.avgReportScore || '-') : '-'}`
        }},
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: {
          type: 'category',
          data: tasks.map(t => t.taskName),
          axisLabel: { rotate: tasks.length > 6 ? 25 : 0, interval: 0, width: 80, overflow: 'truncate' }
        },
        yAxis: { type: 'value', name: '分数', max: 100 },
        series: [{
          name: 'AI平均分', type: 'bar',
          data: tasks.map(t => Number(t.avgAiScore) || 0),
          itemStyle: {
            color: (params) => {
              const val = params.value
              if (val >= 80) return new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#67c23a' }, { offset: 1, color: '#95d475' }])
              if (val >= 60) return new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#e6a23c' }, { offset: 1, color: '#f0c78a' }])
              return new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f56c6c' }, { offset: 1, color: '#fab6b6' }])
            },
            borderRadius: [4, 4, 0, 0]
          },
          barWidth: '50%',
          label: { show: true, position: 'top', formatter: '{c}', fontSize: 11 }
        }]
      })
    }

    // 2. 各教师评分统计对比（AI分 + 完成率，去除报告分）
    if (teacherCompareChartRef.value) {
      teacherCompareChart?.dispose()
      teacherCompareChart = echarts.init(teacherCompareChartRef.value)
      teacherCompareChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['AI平均分', '评分完成率'], top: 0 },
        grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: teachers.map(t => t.teacherName) },
        yAxis: [
          { type: 'value', name: '平均分', max: 100 },
          { type: 'value', name: '完成率(%)', max: 100 }
        ],
        series: [
          {
            name: 'AI平均分', type: 'bar',
            data: teachers.map(t => Number(t.avgScore) || 0),
            itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#4facfe' }, { offset: 1, color: '#00f2fe' }]), borderRadius: [4, 4, 0, 0] },
            barWidth: '30%', label: { show: true, position: 'top', formatter: '{c}' }
          },
          {
            name: '评分完成率', type: 'line', yAxisIndex: 1,
            data: teachers.map(t => t.resultCount > 0 ? Math.round(t.scoredCount / t.resultCount * 100) : 0),
            lineStyle: { color: '#e6a23c', width: 2 },
            itemStyle: { color: '#e6a23c' },
            label: { show: true, position: 'top', formatter: '{c}%' }
          }
        ]
      })
    }
  } catch (e) {
    console.error('图表初始化失败:', e)
  }
}

// ============ 教师视图相关(保持原有逻辑) ============
const teacherList = ref([])
const filterTeacher = ref('')
const evaluateList = ref([])
const groupedEvaluateList = ref([])
const resultList = ref([])
const showEvaluateDetailDialog = ref(false)
const showContentDialog = ref(false)
const currentEvaluateDetail = ref({})
const currentResult = ref({})
const contentTitle = ref('')
const loading = ref(false)
const loadingResults = ref(false)
// 重新评分加载状态（记录正在重新评分的resultId）
const reEvaluatingIds = ref(new Set())
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 评价记录分页
const evaluatePageNum = ref(1)
const evaluatePageSize = ref(10)
// 筛选条件
const searchText = ref('')
const filterStudent = ref('')
const filterTask = ref('')
const filterStatus = ref(null)
const studentList = ref([])
const taskList = ref([])
// 评分标准弹框
const showIndicatorChart = ref(false)
const indicatorList = ref([])
const indicatorChartRef = ref(null)
let indicatorChartInstance = null

const indicatorTotalWeight = computed(() =>
  indicatorList.value.reduce((sum, item) => sum + (Number(item.defaultWeight) || 0), 0)
)
const getIndicatorPercent = (weight) => {
  const tw = indicatorTotalWeight.value
  return tw ? Math.round((Number(weight) / tw) * 100) : 0
}

const renderIndicatorChart = () => {
  if (!indicatorChartRef.value) return
  if (indicatorChartInstance) indicatorChartInstance.dispose()
  indicatorChartInstance = echarts.init(indicatorChartRef.value)
  const data = indicatorList.value.map(item => ({ name: item.name, value: Number(item.defaultWeight) || 0 }))
  indicatorChartInstance.setOption({
    tooltip: { trigger: 'item', formatter: (p) => `${p.name}<br/>权重: ${p.value} | 占比: ${((p.value / indicatorTotalWeight.value) * 100).toFixed(1)}%` },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemGap: 12 },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c', '#3498db'],
    series: [{ type: 'pie', radius: ['35%', '65%'], center: ['40%', '50%'], avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: (p) => `${p.name}\n${((p.value / indicatorTotalWeight.value) * 100).toFixed(1)}%`, fontSize: 13, lineHeight: 18 },
      emphasis: { label: { show: true, fontSize: 15, fontWeight: 'bold' }, itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } },
      data
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
  } catch (e) { ElMessage.error('加载评分标准失败') }
}

// 防抖搜索
let searchTimer = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { resultPageNum.value = 1; loadResults() }, 500)
}

const viewEvaluateDetail = (row) => { currentEvaluateDetail.value = row; showEvaluateDetailDialog.value = true }

const viewContent = async (row) => {
  try {
    const res = await request.get(`/result/${row.id}`)
    currentResult.value = res.data || row
    contentTitle.value = `查看作业 - ${currentResult.value.studentName || '未知'} - ${currentResult.value.fileName || ''}`
    showContentDialog.value = true
  } catch (e) { ElMessage.error('加载作业内容失败') }
}

const downloadFile = () => {
  if (!currentResult.value.filePath) return
  const link = document.createElement('a')
  link.href = '/' + currentResult.value.filePath
  link.download = currentResult.value.fileName || 'download'
  link.click()
}

const downloadSourceFile = (row) => {
  if (!row.filePath) { ElMessage.warning('文件路径不存在'); return }
  const link = document.createElement('a')
  link.href = '/' + row.filePath
  link.download = row.fileName || 'download'
  document.body.appendChild(link); link.click(); document.body.removeChild(link)
}

const loadResults = async () => {
  loadingResults.value = true
  try {
    const res = await request.get('/result/page', {
      params: { pageNum: resultPageNum.value, pageSize: resultPageSize.value,
        studentId: filterStudent.value || undefined, taskId: filterTask.value || undefined,
        status: filterStatus.value !== null ? filterStatus.value : undefined,
        fileName: searchText.value || undefined }
    })
    console.log('loadResults API返回:', res)
    const list = res.data?.list || []
    console.log('成果列表数据:', list.map(item => ({ id: item.id, fileName: item.fileName, status: item.status })))
    resultList.value = list.map(item => ({ ...item, evaluating: false }))
      .sort((a, b) => (a.status ?? 99) - (b.status ?? 99))
    resultTotal.value = res.data?.total || 0
  } catch (e) { console.error('加载成果列表失败:', e) }
  finally { loadingResults.value = false }
}

const loadStudents = async () => {
  try { const res = await request.get('/user/student/list'); studentList.value = res.data || [] } catch (e) {}
}
const loadTasks = async () => {
  try {
    const res = await request.get('/task/page', { params: { pageNum: 1, pageSize: 100 } })
    taskList.value = res.data.list || []
  } catch (e) {}
}

const resetFilter = () => {
  searchText.value = ''; filterStudent.value = ''; filterTask.value = ''
  filterStatus.value = null; resultPageNum.value = 1; loadResults()
}

// 排序
const sortField = ref('')
const sortOrder = ref('')
const handleSortChange = ({ prop, order }) => { sortField.value = prop || ''; sortOrder.value = order || '' }

const sortedEvaluateList = computed(() => {
  const list = [...groupedEvaluateList.value]
  list.sort((a, b) => {
    const aUnscored = !a.avgTeacherScore, bUnscored = !b.avgTeacherScore
    if (aUnscored && !bUnscored) return -1
    if (!aUnscored && bUnscored) return 1
    if (sortField.value && sortOrder.value) {
      const asc = sortOrder.value === 'ascending'; let cmp = 0
      if (sortField.value === 'studentName') cmp = (a.studentName || '').localeCompare(b.studentName || '', 'zh-CN')
      else if (sortField.value === 'evalTime') {
        cmp = (a.evalTime ? new Date(a.evalTime).getTime() : 0) - (b.evalTime ? new Date(b.evalTime).getTime() : 0)
      }
      return asc ? cmp : -cmp
    }
    return (b.evalTime ? new Date(b.evalTime).getTime() : 0) - (a.evalTime ? new Date(a.evalTime).getTime() : 0)
  })
  return list
})

const paginatedEvaluateList = computed(() => {
  const start = (evaluatePageNum.value - 1) * evaluatePageSize.value
  return sortedEvaluateList.value.slice(start, start + evaluatePageSize.value)
})

const loadEvaluations = async () => {
  loading.value = true
  try {
    const results = await request.get('/result/list')
    const groupedEvaluations = []
    for (const result of (results.data || [])) {
      const res = await request.get(`/evaluate/records/${result.id}`)
      if (res.data && res.data.length > 0) {
        const records = res.data
        const aiScores = records.map(r => parseFloat(r.aiScore || 0)).filter(s => !isNaN(s))
        const teacherScores = records.map(r => parseFloat(r.teacherScore || 0)).filter(s => !isNaN(s) && s > 0)
        groupedEvaluations.push({
          resultId: result.id, fileName: result.fileName, studentName: result.studentName,
          taskTitle: result.taskTitle, evalTime: records[0].createTime, records,
          avgAiScore: aiScores.length > 0 ? aiScores.reduce((a, b) => a + b, 0) / aiScores.length : null,
          avgTeacherScore: teacherScores.length > 0 ? teacherScores.reduce((a, b) => a + b, 0) / teacherScores.length : null
        })
      }
    }
    groupedEvaluateList.value = groupedEvaluations
  } catch (e) { console.error('加载评价记录失败:', e) }
  finally { loading.value = false }
}

const runEvaluateDirect = async (row) => {
  if (!row.id) { ElMessage.warning('成果ID不存在'); return }
  if (row.status === 1) {
    try {
      await ElMessageBox.confirm('该文件已评价，确定要再次评分吗？这将覆盖之前的评价记录。', '确认操作',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    } catch { return }
  }
  row.evaluating = true
  try {
    ElMessage.info('正在进行AI自动评分，请稍候（约需10-30秒）...')
    const res = await request.post(`/evaluate/ai/${row.id}`)
    console.log('AI评分返回:', res)
    const data = res.data || {}
    const successCount = data.successCount || 0
    const failCount = data.failCount || 0
    if (successCount > 0) {
      ElMessage.success(`AI评分完成！成功 ${successCount} 个指标${failCount > 0 ? '，失败 ' + failCount + ' 个' : ''}`)
      // 刷新数据
      try { await loadEvaluations() } catch (e) { console.error('刷新评价记录失败:', e) }
      try { await loadResults() } catch (e) { console.error('刷新成果列表失败:', e) }
    } else {
      ElMessage.error(`AI评分失败：所有指标评分均未成功。${data.failMessages ? '\n' + data.failMessages.join('\n') : ''}`)
    }
  } catch (e) {
    console.error('AI评分请求失败:', e)
    ElMessage.error('评分失败：' + (e.response?.data?.message || e.message))
  } finally {
    row.evaluating = false
  }
}

// 重新评分（AI评分记录区域）
const reEvaluate = async (row) => {
  if (!row.resultId) { ElMessage.warning('成果ID不存在'); return }
  try {
    await ElMessageBox.confirm('确定要对该成果重新进行AI评分吗？新的评分将覆盖原有记录并同步到报表中心。', '确认重新评分',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
  } catch { return }
  reEvaluatingIds.value.add(row.resultId)
  try {
    ElMessage.info('正在重新进行AI自动评分，请稍候（约需10-30秒）...')
    const res = await request.post(`/evaluate/ai/${row.resultId}`)
    const data = res.data || {}
    const successCount = data.successCount || 0
    const failCount = data.failCount || 0
    if (successCount > 0) {
      ElMessage.success(`重新评分完成！成功 ${successCount} 个指标${failCount > 0 ? '，失败 ' + failCount + ' 个' : ''}`)
      try { await loadEvaluations() } catch (e) { console.error('刷新评价记录失败:', e) }
      try { await loadResults() } catch (e) { console.error('刷新成果列表失败:', e) }
    } else {
      ElMessage.error(`重新评分失败：所有指标评分均未成功。${data.failMessages ? '\n' + data.failMessages.join('\n') : ''}`)
    }
  } catch (e) {
    console.error('重新评分请求失败:', e)
    ElMessage.error('重新评分失败：' + (e.response?.data?.message || e.message))
  } finally {
    reEvaluatingIds.value.delete(row.resultId)
  }
}

// 管理员仪表盘自动刷新定时器（每30秒）
let adminRefreshTimer = null
const ADMIN_REFRESH_INTERVAL = 30000

const refreshAdminData = async () => {
  await loadAdminAllStats()
  // 重新初始化图表（含数据刷新）
  await initAdminCharts()
}

onMounted(async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') {
    await loadAdminAllStats()
    await initAdminCharts()
    // 启动定时刷新
    adminRefreshTimer = setInterval(refreshAdminData, ADMIN_REFRESH_INTERVAL)
  } else {
    loadResults(); loadEvaluations(); loadStudents(); loadTasks()
  }
})

onBeforeUnmount(() => {
  // 清除定时刷新
  if (adminRefreshTimer) clearInterval(adminRefreshTimer)
  taskCompareChart?.dispose(); teacherCompareChart?.dispose(); indicatorChartInstance?.dispose()
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
.stat-label { font-size: 14px; color: #909399; margin-top: 6px; }
.card-title { font-weight: bold; font-size: 15px; color: #303133; }
:deep(.el-card) { border-radius: 12px; }
</style>
