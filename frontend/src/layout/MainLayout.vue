<!--
  管理员侧边栏布局（角色化菜单、权限控制）
-->
<template>
  <!-- 根据角色显示不同布局 -->
  <div v-if="role === 'admin'" style="height: 100vh">
    <!-- 管理员:侧边栏布局 -->
    <el-container style="height: 100vh">
      <el-aside width="180px" style="background-color: #304156">
        <div class="logo">智能实训评价系统</div>
        <el-menu
          :default-active="$route.path"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/user-manage">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/banner-manage">
            <el-icon><PictureFilled /></el-icon>
            <span>大屏管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/course">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/task">
            <el-icon><List /></el-icon>
            <span>实训任务</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/check">
            <el-icon><CircleCheck /></el-icon>
            <span>智能核查</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/evaluate">
            <el-icon><Star /></el-icon>
            <span>评价管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/indicator">
            <el-icon><SetUp /></el-icon>
            <span>评价指标</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/report">
            <el-icon><DataAnalysis /></el-icon>
            <span>报表中心</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/my-courses">
            <el-icon><Reading /></el-icon>
            <span>我的课程</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/upload">
            <el-icon><Upload /></el-icon>
            <span>成果上传</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/my-scores">
            <el-icon><Star /></el-icon>
            <span>我的成绩</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header class="header">
          <span class="title">{{ $route.meta.title }}</span>
          <div class="header-right">
            <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
            <span class="username">{{ userStore.userInfo.realName || '用户' }}</span>
            <el-dropdown @command="handleCommand">
              <el-avatar :size="32" icon="UserFilled" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main style="padding: 15px; background-color: #f0f2f5; overflow-x: hidden">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  
  <div v-else>
    <!-- 教师/学生:顶部导航栏布局 -->
    <top-navbar-layout />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import TopNavbarLayout from './TopNavbarLayout.vue'

const router = useRouter()
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

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3d4d5e;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  background: #fff;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  font-size: 14px;
  color: #666;
}
</style>
