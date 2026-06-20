<!--
  实训成果上传页面（按钮选择任务+弹框展示、按课程分组、已提交/未提交状态）
-->
<template>
  <div>
    <!-- 提交成果区域 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>实训成果提交</span>
          <el-tag type="success" size="small">💡 推荐提交 Word 文档(.docx)</el-tag>
        </div>
      </template>
      <el-form label-width="100px" style="max-width: 600px">
        <el-form-item label="实训任务" required>
          <div style="display: flex; gap: 10px; align-items: center; width: 100%;">
            <el-button type="primary" @click="openTaskDialog">选择实训任务</el-button>
            <span v-if="selectedTask" class="selected-task-info">
              <el-tag type="success" size="small">{{ selectedTask.courseName }}</el-tag>
              {{ selectedTask.title }}
            </span>
            <span v-else style="color: #999; font-size: 13px;">请点击按钮选择实训任务</span>
          </div>
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="selectedFiles"
            accept=".doc,.docx,.pdf,.jpg,.jpeg,.png"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                <div style="margin-bottom: 8px;">支持 Word/PDF/图片格式，单个文件不超过50MB</div>
                <el-alert 
                  title="💡 AI评分格式建议" 
                  type="info" 
                  :closable="false" 
                  show-icon
                  style="margin-top: 8px;"
                >
                  <div style="line-height: 1.8;">
                    <strong>推荐格式：</strong>Word文档(.docx) → AI识别率最高<br/>
                    <strong>支持格式：</strong>.doc / .docx / .pdf / .jpg / .jpeg / .png<br/>
                    <strong>注意事项：</strong>图片格式(jpg/png)无法识别手写内容，建议使用Word或PDF
                  </div>
                </el-alert>
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="submitUpload" :loading="uploading" :disabled="!form.taskId || !form.file">
            提交成果
          </el-button>
        </el-form-item>
      </el-form>
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
            <el-tag v-if="row.status === 0" type="info">待处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="warning">已核查</el-tag>
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
        <div v-for="group in filteredTaskGroups" :key="group.courseName" style="margin-bottom: 16px;">
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
                <el-tag v-if="isTaskExpired(task)" type="danger" size="small">已过期</el-tag>
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
        <el-empty v-if="filteredTaskGroups.length === 0" description="暂无可选的实训任务" :image-size="80" />
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
import { Select } from '@element-plus/icons-vue'
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

// 只保留学生已选课程对应的任务，按课程分组
const taskGroups = computed(() => {
  const groups = {}
  allTasks.value.forEach(task => {
    if (!enrolledCourseIds.value.has(task.courseId)) return
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

// 搜索过滤后的任务分组
const filteredTaskGroups = computed(() => {
  const keyword = taskSearch.value.trim().toLowerCase()
  if (!keyword) return taskGroups.value
  return taskGroups.value.map(g => ({
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
.selected-task-info {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
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
  font-size: 12px;
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
  font-size: 12px;
  color: #909399;
}
</style>
