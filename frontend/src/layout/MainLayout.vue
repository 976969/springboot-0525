<!--
  管理员侧边栏布局（可收缩侧边栏、角色化菜单）
-->
<template>
  <div v-if="role === 'admin'" style="height: 100vh">
    <el-container style="height: 100vh">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <div class="logo">
          <span v-if="!isCollapse">智能实训评价系统</span>
          <span v-else class="logo-collapsed">实训</span>
        </div>
        <el-menu
          :default-active="$route.path"
          background-color="#1d1e2c"
          text-color="#aeb5c4"
          active-text-color="#66bb6a"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
        >
          <el-menu-item :index="role === 'admin' ? '/admin-home' : '/home'">
            <el-icon><HomeFilled /></el-icon>
            <span>{{ role === 'admin' ? '系统总览' : '首页' }}</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/user-manage">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/banner-manage">
            <el-icon><PictureFilled /></el-icon>
            <span>大屏管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'admin'" index="/schedule-manage">
            <el-icon><Calendar /></el-icon>
            <span>选课管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/course">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/task">
            <el-icon><List /></el-icon>
            <span>实训任务</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/indicator">
            <el-icon><SetUp /></el-icon>
            <span>评价指标</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher' || role === 'admin'" index="/evaluate">
            <el-icon><Star /></el-icon>
            <span>{{ role === 'admin' ? '评分与报表' : 'AI评分' }}</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'teacher'" index="/report">
            <el-icon><DataAnalysis /></el-icon>
            <span>报表中心</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/course-select">
            <el-icon><Plus /></el-icon>
            <span>选课中心</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/my-courses">
            <el-icon><Reading /></el-icon>
            <span>我的课程</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/ai-practice">
            <el-icon><EditPen /></el-icon>
            <span>AI练习</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/upload">
            <el-icon><Upload /></el-icon>
            <span>成果上传</span>
          </el-menu-item>
          <el-menu-item v-if="role === 'student'" index="/my-scores">
            <el-icon><Star /></el-icon>
            <span>我的成绩</span>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" :size="22" @click="isCollapse = !isCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <span class="title">{{ $route.meta.title }}</span>
          </div>
          <div class="header-right">
            <el-tag :type="roleTagType" size="small" effect="plain">{{ roleLabel }}</el-tag>
            <span class="username">{{ userStore.userInfo.realName || '用户' }}</span>
            <el-dropdown @command="handleCommand">
              <el-avatar :size="34" :src="avatarSrc" icon="UserFilled" class="header-avatar" />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main class="main-area">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>

  <div v-else>
    <top-navbar-layout />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import TopNavbarLayout from './TopNavbarLayout.vue'
import { Fold, Expand } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const role = computed(() => userStore.userInfo.role || 'student')
const roleLabel = computed(() => {
  const map = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[role.value] || '用户'
})
const roleTagType = computed(() => {
  const map = { admin: 'danger', teacher: 'warning', student: 'success' }
  return map[role.value] || 'info'
})
const avatarSrc = computed(() => userStore.userInfo.avatar || '')

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
/* 侧边栏 */
.sidebar {
  background-color: #1d1e2c !important;
  transition: width 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.sidebar :deep(.el-menu) {
  border-right: none;
}

/* 修复 el-menu 折叠时图标居中 */
.sidebar :deep(.el-menu--collapse) {
  width: 64px;
}
.sidebar :deep(.el-menu--collapse .el-menu-item) {
  justify-content: center;
  padding: 0 !important;
}

/* Logo */
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  background: #1d1e2c;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  white-space: nowrap;
  letter-spacing: 1px;
  flex-shrink: 0;
}
.logo-collapsed {
  font-size: 14px;
  letter-spacing: 2px;
}

/* 顶部栏 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  padding: 0 20px;
  z-index: 10;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.collapse-btn {
  cursor: pointer;
  color: #606266;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}
.collapse-btn:hover {
  background: #f0f2f5;
  color: #66bb6a;
}
.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.username {
  font-size: 13px;
  color: #909399;
}
.header-avatar {
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.2s;
}
.header-avatar:hover {
  transform: scale(1.05);
}

/* 内容区 */
.main-area {
  padding: 20px;
  background: #f5f7fa;
  overflow-x: hidden;
  min-height: calc(100vh - 60px);
}
</style>
