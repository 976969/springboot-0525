
<!--
  AI练习题页面 - 学生根据已选课程生成AI练习题并自测
-->
<template>
  <div class="ai-practice-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><el-icon><EditPen /></el-icon> AI智能练习</h2>
      <p class="subtitle">选择已选课程，AI自动生成练习题，巩固所学知识</p>
    </div>

    <!-- 步骤1: 选择课程 -->
    <div v-if="currentStep === 'select'" class="step-section">
      <el-row :gutter="20">
        <el-col :span="24">
          <h3 class="section-title">我的课程</h3>
        </el-col>
      </el-row>

      <el-row v-if="myCourses.length > 0" :gutter="20" class="course-cards">
        <el-col v-for="course in myCourses" :key="course.id" :xs="24" :sm="12" :md="8" :lg="6">
          <el-card class="course-card" shadow="hover" @click="selectCourse(course)">
            <div class="course-icon">
              <el-icon :size="32"><Reading /></el-icon>
            </div>
            <div class="course-info">
              <el-tooltip :content="course.name" placement="top" :show-after="300">
                <h4>{{ course.name }}</h4>
              </el-tooltip>
              <p class="teacher">{{ course.teacherName }}</p>
              <div class="course-stats">
                <el-tag size="small" type="info">练习 {{ course.practiceCount }} 次</el-tag>
                <el-tag v-if="course.bestScore > 0" size="small" type="success">最高 {{ course.bestScore }} 分</el-tag>
              </div>
            </div>
            <div class="course-action">
              <el-button type="primary" size="small" @click.stop="startPractice(course)">
                <el-icon><MagicStick /></el-icon> 生成练习
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-else description="您还没有选课，请先去选课中心选课" :image-size="120" />

      <!-- 历史记录 -->
      <div v-if="history.length > 0" class="history-section">
        <h3 class="section-title">
          <el-icon><Clock /></el-icon> 练习历史
          <el-button size="small" text @click="showHistory = !showHistory" style="float:right">
            {{ showHistory ? '收起' : '展开' }}
          </el-button>
        </h3>
        <el-table v-if="showHistory" :data="history" stripe style="width: 100%">
          <el-table-column prop="courseName" label="课程" width="160" />
          <el-table-column label="得分" width="120">
            <template #default="{ row }">
              <el-tag :type="getScoreType(row.score)" effect="plain">
                {{ row.score !== null ? row.score + ' / ' + row.totalScore : '未评分' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="练习时间" width="180" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="viewDetail(row.id)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 步骤2: 答题中 -->
    <div v-if="currentStep === 'answering'" class="step-section">
      <div class="practice-header">
        <el-button @click="backToSelect" :disabled="submitting">
          <el-icon><ArrowLeft /></el-icon> 返回选课
        </el-button>
        <h3>{{ currentCourseName }} - AI练习题</h3>
        <el-tag type="warning">共 {{ questions.length }} 题，每题 {{ (100 / questions.length).toFixed(0) }} 分</el-tag>
      </div>

      <div class="questions-list">
        <el-card v-for="(q, index) in questions" :key="q.id" class="question-card" shadow="hover">
          <div class="question-number">
            <el-tag type="primary" effect="dark" round>{{ index + 1 }}</el-tag>
          </div>
          <div class="question-content">
            <p class="question-text">{{ q.question }}</p>
            <div class="options-list">
              <div
                v-for="(optionText, optionKey) in q.options"
                :key="optionKey"
                class="option-item"
                :class="{
                  'selected': userAnswers[String(q.id)] === optionKey,
                  'disabled': submitted
                }"
                @click="!submitted && selectAnswer(q.id, optionKey)"
              >
                <span class="option-label">{{ optionKey }}.</span>
                <span class="option-text">{{ optionText }}</span>
                <el-icon v-if="submitted && q.answer === optionKey" class="correct-icon" color="#67c23a"><CircleCheckFilled /></el-icon>
                <el-icon v-if="submitted && userAnswers[String(q.id)] === optionKey && q.answer !== optionKey" class="wrong-icon" color="#f56c6c"><CircleCloseFilled /></el-icon>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="submit-area" v-if="!submitted">
        <el-button type="primary" size="large" @click="submitAnswers" :loading="submitting" :disabled="Object.keys(userAnswers).length === 0">
          提交答案（已答 {{ Object.keys(userAnswers).length }}/{{ questions.length }} 题）
        </el-button>
      </div>

      <!-- 评分结果 -->
      <div v-if="submitted && scoreResult" class="score-result">
        <el-card shadow="never">
          <div class="score-display">
            <div class="score-circle" :class="getScoreClass(scoreResult.score)">
              <span class="score-number">{{ scoreResult.score }}</span>
              <span class="score-total">/ {{ scoreResult.totalScore }}</span>
            </div>
            <div class="score-info">
              <h3>练习完成！</h3>
              <p>答对 {{ scoreResult.correctCount }} / {{ scoreResult.questionCount }} 题</p>
            </div>
          </div>
          <el-divider />
          <h4>答题详情</h4>
          <div v-for="(detail, idx) in scoreResult.details" :key="idx" class="detail-item">
            <el-tag :type="detail.isCorrect ? 'success' : 'danger'" size="small" effect="dark" round style="margin-right:8px">
              {{ detail.isCorrect ? '正确' : '错误' }}
            </el-tag>
            <span class="detail-question">第{{ idx + 1 }}题：{{ detail.question }}</span>
            <div class="detail-answer">
              <span>你的答案：<strong :class="detail.isCorrect ? 'text-success' : 'text-danger'">{{ detail.studentAnswer }}</strong></span>
              <span v-if="!detail.isCorrect">正确答案：<strong class="text-success">{{ detail.correctAnswer }}</strong></span>
            </div>
            <div v-if="detail.explanation" class="detail-explanation">
              <el-icon><InfoFilled /></el-icon> {{ detail.explanation }}
            </div>
          </div>
          <div class="result-actions">
            <el-button type="primary" @click="backToSelect">返回选课</el-button>
            <el-button @click="retryPractice">再练一次</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 步骤3: 查看详情 -->
    <div v-if="currentStep === 'detail'" class="step-section">
      <div class="practice-header">
        <el-button @click="backToSelect"><el-icon><ArrowLeft /></el-icon> 返回</el-button>
        <h3>练习详情 - {{ detailData.courseName }}</h3>
        <el-tag type="success" size="large">得分：{{ detailData.score }} / {{ detailData.totalScore }}</el-tag>
      </div>
      <div v-if="detailData.questions" class="questions-list">
        <el-card v-for="(q, index) in detailData.questions" :key="q.id" class="question-card" shadow="hover">
          <div class="question-number">
            <el-tag type="primary" effect="dark" round>{{ index + 1 }}</el-tag>
          </div>
          <div class="question-content">
            <p class="question-text">{{ q.question }}</p>
            <div class="options-list">
              <div
                v-for="(optionText, optionKey) in q.options"
                :key="optionKey"
                class="option-item disabled"
                :class="{ 'is-correct': q.answer === optionKey }"
              >
                <span class="option-label">{{ optionKey }}.</span>
                <span class="option-text">{{ optionText }}</span>
                <el-icon v-if="q.answer === optionKey" class="correct-icon" color="#67c23a"><CircleCheckFilled /></el-icon>
              </div>
            </div>
            <div v-if="q.explanation" class="explanation-box">
              <el-icon><InfoFilled /></el-icon> 解析：{{ q.explanation }}
            </div>
          </div>
        </el-card>
      </div>
      <div class="result-actions" style="margin-top:20px">
        <el-button type="primary" @click="backToSelect">返回选课</el-button>
      </div>
    </div>

    <!-- 生成题目loading -->
    <el-dialog v-model="generating" title="AI正在生成题目..." width="400" :close-on-click-modal="false" :show-close="false">
      <div class="generating-content">
        <el-icon class="is-loading" :size="40" color="#409EFF"><Loading /></el-icon>
        <p>AI正在根据课程内容生成练习题，请稍候...</p>
        <p class="tip">通常需要 10-30 秒</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const currentStep = ref('select') // select | answering | detail
const myCourses = ref([])
const history = ref([])
const showHistory = ref(true)
const questions = ref([])
const userAnswers = ref({})
const submitting = ref(false)
const submitted = ref(false)
const generating = ref(false)
const currentCourseName = ref('')
const currentCourseId = ref(null)
const currentPracticeId = ref(null)
const scoreResult = ref(null)
const detailData = ref({})

// 加载数据
onMounted(() => {
  loadMyCourses()
  loadHistory()
})

async function loadMyCourses() {
  try {
    const res = await request.get('/ai-practice/my-courses')
    myCourses.value = res.data || []
  } catch (e) {
    ElMessage.error('加载课程失败')
  }
}

async function loadHistory() {
  try {
    const res = await request.get('/ai-practice/history')
    history.value = res.data || []
  } catch (e) {
    // ignore
  }
}

function selectCourse(course) {
  // 点击卡片只是选中，不触发练习
}

async function startPractice(course) {
  generating.value = true
  currentCourseName.value = course.name
  currentCourseId.value = course.id
  try {
    const res = await request.post('/ai-practice/generate', { courseId: course.id })
    questions.value = res.data.questions || []
    currentPracticeId.value = res.data.practiceId
    userAnswers.value = {}
    submitted.value = false
    scoreResult.value = null
    currentStep.value = 'answering'
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '生成题目失败，请重试')
  } finally {
    generating.value = false
  }
}

function selectAnswer(questionId, optionKey) {
  userAnswers.value[String(questionId)] = optionKey
}

async function submitAnswers() {
  if (Object.keys(userAnswers.value).length === 0) {
    ElMessage.warning('请至少回答一道题')
    return
  }
  submitting.value = true
  try {
    const res = await request.post('/ai-practice/submit', {
      practiceId: currentPracticeId.value,
      answers: userAnswers.value
    })
    scoreResult.value = res.data
    submitted.value = true
    // 刷新历史
    await loadHistory()
    await loadMyCourses()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '提交失败')
  } finally {
    submitting.value = false
  }
}

async function viewDetail(id) {
  try {
    const res = await request.get(`/ai-practice/detail/${id}`)
    detailData.value = res.data
    currentStep.value = 'detail'
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

function backToSelect() {
  currentStep.value = 'select'
  questions.value = []
  userAnswers.value = {}
  submitted.value = false
  scoreResult.value = null
  detailData.value = {}
}

function retryPractice() {
  const course = myCourses.value.find(c => c.id === currentCourseId.value)
  if (course) {
    startPractice(course)
  }
}

function getScoreType(score) {
  if (score === null || score === undefined) return 'info'
  if (score >= 90) return 'success'
  if (score >= 70) return 'warning'
  return 'danger'
}

function getScoreClass(score) {
  if (score >= 90) return 'score-excellent'
  if (score >= 70) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}
</script>

<style scoped>
.ai-practice-page { max-width: 1200px; margin: 0 auto; }

.page-header { margin-bottom: 30px; }
.page-header h2 { font-size: 24px; color: #303133; display: flex; align-items: center; gap: 10px; margin: 0 0 8px 0; }
.page-header .subtitle { color: #909399; margin: 0; font-size: 14px; }

.section-title { font-size: 18px; color: #303133; margin: 0 0 20px 0; display: flex; align-items: center; gap: 8px; }

.course-cards { margin-bottom: 30px; }
.course-card { cursor: pointer; transition: all 0.3s; border-radius: 12px; }
.course-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.1); }
.course-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; padding: 20px; }
.course-icon { width: 60px; height: 60px; border-radius: 12px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; color: white; flex-shrink: 0; }
.course-info { flex: 1; min-width: 0; }
.course-info h4 { margin: 0 0 4px 0; font-size: 16px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.course-info .teacher { margin: 0 0 8px 0; font-size: 14px; color: #909399; }
.course-stats { display: flex; gap: 8px; flex-wrap: wrap; }
.course-action { flex-shrink: 0; }

.history-section { margin-top: 10px; }

.practice-header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; flex-wrap: wrap; }
.practice-header h3 { margin: 0; flex: 1; font-size: 20px; color: #303133; }

.questions-list { display: flex; flex-direction: column; gap: 16px; }
.question-card { border-radius: 12px; }
.question-card :deep(.el-card__body) { display: flex; gap: 16px; padding: 24px; }
.question-number { flex-shrink: 0; padding-top: 4px; }
.question-content { flex: 1; }
.question-text { font-size: 16px; font-weight: 500; color: #303133; margin: 0 0 16px 0; line-height: 1.6; }

.options-list { display: flex; flex-direction: column; gap: 10px; }
.option-item { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: 2px solid #e4e7ed; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.option-item:hover:not(.disabled) { border-color: #409eff; background-color: #ecf5ff; }
.option-item.selected { border-color: #409eff; background-color: #ecf5ff; }
.option-item.disabled { cursor: default; }
.option-item.is-correct { border-color: #67c23a; background-color: #f0f9eb; }
.option-label { font-weight: 600; color: #606266; min-width: 20px; }
.option-text { flex: 1; color: #303133; }

.submit-area { text-align: center; margin: 30px 0; }

.score-result { margin-top: 24px; }
.score-display { display: flex; align-items: center; gap: 24px; padding: 10px 0; }
.score-circle { width: 120px; height: 120px; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; border: 4px solid; }
.score-circle.score-excellent { border-color: #67c23a; color: #67c23a; }
.score-circle.score-good { border-color: #409eff; color: #409eff; }
.score-circle.score-pass { border-color: #e6a23c; color: #e6a23c; }
.score-circle.score-fail { border-color: #f56c6c; color: #f56c6c; }
.score-number { font-size: 36px; font-weight: bold; line-height: 1; }
.score-total { font-size: 14px; opacity: 0.7; }
.score-info h3 { margin: 0 0 8px 0; font-size: 20px; color: #303133; }
.score-info p { margin: 0; color: #909399; }

.detail-item { padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.detail-item:last-child { border-bottom: none; }
.detail-question { font-size: 14px; color: #303133; }
.detail-answer { margin-top: 6px; font-size: 14px; color: #606266; display: flex; gap: 20px; }
.detail-explanation { margin-top: 6px; font-size: 13px; color: #909399; display: flex; align-items: center; gap: 4px; }
.text-success { color: #67c23a; }
.text-danger { color: #f56c6c; }

.result-actions { display: flex; gap: 12px; justify-content: center; margin-top: 20px; }

.generating-content { text-align: center; padding: 20px 0; }
.generating-content p { margin: 16px 0 0 0; color: #606266; }
.generating-content .tip { font-size: 13px; color: #909399; }

.explanation-box { margin-top: 12px; padding: 10px 14px; background: #f4f4f5; border-radius: 8px; font-size: 13px; color: #606266; display: flex; align-items: flex-start; gap: 6px; line-height: 1.5; }
.correct-icon, .wrong-icon { margin-left: auto; flex-shrink: 0; }
</style>