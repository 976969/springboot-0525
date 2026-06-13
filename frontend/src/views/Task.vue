<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>实训任务</span>
          <el-button type="primary" @click="dialogVisible = true">发布任务</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="courseName" label="所属课程" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">进行中</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">已结束</el-tag>
            <el-tag v-else type="warning">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">查看</el-button>
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
    <el-dialog v-model="dialogVisible" title="发布实训任务" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="任务标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="所属课程"><el-input v-model="form.courseId" /></el-form-item>
        <el-form-item label="任务描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="实训要求"><el-input v-model="form.requirements" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="截止时间"><el-date-picker v-model="form.deadline" type="datetime" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">发布</el-button>
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
const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const form = reactive({ 
  id: null, 
  title: '', 
  courseId: '', 
  description: '', 
  requirements: '', 
  deadline: '' 
})

// 加载实训任务数据
const loadTasks = async () => {
  loading.value = true
  try {
    const res = await request.get('/task/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
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

// 打开编辑对话框
const openEdit = (row) => {
  isEdit.value = !!row.id
  if (row.id) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, title: '', courseId: '', description: '', requirements: '', deadline: '' })
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await request.put('/task', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/task', form)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadTasks()
  } catch (e) {
    console.error('操作失败:', e)
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

onMounted(loadTasks)
</script>
