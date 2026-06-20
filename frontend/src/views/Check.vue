<!--
  智能核查页面（AI辅助核查、核查记录、大模型调用）
-->
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>智能核查</span>
          <el-alert title="点击操作栏的'核查'按钮可对该成果进行AI智能核查" type="info" :closable="false" style="width: auto; margin: 0" />
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div style="margin-bottom: 20px; display: flex; gap: 15px; flex-wrap: wrap">
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
          style="width: 120px"
          @change="loadResults"
        >
          <el-option label="待处理" :value="0" />
          <el-option label="已核查" :value="1" />
          <el-option label="已评价" :value="2" />
        </el-select>
        <el-button type="primary" @click="resetFilter">重置筛选</el-button>
      </div>
      
      <el-table :data="resultList" border stripe v-loading="loadingResults">
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
        <el-table-column prop="createTime" label="提交时间" width="160" sortable />
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="viewContent(row)">查看</el-button>
            <el-button 
              size="small" 
              type="success" 
              @click="runCheckDirect(row)" 
              :loading="row.checking"
              :disabled="row.checking"
            >
              核查
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

    <el-card style="margin-top: 20px">
      <template #header>核查记录</template>
      <el-table :data="paginatedCheckList" border stripe v-loading="loading">
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (checkPageNum - 1) * checkPageSize + i + 1" />
        
        <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="90" />
        <el-table-column label="核查类型" width="180" align="center">
          <template #default="{ row }">
            <div style="display: flex; gap: 5px; flex-wrap: wrap">
              <el-tag v-if="row.completeness" :type="row.completeness.checkResult === 1 ? 'success' : 'warning'" size="small">
                完整性
              </el-tag>
              <el-tag v-if="row.logic" :type="row.logic.checkResult === 1 ? 'success' : 'warning'" size="small">
                逻辑性
              </el-tag>
              <el-tag v-if="row.match" :type="row.match.checkResult === 1 ? 'success' : 'warning'" size="small">
                匹配度
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="核查结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.allPassed" type="success">全部通过</el-tag>
            <el-tag v-else-if="row.hasIssues" type="warning">需改进</el-tag>
            <el-tag v-else type="info">未核查</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkTime" label="核查时间" width="160" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewCheckDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="checkPageNum"
        v-model:page-size="checkPageSize"
        :page-sizes="[5, 10, 20]"
        :total="groupedCheckList.length"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadChecks"
        @current-change="loadChecks"
      />
      <el-empty v-if="groupedCheckList.length === 0" description="暂无核查记录" />
    </el-card>

    <!-- 核查详情对话框 -->
    <el-dialog v-model="showCheckDetailDialog" title="核查详情" width="900px" top="5vh">
      <el-alert :title="'成果文件：' + (currentCheckDetail.fileName || '')" type="info" :closable="false" style="margin-bottom: 20px" />
      
      <el-descriptions :column="1" border style="margin-bottom: 20px">
        <el-descriptions-item label="提交学生">{{ currentCheckDetail.studentName || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="实训任务">{{ currentCheckDetail.taskTitle || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="核查时间">{{ currentCheckDetail.checkTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-tabs type="border-card">
        <!-- 完整性核查 -->
        <el-tab-pane v-if="currentCheckDetail.completeness" label="完整性核查">
          <div style="padding: 10px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="核查结果">
                <el-tag :type="currentCheckDetail.completeness.checkResult === 1 ? 'success' : 'warning'">
                  {{ currentCheckDetail.completeness.checkResult === 1 ? '通过' : '待改进' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题描述">
                <div style="white-space: pre-wrap; line-height: 1.8">
                  {{ currentCheckDetail.completeness.issueDescription || '无' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="改进建议">
                <div style="white-space: pre-wrap; line-height: 1.8; color: #409eff">
                  {{ currentCheckDetail.completeness.suggestion || '无' }}
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <!-- 逻辑性核查 -->
        <el-tab-pane v-if="currentCheckDetail.logic" label="逻辑性核查">
          <div style="padding: 10px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="核查结果">
                <el-tag :type="currentCheckDetail.logic.checkResult === 1 ? 'success' : 'warning'">
                  {{ currentCheckDetail.logic.checkResult === 1 ? '通过' : '待改进' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题描述">
                <div style="white-space: pre-wrap; line-height: 1.8">
                  {{ currentCheckDetail.logic.issueDescription || '无' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="改进建议">
                <div style="white-space: pre-wrap; line-height: 1.8; color: #409eff">
                  {{ currentCheckDetail.logic.suggestion || '无' }}
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>

        <!-- 匹配度核查 -->
        <el-tab-pane v-if="currentCheckDetail.match" label="匹配度核查">
          <div style="padding: 10px">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="核查结果">
                <el-tag :type="currentCheckDetail.match.checkResult === 1 ? 'success' : 'warning'">
                  {{ currentCheckDetail.match.checkResult === 1 ? '通过' : '待改进' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题描述">
                <div style="white-space: pre-wrap; line-height: 1.8">
                  {{ currentCheckDetail.match.issueDescription || '无' }}
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="改进建议">
                <div style="white-space: pre-wrap; line-height: 1.8; color: #409eff">
                  {{ currentCheckDetail.match.suggestion || '无' }}
                </div>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="showCheckDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Loading, Search } from '@element-plus/icons-vue'

const checkList = ref([])
const groupedCheckList = ref([])
const resultList = ref([])
const selectedResultId = ref(null)
const showRunDialog = ref(false)
const showContentDialog = ref(false)
const showCheckDetailDialog = ref(false)
const currentResult = ref({})
const currentCheckDetail = ref({})
const contentTitle = ref('')
const loading = ref(false)
const loadingResults = ref(false)
const checking = ref(false)
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 核查记录分页
const checkPageNum = ref(1)
const checkPageSize = ref(10)
const checkTotal = ref(0)
// 筛选条件
const searchText = ref('')
const filterStudent = ref('')
const filterTask = ref('')
const filterStatus = ref(null)
const studentList = ref([])
const taskList = ref([])

// 防抖搜索(输入时自动触发,延迟500ms)
let searchTimer = null
const debounceSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    resultPageNum.value = 1  // 搜索时重置到第1页
    loadResults()
  }, 500)
}

// 查看核查详情
const viewCheckDetail = (row) => {
  currentCheckDetail.value = row
  showCheckDetailDialog.value = true
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

// 下载文件
const downloadFile = () => {
  if (!currentResult.value.filePath) return
  const link = document.createElement('a')
  link.href = '/' + currentResult.value.filePath
  link.download = currentResult.value.fileName || 'download'
  link.click()
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
        fileName: searchText.value || undefined
      }
    })
    resultList.value = (res.data.list || []).map(item => ({
      ...item,
      checking: false // 为每行添加核查状态
    }))
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
  resultPageNum.value = 1
  loadResults()
}

// 分页后的核查记录列表(计算属性)
const paginatedCheckList = computed(() => {
  const start = (checkPageNum.value - 1) * checkPageSize.value
  const end = start + checkPageSize.value
  return groupedCheckList.value.slice(start, end)
})

// 加载核查记录（按成果分组）
const loadChecks = async () => {
  loading.value = true
  try {
    const results = await request.get('/result/list')
    const groupedChecks = []
    
    for (const result of (results.data || [])) {
      const res = await request.get(`/check/records/${result.id}`)
      if (res.data && res.data.length > 0) {
        // 将同一成果的三条核查记录合并为一行
        const checkGroup = {
          resultId: result.id,
          fileName: result.fileName,
          studentName: result.studentName,
          taskTitle: result.taskTitle,
          checkTime: res.data[0].createTime,
          completeness: null,
          logic: null,
          match: null
        }
        
        // 按核查类型分类
        for (const check of res.data) {
          if (check.checkType === 'completeness') {
            checkGroup.completeness = check
          } else if (check.checkType === 'logic') {
            checkGroup.logic = check
          } else if (check.checkType === 'match') {
            checkGroup.match = check
          }
        }
        
        // 计算综合结果
        const results_arr = [
          checkGroup.completeness?.checkResult,
          checkGroup.logic?.checkResult,
          checkGroup.match?.checkResult
        ].filter(r => r !== null && r !== undefined)
        
        checkGroup.allPassed = results_arr.length > 0 && results_arr.every(r => r === 1)
        checkGroup.hasIssues = results_arr.some(r => r === 2)
        
        groupedChecks.push(checkGroup)
      }
    }
    
    groupedCheckList.value = groupedChecks.reverse()
  } catch (e) {
    console.error('加载核查记录失败:', e)
  } finally {
    loading.value = false
  }
}

// 执行智能核查 (旧方法,保留兼容)
const runCheck = async () => {
  if (!selectedResultId.value) {
    ElMessage.warning('请选择要核查的成果')
    return
  }
  
  checking.value = true
  try {
    const res = await request.post(`/check/run/${selectedResultId.value}`)
    ElMessage.success('AI核查完成！')
    showRunDialog.value = false
    loadChecks()
    loadResults()
  } catch (e) {
    console.error('核查失败:', e)
    ElMessage.error('核查失败：' + (e.response?.data?.msg || e.message))
  } finally {
    checking.value = false
  }
}

// 直接核查当前行 (新方法)
const runCheckDirect = async (row) => {
  if (!row.id) {
    ElMessage.warning('成果ID不存在')
    return
  }
  
  // 设置当前行的核查状态为进行中
  row.checking = true
  
  try {
    ElMessage.info('正在进行AI智能核查，请稍候（约需10-30秒）...')
    const res = await request.post(`/check/run/${row.id}`)
    ElMessage.success('AI核查完成！')
    
    // 刷新列表和核查记录
    await loadChecks()
    await loadResults()
  } catch (e) {
    console.error('核查失败:', e)
    ElMessage.error('核查失败：' + (e.response?.data?.msg || e.message))
  } finally {
    row.checking = false
  }
}

onMounted(() => {
  loadResults()
  loadChecks()
  loadStudents()
  loadTasks()
})
</script>
