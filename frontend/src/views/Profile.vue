<!--
  个人中心页面（用户信息查看、密码修改）
-->
<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div style="text-align: center; padding: 20px">
            <el-avatar :size="80" icon="UserFilled" />
            <h3 style="margin-top: 12px">{{ userStore.userInfo.realName || '用户' }}</h3>
            <el-tag :type="roleTagType" style="margin-top: 8px">{{ roleLabel }}</el-tag>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>基本信息</template>
          <el-form :model="form" label-width="80px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input :value="userStore.userInfo.username" disabled />
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
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px">
          <template #header>修改密码</template>
          <el-form :model="pwdForm" label-width="80px" style="max-width: 500px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="changePassword">修改密码</el-button>
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

const userStore = useUserStore()

const role = computed(() => userStore.userInfo.role || 'student')
const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[role.value] || '用户'
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[role.value] || 'info'
})

const form = reactive({
  realName: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

onMounted(async () => {
  try {
    const res = await request.get('/user/profile')
    form.realName = res.data.realName || ''
    form.email = res.data.email || ''
    form.phone = res.data.phone || ''
  } catch (e) {}
})

const saveProfile = async () => {
  try {
    await request.put('/user/profile', form)
    // 更新store中的信息
    const res = await request.get('/auth/info')
    userStore.setUserInfo(res.data)
    ElMessage.success('保存成功')
  } catch (e) {}
}

const changePassword = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  try {
    await request.put('/user/password', pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
  } catch (e) {}
}
</script>
