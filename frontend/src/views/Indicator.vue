<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>评价指标管理</span>
        <el-button type="primary" @click="openAdd">新增指标</el-button>
      </div>
    </template>
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="name" label="指标名称" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column prop="defaultWeight" label="默认权重(%)" width="120" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="isSystem" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isSystem === 1 ? 'danger' : 'success'">
            {{ row.isSystem === 1 ? '系统' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)" :disabled="row.isSystem === 1">删除</el-button>
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
      @size-change="loadIndicators"
      @current-change="loadIndicators"
    />
  </el-card>
  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑评价指标' : '新增评价指标'" width="500px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="指标名称" required>
        <el-input v-model="form.name" placeholder="如：代码质量" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-input v-model="form.category" placeholder="如：技术能力" />
      </el-form-item>
      <el-form-item label="权重(%)" required>
        <el-input-number v-model="form.defaultWeight" :min="1" :max="100" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="指标说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = {
  id: null,
  name: '',
  category: '',
  defaultWeight: 20,
  description: ''
}
const form = reactive({ ...defaultForm })

// 加载评价指标
const loadIndicators = async () => {
  loading.value = true
  try {
    const res = await request.get('/indicator/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载评价指标失败:', e)
  } finally {
    loading.value = false
  }
}

// 打开新增对话框
const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { ...defaultForm })
  dialogVisible.value = true
}

// 打开编辑对话框
const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!form.name || !form.category) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/indicator', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/indicator', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadIndicators()
  } catch (e) {
    console.error('操作失败:', e)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除指标
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该指标？', '提示', { type: 'warning' })
    await request.delete(`/indicator/${id}`)
    ElMessage.success('删除成功')
    loadIndicators()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

onMounted(loadIndicators)
</script>
