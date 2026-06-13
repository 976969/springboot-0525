<template>
  <div>
    <!-- 提交成果区域 -->
    <el-card>
      <template #header>
        <span>实训成果提交</span>
      </template>
      <el-form label-width="100px" style="max-width: 600px">
        <el-form-item label="实训任务" required>
          <el-select v-model="form.taskId" placeholder="请选择要提交的实训任务" style="width: 100%">
            <el-option 
              v-for="task in taskList" 
              :key="task.id" 
              :label="task.title" 
              :value="task.id" 
            />
          </el-select>
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
              <div class="el-upload__tip">支持 Word/PDF/图片格式，单个文件不超过50MB</div>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const fileList = ref([])
const taskList = ref([])
const selectedFiles = ref([])
const loading = ref(false)
const uploading = ref(false)
const uploadRef = ref(null)
// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const form = reactive({
  taskId: null,
  file: null
})

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

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', form.file)
    formData.append('taskId', form.taskId)

    await request.post('/result/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('提交成功！')
    
    // 重置表单
    form.taskId = null
    form.file = null
    selectedFiles.value = []
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
    
    // 刷新列表
    loadFiles()
  } catch (e) {
    console.error('提交失败:', e)
    ElMessage.error('提交失败：' + (e.response?.data?.message || e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 加载实训任务列表
const loadTasks = async () => {
  try {
    const res = await request.get('/task/list')
    taskList.value = res.data || []
  } catch (e) {
    console.error('加载任务列表失败:', e)
  }
}

// 加载当前学生的已上传文件列表
const loadFiles = async () => {
  loading.value = true
  try {
    const res = await request.get('/result/my/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
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
  loadTasks()
  loadFiles()
})
</script>

<style scoped>
</style>
