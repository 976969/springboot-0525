<template>
  <el-card>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>大屏轮播管理</span>
        <el-button type="primary" @click="openAddDialog">新增轮播</el-button>
      </div>
    </template>

    <el-table :data="bannerList" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column label="预览图" width="120" align="center">
        <template #default="{ row }">
          <img v-if="row.imageUrl" :src="row.imageUrl" style="width: 100px; height: 50px; object-fit: cover; border-radius: 4px" />
          <span v-else style="color: #999">无图片</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
      <el-table-column prop="content" label="文案内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" @click="deleteBanner(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingBanner.id ? '编辑轮播' : '新增轮播'" width="600px">
      <el-form :model="editingBanner" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editingBanner.title" placeholder="请输入标题（如：校园科技节开幕）" />
        </el-form-item>
        <el-form-item label="文案内容">
          <el-input v-model="editingBanner.content" type="textarea" :rows="3" placeholder="请输入播报文案" />
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            :show-file-list="false"
            :http-request="customUpload"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <img v-if="editingBanner.imageUrl" :src="editingBanner.imageUrl" style="width: 100%; max-height: 120px; object-fit: cover; border-radius: 4px" />
            <el-button v-else type="primary" plain :loading="uploading">点击上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="editingBanner.sort" :min="0" :max="999" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editingBanner.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveBanner">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const bannerList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const uploading = ref(false)
const editingBanner = ref({
  id: null,
  title: '',
  content: '',
  imageUrl: '',
  sort: 0,
  status: 1
})

const loadBanners = async () => {
  loading.value = true
  try {
    const res = await request.get('/banner/list')
    bannerList.value = res.data || []
  } catch (e) {
    console.error('加载轮播列表失败:', e)
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  editingBanner.value = {
    id: null,
    title: '',
    content: '',
    imageUrl: '',
    sort: 0,
    status: 1
  }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  editingBanner.value = { ...row }
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const customUpload = async (options) => {
  const { file } = options
  uploading.value = true
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post('/banner/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    editingBanner.value.imageUrl = res.data
    ElMessage.success('图片上传成功')
  } catch (e) {
    console.error('上传失败:', e)
    ElMessage.error('图片上传失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const saveBanner = async () => {
  if (!editingBanner.value.title) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!editingBanner.value.imageUrl) {
    ElMessage.warning('请上传图片')
    return
  }
  try {
    if (editingBanner.value.id) {
      await request.put('/banner', editingBanner.value)
    } else {
      await request.post('/banner', editingBanner.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadBanners()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const toggleStatus = async (row) => {
  try {
    await request.put('/banner', { id: row.id, status: row.status === 1 ? 0 : 1 })
    ElMessage.success(row.status === 1 ? '已下架' : '已上架')
    loadBanners()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const deleteBanner = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该轮播图吗？', '提示', { type: 'warning' })
    await request.delete(`/banner/${row.id}`)
    ElMessage.success('删除成功')
    loadBanners()
  } catch (e) {
    // 用户取消
  }
}

onMounted(loadBanners)
</script>
