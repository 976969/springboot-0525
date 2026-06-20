<!--
  实训任务管理页面（任务发布、过期状态、列排序）
-->
<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>实训任务</span>
          <el-button type="primary" @click="dialogVisible = true">发布任务</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading" @sort-change="handleSortChange">
        <!-- 序号列 -->
        <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (pageNum - 1) * pageSize + i + 1" />
        
        <el-table-column prop="courseName" label="所属课程" sortable="custom" />
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="deadline" label="截止时间" width="180" sortable="custom" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1 && !isExpired(row)" type="success">进行中</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">已结束</el-tag>
            <el-tag v-else-if="isExpired(row)" type="danger">已过期</el-tag>
            <el-tag v-else type="warning">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="openView(row)">查看</el-button>
            <el-button size="small" type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadTasks"
        @current-change="loadTasks"
      />
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isView ? '查看实训任务' : (isEdit ? '编辑实训任务' : '发布实训任务')" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="任务标题">
          <el-input v-model="form.title" :disabled="isView" />
        </el-form-item>
        <el-form-item label="所属课程">
          <el-select v-model="form.courseId" placeholder="请选择课程" style="width: 100%" :disabled="isView">
            <el-option
              v-for="course in courseList"
              :key="course.id"
              :label="course.name"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="3" :disabled="isView" />
        </el-form-item>
        <el-form-item label="实训要求">
          <el-input v-model="form.requirements" type="textarea" :rows="4" :disabled="isView" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="form.deadline" type="datetime" style="width: 100%" :disabled="isView" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="!isView" type="primary" @click="handleSubmit">{{ isEdit ? '更新' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false) // 查看模式
const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const courseList = ref([]) // 课程列表
const sortField = ref('')  // 排序字段: courseName / deadline
const sortOrder = ref('')  // 排序方向: asc / desc
const form = reactive({ 
  id: null, 
  title: '', 
  courseId: '', 
  description: '', 
  requirements: '', 
  deadline: '' 
})

// 加载课程列表
const loadCourses = async () => {
  try {
    const res = await request.get('/course/list')
    courseList.value = res.data || []
  } catch (e) {
    console.error('加载课程列表失败:', e)
  }
}

// 加载实训任务数据
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await request.get('/task/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        sortField: sortField.value || undefined,
        sortOrder: sortOrder.value || undefined
      }
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载任务失败:', e)
  } finally {
    loading.value = false
  }
}

// 打开查看对话框(只读)
const openView = (row) => {
  isView.value = true
  isEdit.value = false
  Object.assign(form, row)
  dialogVisible.value = true
}

// 打开编辑对话框
const openEdit = (row) => {
  isView.value = false
  isEdit.value = !!row.id
  if (row.id) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, title: '', courseId: '', description: '', requirements: '', deadline: '' })
  }
  dialogVisible.value = true
}

// 判断是否过期
const isExpired = (row) => {
  if (!row.deadline) return false
  return new Date(row.deadline) < new Date()
}

// 提交表单
const handleSubmit = async () => {
  // 验证必填字段
  if (!form.title || !form.courseId) {
    ElMessage.warning('请填写任务标题和选择所属课程')
    return
  }
  
  // 格式化日期
  const submitData = {
    ...form,
    deadline: form.deadline ? new Date(form.deadline).toISOString().slice(0, 19).replace('T', ' ') : null
  }
  
  try {
    if (isEdit.value) {
      await request.put('/task', submitData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/task', submitData)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadTasks()
  } catch (e) {
    console.error('操作失败:', e)
    ElMessage.error('操作失败: ' + (e.message || '未知错误'))
  }
}

// 删除任务
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该任务？', '提示', { type: 'warning' })
    await request.delete(`/task/${id}`)
    ElMessage.success('删除成功')
    loadTasks()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

// 排序变化处理
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop || ''
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  pageNum.value = 1
  loadTasks()
}

onMounted(async () => {
  await loadCourses() // 先加载课程列表
  await loadTasks()   // 再加载任务列表
})
</script>
