<template>
  <div>
    <!-- 评价操作区 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>AI自动评分</span>
          <el-button type="primary" @click="showEvaluateDialog = true" :disabled="evaluating">开始评分</el-button>
        </div>
      </template>
      
      <el-alert title="选择实训成果进行AI自动评分，评分后教师可修改分数" type="info" :closable="false" style="margin-bottom: 15px" />
      
      <!-- 成果列表，显示提交人信息 -->
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
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewContent(row)">查看</el-button>
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

    <!-- 评价记录区 -->
    <el-card style="margin-top: 20px">
      <template #header>评价记录</template>
      <el-table :data="groupedEvaluateList" border stripe v-loading="loading">
        <el-table-column prop="fileName" label="成果文件" min-width="150" show-overflow-tooltip />
        <el-table-column prop="studentName" label="提交人" width="90" />
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
        <el-table-column label="教师评分" width="90" align="center">
          <template #default="{ row }">
            {{ row.avgTeacherScore ? row.avgTeacherScore.toFixed(1) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="最终得分" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.avgFinalScore >= 80 ? 'success' : row.avgFinalScore >= 60 ? 'warning' : 'danger'">
              {{ row.avgFinalScore ? row.avgFinalScore.toFixed(1) : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="evalTime" label="评价时间" width="160" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="viewEvaluateDetail(row)">查看详情</el-button>
            <el-button size="small" @click="openTeacherScoreForGroup(row)">评分</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="evaluatePageNum"
        v-model:page-size="evaluatePageSize"
        :page-sizes="[5, 10, 20]"
        :total="evaluateTotal"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadEvaluations"
        @current-change="loadEvaluations"
      />
      <el-empty v-if="groupedEvaluateList.length === 0" description="暂无评价记录" />
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
        <el-table-column prop="teacherScore" label="教师评分" width="100" />
        <el-table-column prop="finalScore" label="最终得分" width="100" />
        <el-table-column prop="aiComment" label="AI评语" show-overflow-tooltip />
        <el-table-column prop="teacherComment" label="教师评语" show-overflow-tooltip />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openTeacherScore(row)">教师评分</el-button>
          </template>
        </el-table-column>
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

    <!-- AI评分对话框 -->
    <el-dialog v-model="showEvaluateDialog" title="AI自动评分" width="500px">
      <el-alert title="选择成果进行AI自动评分，将对该成果的所有指标进行评分" type="info" :closable="false" style="margin-bottom: 15px" />
      <el-form>
        <el-form-item label="选择成果">
          <el-select v-model="selectedResultId" placeholder="请选择要评价的成果" style="width: 100%">
            <el-option 
              v-for="item in resultList" 
              :key="item.id" 
              :label="`${item.fileName} - ${item.studentName || '未知'} - ${item.taskTitle || '未知'}`" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEvaluateDialog = false" :disabled="evaluating">取消</el-button>
        <el-button type="primary" @click="runAiEvaluate" :loading="evaluating" :disabled="evaluating">开始评分</el-button>
        <div v-if="evaluating" style="text-align: center; margin-top: 10px; color: #409eff">
          <el-icon class="is-loading" style="margin-right: 5px"><Loading /></el-icon>AI正在评分中，请稍候（逐指标评分，约需30-60秒）...
        </div>
      </template>
    </el-dialog>

    <!-- 教师评分对话框 -->
    <el-dialog v-model="showTeacherScoreDialog" title="教师评分" width="600px">
      <el-form :model="teacherScoreForm" label-width="80px">
        <el-form-item label="评价指标">
          <el-input :value="teacherScoreForm.indicatorName" disabled />
        </el-form-item>
        <el-form-item label="AI评分">
          <el-input :value="teacherScoreForm.aiScore" disabled />
        </el-form-item>
        <el-form-item label="AI评语">
          <el-input :value="teacherScoreForm.aiComment" type="textarea" :rows="2" disabled />
        </el-form-item>
        <el-form-item label="教师评分" required>
          <el-input-number v-model="teacherScoreForm.teacherScore" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="教师评语">
          <el-input v-model="teacherScoreForm.teacherComment" type="textarea" :rows="3" placeholder="请输入评语" />
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
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const evaluateList = ref([])
const groupedEvaluateList = ref([])
const resultList = ref([])
const selectedResultId = ref(null)
const showEvaluateDialog = ref(false)
const showTeacherScoreDialog = ref(false)
const showContentDialog = ref(false)
const showEvaluateDetailDialog = ref(false)
const currentResult = ref({})
const currentEvaluateDetail = ref({})
const contentTitle = ref('')
const loading = ref(false)
const loadingResults = ref(false)
const evaluating = ref(false)
const submitting = ref(false)
// 成果列表分页
const resultPageNum = ref(1)
const resultPageSize = ref(10)
const resultTotal = ref(0)
// 评价记录分页
const evaluatePageNum = ref(1)
const evaluatePageSize = ref(10)
const evaluateTotal = ref(0)

// 查看评价详情
const viewEvaluateDetail = (row) => {
  currentEvaluateDetail.value = row
  showEvaluateDetailDialog.value = true
}

// 为分组打开教师评分（选择指标）
const openTeacherScoreForGroup = (row) => {
  if (!row.records || row.records.length === 0) {
    ElMessage.warning('该成果暂无评价记录')
    return
  }
  // 默认打开第一个未评分的指标
  const unscored = row.records.find(r => !r.teacherScore)
  if (unscored) {
    openTeacherScore(unscored)
  } else {
    openTeacherScore(row.records[0])
  }
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

const teacherScoreForm = reactive({
  id: null,
  indicatorName: '',
  aiScore: 0,
  aiComment: '',
  teacherScore: 0,
  teacherComment: ''
})

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
    console.error('加载成果列表失败:', e)
  } finally {
    loadingResults.value = false
  }
}

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
          avgAiScore: aiScores.length > 0 ? aiScores.reduce((a, b) => a + b, 0) / aiScores.length : null,
          avgTeacherScore: teacherScores.length > 0 ? teacherScores.reduce((a, b) => a + b, 0) / teacherScores.length : null,
          avgFinalScore: finalScores.length > 0 ? finalScores.reduce((a, b) => a + b, 0) / finalScores.length : null
        }
        
        groupedEvaluations.push(evalGroup)
      }
    }
    
    groupedEvaluateList.value = groupedEvaluations.reverse()
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

// 打开教师评分对话框
const openTeacherScore = (row) => {
  Object.assign(teacherScoreForm, {
    id: row.id,
    indicatorName: row.indicatorName,
    aiScore: row.aiScore,
    aiComment: row.aiComment,
    teacherScore: row.teacherScore || 0,
    teacherComment: row.teacherComment || ''
  })
  showTeacherScoreDialog.value = true
}

// 提交教师评分
const submitTeacherScore = async () => {
  if (!teacherScoreForm.teacherScore) {
    ElMessage.warning('请输入教师评分')
    return
  }
  
  submitting.value = true
  try {
    await request.put('/evaluate/teacher-score', {
      id: teacherScoreForm.id,
      teacherScore: teacherScoreForm.teacherScore,
      teacherComment: teacherScoreForm.teacherComment
    })
    ElMessage.success('评分成功')
    showTeacherScoreDialog.value = false
    loadEvaluations()
  } catch (e) {
    console.error('评分失败:', e)
    ElMessage.error('评分失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadResults()
  loadEvaluations()
})
</script>
