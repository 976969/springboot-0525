<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>课程管理</span>
          <el-button type="primary" @click="dialogVisible = true">新增课程</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="description" label="课程描述" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
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
        @size-change="loadCourses"
        @current-change="loadCourses"
      />
    </el-card>
    <el-dialog v-model="dialogVisible" title="新增课程" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="课程名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="课程描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
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
const form = reactive({ id: null, name: '', description: '', teacherId: '' })

// 加载课程数据
const loadCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/course/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载课程失败:', e)
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await request.put('/course', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/course', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadCourses()
  } catch (e) {
    console.error('操作失败:', e)
  }
}

// 删除课程
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该课程？', '提示', { type: 'warning' })
    await request.delete(`/course/${id}`)
    ElMessage.success('删除成功')
    loadCourses()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

onMounted(loadCourses)
</script>
