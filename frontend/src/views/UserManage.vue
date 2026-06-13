<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>用户管理</span>
        <el-button type="primary" @click="openDialog(null)">新增</el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="loadList">
      <el-tab-pane label="管理员" name="admin" />
      <el-tab-pane label="教师" name="teacher" />
      <el-tab-pane label="学生" name="student" />
    </el-tabs>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column v-if="activeTab === 'teacher'" prop="department" label="院系" width="120" />
      <el-table-column v-if="activeTab === 'teacher'" prop="title" label="职称" width="100" />
      <el-table-column v-if="activeTab === 'student'" prop="studentNo" label="学号" width="120" />
      <el-table-column v-if="activeTab === 'student'" prop="className" label="班级" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
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
      @size-change="loadList"
      @current-change="loadList"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名" v-if="!isEdit">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码" v-if="!isEdit">
        <el-input v-model="form.password" type="password" placeholder="默认123456" />
      </el-form-item>
      <el-form-item label="真实姓名">
        <el-input v-model="form.realName" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>
      <template v-if="activeTab === 'teacher'">
        <el-form-item label="院系">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" />
        </el-form-item>
      </template>
      <template v-if="activeTab === 'student'">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" />
        </el-form-item>
      </template>
      <el-form-item label="状态" v-if="isEdit">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('admin')
const dialogVisible = ref(false)
const isEdit = ref(false)
const tableData = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const defaultForm = {
  id: null, username: '', password: '123456', realName: '',
  email: '', phone: '', status: 1, department: '', title: '', studentNo: '', className: ''
}
const form = reactive({ ...defaultForm })

// 加载列表数据
const loadList = async () => {
  loading.value = true
  try {
    const res = await request.get(`/user/${activeTab.value}/page`, {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载用户列表失败:', e)
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 监听 tab 切换，确保数据正确加载
watch(activeTab, (newTab) => {
  console.log('切换到:', newTab)
  pageNum.value = 1  // 切换tab时重置页码
  loadList()
})

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, { ...defaultForm, ...row })
  } else {
    isEdit.value = false
    Object.assign(form, { ...defaultForm })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    const type = activeTab.value
    if (isEdit.value) {
      await request.put(`/user/${type}`, form)
    } else {
      await request.post(`/user/${type}`, form)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    console.error('操作失败:', e)
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该用户？', '提示', { type: 'warning' })
    const type = activeTab.value
    await request.delete(`/user/${type}/${id}`)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    console.error('删除失败:', e)
  }
}

onMounted(loadList)
</script>