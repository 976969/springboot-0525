<!--
  实训成果上传页面（按钮选择任务+弹框展示、按课程分组、已提交/未提交状态）
-->
<template>
  <div>
    <!-- 提交成果区域 -->
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-size: 16px; font-weight: 600">📤 实训成果提交</span>
          <el-tag type="success" size="small" effect="plain">推荐 Word 文档(.docx)</el-tag>
        </div>
      </template>

      <!-- 步骤引导 -->
      <div class="upload-steps">
        <!-- 步骤 1：选择任务 -->
        <div class="step-item" :class="{ 'step-active': selectedTask }" @click="openTaskDialog">
          <div class="step-number">1</div>
          <div class="step-content">
            <div class="step-title">选择实训任务</div>
            <div v-if="selectedTask" class="step-desc">
              <el-tag type="success" size="small" effect="plain">{{ selectedTask.courseName }}</el-tag>
              <span class="task-name">{{ selectedTask.title }}</span>
            </div>
            <div v-else class="step-desc empty">点击此处选择要提交的任务</div>
          </div>
          <el-icon class="step-arrow" :size="20" color="#c0c4cc"><ArrowRight /></el-icon>
        </div>

        <!-- 步骤 2：上传文件 -->
        <div class="step-item" :class="{ 'step-active': form.file }" v-if="selectedTask">
          <div class="step-number">2</div>
          <div class="step-content">
            <div class="step-title">上传文件</div>
            <div v-if="form.file" class="step-desc">
              <el-icon color="#67c23a"><Document /></el-icon>
              <span class="file-name">{{ form.file.name }}</span>
              <span class="file-size">{{ formatSize(form.file.size) }}</span>
            </div>
            <div v-else class="step-desc empty">点击右侧按钮选择文件</div>
          </div>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="selectedFiles"
            accept=".doc,.docx,.pdf,.jpg,.jpeg,.png"
            class="inline-upload"
          >
            <el-button type="primary" size="default">
              {{ form.file ? '更换文件' : '选择文件' }}
            </el-button>
          </el-upload>
        </div>

        <!-- 步骤 3：提交 -->
        <div class="step-item step-submit" :class="{ 'step-ready': form.taskId && form.file }" v-if="selectedTask">
          <div class="step-number">3</div>
          <div class="step-content">
            <div class="step-title">确认提交</div>
            <div class="step-desc" :class="{ empty: !form.taskId || !form.file }">
              <template v-if="form.taskId && form.file">
                <el-icon color="#67c23a"><CircleCheck /></el-icon>
                所有信息已就绪，点击下方按钮提交
              </template>
              <template v-else>
                请先完成以上步骤
              </template>
            </div>
          </div>
          <el-button
            type="success"
            size="large"
            @click="submitUpload"
            :loading="uploading"
            :disabled="!form.taskId || !form.file"
            class="submit-btn-inline"
          >
            <el-icon style="margin-right: 6px"><UploadFilled /></el-icon>
            提交成果
          </el-button>
        </div>
      </div>

      <div class="submit-hint">提交后将由 AI 自动评分，请耐心等待结果</div>

      <!-- 格式说明面板 -->
      <el-collapse v-model="formatTipVisible" class="format-collapse">
        <el-collapse-item name="tips">
          <template #title>
            <span style="font-size: 14px; color: #909399">📋 文件格式说明</span>
          </template>
          <div class="format-tips">
            <div class="tip-row">
              <el-icon color="#409eff" :size="16"><CircleCheck /></el-icon>
              <div>
                <strong>推荐格式：</strong>Word文档(.docx) — AI识别率最高
              </div>
            </div>
            <div class="tip-row">
              <el-icon color="#67c23a" :size="16"><CircleCheck /></el-icon>
              <div>
                <strong>支持格式：</strong>.doc / .docx / .pdf / .jpg / .jpeg / .png
              </div>
            </div>
            <div class="tip-row">
              <el-icon color="#e6a23c" :size="16"><Warning /></el-icon>
              <div>
                <strong>注意事项：</strong>图片格式(jpg/png)无法识别手写内容，建议使用Word或PDF
              </div>
            </div>
            <div class="tip-row">
              <el-icon color="#909399" :size="16"><InfoFilled /></el-icon>
              <div>
                <strong>文件大小：</strong>单个文件不超过 50MB
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 已提交记录 -->
    <el-card style="margin-top: 20px">
      <template #header>我的提交记录</template>
      <el-table :data="fileList" border stripe v-loading="loading">
        <el-table-column prop="fileName" label="文件名" min-width="200" />
        <el-table-column prop="taskTitle" label="实训任务" min-width="180" />
        <el-table-column prop="fileSize" label="大小" width="100">
          <template #default="{ row }">
            {{ row.fileSize ? formatSize(row.fileSize) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="info">待评价</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">评价中</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已评价</el-tag>
            <el-tag v-else>未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadFiles"
        @current-change="loadFiles"
      />
      
      <el-empty v-if="fileList.length === 0 && !loading" description="暂无提交记录" />
    </el-card>

    <!-- 选择实训任务对话框 -->
    <el-dialog v-model="taskDialogVisible" title="选择实训任务" width="750px" top="5vh">
      <el-input v-model="taskSearch" placeholder="搜索任务名称" size="small" clearable style="margin-bottom: 15px;" />
      <div class="task-list-container" v-loading="taskLoading">
        <!-- 未过期任务（正常显示） -->
        <div v-for="group in filteredActiveGroups" :key="'active-' + group.courseName" style="margin-bottom: 16px;">
          <div class="course-header">
            <el-tag type="primary" size="small">{{ group.courseName }}</el-tag>
            <span class="teacher-name">授课教师：{{ group.teacherName || '未知' }}</span>
          </div>
          <div 
            v-for="task in group.tasks" 
            :key="task.id" 
            class="task-item"
            :class="{ 'task-selected': tempSelectedTask?.id === task.id }"
            @click="selectTask(task)"
          >
            <div class="task-info">
              <div class="task-title">
                {{ task.title }}
                <el-tag v-if="submittedTaskIds.has(task.id)" type="success" size="small">已提交</el-tag>
                <el-tag v-else type="warning" size="small">未提交</el-tag>
              </div>
              <div class="task-meta">
                <span v-if="task.deadline">截止时间：{{ task.deadline }}</span>
                <span v-else>无截止时间</span>
              </div>
            </div>
            <el-icon v-if="tempSelectedTask?.id === task.id" color="#409eff" :size="20"><Select /></el-icon>
          </div>
          <el-empty v-if="group.tasks.length === 0" description="暂无任务" :image-size="40" />
        </div>
        <el-empty v-if="filteredActiveGroups.length === 0 && filteredExpiredGroups.length === 0" description="暂无可选的实训任务" :image-size="80" />

        <!-- 已过期任务（折叠区域） -->
        <div v-if="filteredExpiredGroups.length > 0" class="expired-section">
          <div class="expired-toggle" @click="showExpired = !showExpired">
            <el-icon :size="16" :style="{ transform: showExpired ? 'rotate(90deg)' : 'rotate(0)', transition: 'transform 0.2s' }"><ArrowRight /></el-icon>
            <span>已过期任务（{{ expiredTaskCount }} 个）</span>
          </div>
          <div v-show="showExpired" class="expired-content">
            <div v-for="group in filteredExpiredGroups" :key="'expired-' + group.courseName" style="margin-bottom: 12px;">
              <div class="course-header expired-header">
                <el-tag type="info" size="small" effect="plain">{{ group.courseName }}</el-tag>
                <span class="teacher-name">{{ group.teacherName || '未知' }}</span>
              </div>
              <div 
                v-for="task in group.tasks" 
                :key="task.id" 
                class="task-item task-expired"
                @click="selectTask(task)"
              >
                <div class="task-info">
                  <div class="task-title">
                    {{ task.title }}
                    <el-tag type="danger" size="small">已过期</el-tag>
                    <el-tag v-if="submittedTaskIds.has(task.id)" type="success" size="small">已提交</el-tag>
                  </div>
                  <div class="task-meta">
                    <span v-if="task.deadline">截止时间：{{ task.deadline }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Select, ArrowRight, Document, UploadFilled, CircleCheck, Warning, InfoFilled } from '@element-plus/icons-vue'
import request from '../utils/request'

const fileList = ref([])
const selectedFiles = ref([])
const loading = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({
  taskId: null,
  file: null
})

// ============ 任务选择弹框 ============
const taskDialogVisible = ref(false)
const taskLoading = ref(false)
const taskSearch = ref('')
const allTasks = ref([])              // 全部活跃任务
const myCourses = ref([])              // 学生已选课程
const myResults = ref([])              // 学生已提交成果
const selectedTask = ref(null)         // 当前选中的任务对象
const tempSelectedTask = ref(null)     // 弹框中临时选中的任务
const showExpired = ref(false)           // 是否展开已过期任务
const formatTipVisible = ref([])          // 格式说明折叠面板

// 已选课程ID集合
const enrolledCourseIds = computed(() => new Set(myCourses.value.map(c => c.courseId)))

// 已提交任务ID集合
const submittedTaskIds = computed(() => new Set(myResults.value.map(r => r.taskId)))

// 课程→教师名映射
const courseTeacherMap = computed(() => {
  const map = {}
  myCourses.value.forEach(c => {
    map[c.courseId] = c.teacherName
  })
  return map
})

// 将任务分为未过期和已过期两组，按课程分组
const activeGroups = computed(() => {
  const groups = {}
  allTasks.value.forEach(task => {
    if (!enrolledCourseIds.value.has(task.courseId)) return
    if (isTaskExpired(task)) return
    const key = task.courseName || '未知课程'
    if (!groups[key]) {
      groups[key] = {
        courseName: key,
        teacherName: courseTeacherMap.value[task.courseId] || '未知',
        tasks: []
      }
    }
    groups[key].tasks.push(task)
  })
  return Object.values(groups)
})

const expiredGroups = computed(() => {
  const groups = {}
  allTasks.value.forEach(task => {
    if (!enrolledCourseIds.value.has(task.courseId)) return
    if (!isTaskExpired(task)) return
    const key = task.courseName || '未知课程'
    if (!groups[key]) {
      groups[key] = {
        courseName: key,
        teacherName: courseTeacherMap.value[task.courseId] || '未知',
        tasks: []
      }
    }
    groups[key].tasks.push(task)
  })
  return Object.values(groups)
})

// 过期任务总数
const expiredTaskCount = computed(() => {
  return expiredGroups.value.reduce((sum, g) => sum + g.tasks.length, 0)
})

// 搜索过滤后的未过期任务分组
const filteredActiveGroups = computed(() => {
  const keyword = taskSearch.value.trim().toLowerCase()
  if (!keyword) return activeGroups.value
  return activeGroups.value.map(g => ({
    ...g,
    tasks: g.tasks.filter(t => t.title?.toLowerCase().includes(keyword))
  })).filter(g => g.tasks.length > 0)
})

// 搜索过滤后的已过期任务分组
const filteredExpiredGroups = computed(() => {
  const keyword = taskSearch.value.trim().toLowerCase()
  if (!keyword) return expiredGroups.value
  return expiredGroups.value.map(g => ({
    ...g,
    tasks: g.tasks.filter(t => t.title?.toLowerCase().includes(keyword))
  })).filter(g => g.tasks.length > 0)
})

// 判断任务是否过期
const isTaskExpired = (task) => {
  if (!task.deadline) return false
  return new Date(task.deadline) < new Date()
}

// 打开任务选择弹框
const openTaskDialog = async () => {
  tempSelectedTask.value = selectedTask.value
  showExpired.value = false
  taskDialogVisible.value = true
  if (allTasks.value.length === 0) {
    taskLoading.value = true
    try {
      const [taskRes, courseRes, resultRes] = await Promise.all([
        request.get('/task/list'),
        request.get('/user/my-courses'),
        request.get('/result/my')
      ])
      allTasks.value = taskRes.data || []
      myCourses.value = courseRes.data || []
      myResults.value = resultRes.data || []
    } catch (e) {
      console.error('加载数据失败:', e)
    } finally {
      taskLoading.value = false
    }
  }
}

// 在弹框中点选任务
const selectTask = (task) => {
  if (isTaskExpired(task)) {
    ElMessage.warning('该任务已过期，无法提交')
    return
  }
  tempSelectedTask.value = task
}

// 确认选择
const confirmTask = () => {
  if (!tempSelectedTask.value) {
    ElMessage.warning('请选择一个实训任务')
    return
  }
  selectedTask.value = tempSelectedTask.value
  form.taskId = tempSelectedTask.value.id
  taskDialogVisible.value = false
}

// 格式化文件大小
const formatSize = (bytes) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// 文件选择变化
const handleFileChange = (file) => {
  form.file = file.raw
}

// 文件移除
const handleFileRemove = () => {
  form.file = null
}

// 提交上传
const submitUpload = async () => {
  if (!form.taskId) {
    ElMessage.warning('请选择实训任务')
    return
  }
  if (!form.file) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  if (selectedTask.value && isTaskExpired(selectedTask.value)) {
    ElMessage.error('该任务已过期,无法提交')
    return
  }

  uploading.value = true
  try {
    await doUpload(false)
  } catch (e) {
    console.error('提交失败:', e)
    if (e.isConflict || (e.response && e.response.data && e.response.data.code === 409)) {
      try {
        await ElMessageBox.confirm(
          e.message || '该任务已提交过成果,是否覆盖提交?',
          '提示',
          { confirmButtonText: '覆盖提交', cancelButtonText: '取消', type: 'warning' }
        )
        await doUpload(true)
        ElMessage.success('覆盖成功!')
      } catch (confirmErr) {
        if (confirmErr === 'cancel') {
          ElMessage.info('已取消上传')
        } else {
          ElMessage.error('覆盖失败:' + (confirmErr.response?.data?.message || confirmErr.message))
        }
      }
    } else {
      ElMessage.error('提交失败:' + (e.response?.data?.message || e.message || '未知错误'))
    }
  } finally {
    uploading.value = false
  }
}

// 执行上传
const doUpload = async (overwrite) => {
  const formData = new FormData()
  formData.append('file', form.file)
  formData.append('taskId', form.taskId)
  formData.append('overwrite', overwrite)

  const res = await request.post('/result/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  
  if (!overwrite) {
    ElMessage.success('提交成功!')
  }
  
  // 重置表单
  form.taskId = null
  form.file = null
  selectedTask.value = null
  selectedFiles.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  
  // 刷新列表和提交状态
  await loadFiles()
  // 刷新已提交状态
  try {
    const resultRes = await request.get('/result/my')
    myResults.value = resultRes.data || []
  } catch (e) { /* ignore */ }
  
  return res
}

// 加载当前学生的已上传文件列表
const loadFiles = async () => {
  loading.value = true
  try {
    const res = await request.get('/result/my/page', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    fileList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载文件列表失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
/* ============ 步骤引导 ============ */
.upload-steps {
  padding: 10px 0 10px;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #fafafa;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  margin-bottom: 0;
}
.step-item.step-active {
  background: #f0f9eb;
  border-color: #c2e7b0;
}
/* 步骤1整行可点击 */
.step-item:first-child {
  cursor: pointer;
}
.step-item:first-child:hover {
  border-color: #409eff;
  background: #ecf5ff;
}
.step-item:first-child.step-active:hover {
  border-color: #c2e7b0;
  background: #f0f9eb;
}
.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e4e7ed;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  transition: all 0.3s;
}
.step-active .step-number {
  background: #67c23a;
  color: #fff;
}
.step-content {
  flex: 1;
  min-width: 0;
}
.step-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}
.step-desc {
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 6px;
}
.step-desc.empty {
  color: #c0c4cc;
}
.task-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-name {
  font-weight: 500;
  color: #303133;
}
.file-size {
  color: #909399;
  font-size: 13px;
}
.step-arrow {
  flex-shrink: 0;
  transition: transform 0.2s;
}
.step-item:first-child:hover .step-arrow {
  transform: translateX(4px);
  color: #409eff !important;
}
.inline-upload {
  display: inline-flex;
}
.inline-upload :deep(.el-upload) {
  display: inline-flex;
}

/* ============ 步骤3：提交 ============ */
.step-item.step-submit {
  background: #f0f9eb;
  border-color: #c2e7b0;
  border-width: 2px;
  margin-top: 4px;
}
.step-item.step-submit.step-ready {
  background: #f0f9eb;
  border-color: #67c23a;
  box-shadow: 0 2px 12px rgba(103, 194, 58, 0.15);
}
.step-item.step-submit .step-number {
  background: #67c23a;
  color: #fff;
}
.submit-btn-inline {
  padding: 12px 36px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 600;
  flex-shrink: 0;
}

/* ============ 提交提示 ============ */
.submit-hint {
  text-align: center;
  font-size: 13px;
  color: #c0c4cc;
  margin-top: 12px;
  margin-bottom: 4px;
}

/* ============ 格式说明 ============ */
.format-collapse {
  margin-top: 16px;
  border: none;
}
.format-collapse :deep(.el-collapse-item__header) {
  height: 36px;
  line-height: 36px;
}
.format-tips {
  padding: 4px 0 8px 8px;
}
.tip-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 6px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}
.tip-row strong {
  color: #303133;
}

/* ============ 任务列表弹框 ============ */
.task-list-container {
  max-height: 500px;
  overflow-y: auto;
}
.course-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid #ecf5ff;
}
.teacher-name {
  font-size: 13px;
  color: #909399;
}
.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 6px;
  cursor: pointer;
  transition: all 0.2s;
}
.task-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}
.task-item.task-selected {
  border-color: #409eff;
  border-width: 2px;
  background-color: #ecf5ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}
.task-info {
  flex: 1;
}
.task-title {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.task-meta {
  font-size: 13px;
  color: #909399;
}

/* 已过期任务折叠区域 */
.expired-section {
  margin-top: 8px;
  border-top: 1px dashed #dcdfe6;
  padding-top: 12px;
}
.expired-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  color: #909399;
  font-size: 14px;
  user-select: none;
  transition: color 0.2s;
}
.expired-toggle:hover {
  color: #606266;
}
.expired-content {
  padding: 4px 0 0 20px;
}
.expired-header {
  opacity: 0.7;
}
.task-item.task-expired {
  background-color: #f5f5f5;
  border-color: #d9d9d9;
  cursor: not-allowed;
  opacity: 0.65;
}
.task-item.task-expired:hover {
  border-color: #d9d9d9;
  background-color: #ebebeb;
}
</style>
