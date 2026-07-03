<!--
  个人中心 — 头像上传、基本信息编辑、修改密码
-->
<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧：头像卡片 -->
      <el-col :span="7">
        <el-card class="avatar-card" shadow="hover">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="/api/user/avatar"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              accept="image/*"
              with-credentials
            >
              <el-avatar :size="96" :src="avatarUrl" class="profile-avatar">
                <el-icon :size="40"><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="18"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </el-upload>
            <h3 class="profile-name">{{ userStore.userInfo.realName || '用户' }}</h3>
            <el-tag :type="roleTagType" effect="plain" round>{{ roleLabel }}</el-tag>
            <p class="profile-username">@{{ userStore.userInfo.username }}</p>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：基本信息 + 修改密码 -->
      <el-col :span="17">
        <!-- 基本信息 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><User /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <el-form :model="form" :rules="infoRules" ref="infoRef" label-width="80px" class="info-form">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile" :loading="saving">
                <el-icon><Check /></el-icon> 保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 修改密码 -->
        <el-card class="info-card" shadow="hover" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </div>
          </template>
          <el-form :model="pwdForm" :rules="pwdRules" ref="pwdRef" label-width="100px" class="info-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password
                        placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password
                        placeholder="请输入新密码（至少4位）" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password
                        placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="changePassword" :loading="changing">
                <el-icon><Key /></el-icon> 修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { UserFilled, Camera, User, Lock, Key, Check } from '@element-plus/icons-vue'

const userStore = useUserStore()
const saving = ref(false)
const changing = ref(false)
const infoRef = ref(null)
const pwdRef = ref(null)

const role = computed(() => userStore.userInfo.role || 'student')
const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[role.value] || '用户'
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[role.value] || 'info'
})

// 头像
const avatarUrl = computed(() => userStore.userInfo.avatar || '')
const uploadHeaders = computed(() => ({
  Authorization: userStore.token
}))

// 基本信息
const form = reactive({ realName: '', email: '', phone: '' })
const infoRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

// 密码
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 4, message: '密码长度不能少于4位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

onMounted(async () => {
  try {
    const res = await request.get('/user/profile')
    form.realName = res.data.realName || ''
    form.email = res.data.email || ''
    form.phone = res.data.phone || ''
  } catch (e) {}
})

// 保存基本信息
const saveProfile = async () => {
  const valid = await infoRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await request.put('/user/profile', form)
    const res = await request.get('/auth/info')
    userStore.setUserInfo(res.data)
    ElMessage.success('保存成功')
  } catch (e) {} finally { saving.value = false }
}

// 修改密码
const changePassword = async () => {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  changing.value = true
  try {
    await request.put('/user/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdRef.value.resetFields()
  } catch (e) {} finally { changing.value = false }
}

// 头像上传回调
const handleAvatarSuccess = async (res) => {
  if (res.code === 200) {
    ElMessage.success('头像更新成功')
    const info = await request.get('/auth/info')
    userStore.setUserInfo(info.data)
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const handleAvatarError = (err) => {
  console.error('头像上传失败:', err)
  ElMessage.error('头像上传失败，请检查网络或重启后端')
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}
</script>

<style scoped>
.profile-page {
  max-width: 1100px;
  margin: 0 auto;
}

/* 头像卡片 */
.avatar-card {
  text-align: center;
}
.avatar-section {
  padding: 24px 0 10px;
}
.profile-avatar {
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  transition: all 0.3s;
}
.avatar-uploader {
  display: inline-block;
  cursor: pointer;
  position: relative;
}
.avatar-overlay {
  position: absolute;
  bottom: 6px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
  background: rgba(0,0,0,0.65);
  color: #fff;
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
  opacity: 0;
  transition: opacity 0.3s;
  white-space: nowrap;
  pointer-events: none;
}
.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}
.avatar-uploader:hover .profile-avatar {
  box-shadow: 0 6px 24px rgba(102,187,106,0.3);
}
.profile-name {
  margin: 16px 0 6px;
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}
.profile-username {
  margin: 8px 0 0;
  font-size: 13px;
  color: #a8abb2;
}

/* 信息卡片 */
.info-card {
  border-radius: 12px;
}
.info-card :deep(.el-card__header) {
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.info-card :deep(.el-card__body) {
  padding: 24px 20px;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.card-header .el-icon {
  color: #66bb6a;
}
.info-form {
  max-width: 460px;
}

/* 全局微调 */
:deep(.el-input__wrapper) {
  border-radius: 8px;
}
:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}
</style>
