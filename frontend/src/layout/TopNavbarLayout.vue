<!--
  教师/学生顶部导航栏布局（角色适配、响应式设计）
-->
<template>
  <el-container style="height: 100vh; background-color: #f5f7fa">
    <!-- 顶部导航栏 -->
    <el-header class="top-navbar">
      <div class="navbar-container">
        <!-- 左侧Logo -->
        <div class="navbar-logo">
          <el-icon :size="28" color="#66bb6a"><School /></el-icon>
          <span class="logo-text">智能实训评价系统</span>
        </div>

        <!-- 中间导航菜单 -->
        <div class="navbar-menu">
          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            background-color="transparent"
            text-color="#606266"
            active-text-color="#409EFF"
            router
            :ellipsis="false"
          >
            <!-- 学生首页 -->
            <el-menu-item v-if="role === 'student'" index="/home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <!-- 教师首页 -->
            <el-menu-item v-if="role === 'teacher'" index="/teacher-home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>

            <!-- 教师功能 -->
            <el-menu-item v-if="role === 'teacher'" index="/course">
              <el-icon><Reading /></el-icon>
              <span>课程管理</span>
            </el-menu-item>
            <el-menu-item v-if="role === 'teacher'" index="/task">
              <el-icon><List /></el-icon>
              <span>实训任务</span>
            </el-menu-item>
            <el-menu-item v-if="role === 'teacher'" index="/check">
              <el-icon><CircleCheck /></el-icon>
              <span>智能核查</span>
            </el-menu-item>
            <el-menu-item v-if="role === 'teacher'" index="/evaluate">
              <el-icon><Star /></el-icon>
              <span>评价管理</span>
            </el-menu-item>
            <el-menu-item v-if="role === 'teacher'" index="/indicator">
              <el-icon><SetUp /></el-icon>
              <span>评价指标</span>
            </el-menu-item>
            <el-menu-item v-if="role === 'teacher'" index="/report">
              <el-icon><DataAnalysis /></el-icon>
              <span>报表中心</span>
            </el-menu-item>

            <!-- 公共功能 -->
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>

            <!-- 学生专属功能 -->
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
        </div>

        <!-- 右侧用户信息 -->
        <div class="navbar-user">
          <el-tag :type="roleTagType" size="small" effect="plain">{{ roleLabel }}</el-tag>
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo.avatar || ''" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo.realName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

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
.top-navbar {
  height: 70px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0;
  z-index: 1000;
}

.navbar-container {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 30px;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  min-width: 200px;
}

.logo-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.navbar-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.navbar-menu :deep(.el-menu) {
  border: none;
  height: 70px;
}

.navbar-menu :deep(.el-menu-item) {
  height: 70px;
  line-height: 70px;
  font-size: 15px;
  font-weight: 500;
  padding: 0 20px;
  transition: all 0.3s;
}

.navbar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(102, 187, 106, 0.06);
}

.navbar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(to bottom, transparent 0%, transparent 66px, #66bb6a 66px, #66bb6a 70px);
  font-weight: bold;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 15px;
  min-width: 200px;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .navbar-menu :deep(.el-menu-item) {
    padding: 0 15px;
    font-size: 14px;
  }
  
  .navbar-logo {
    min-width: 180px;
    font-size: 18px;
  }
}

@media (max-width: 768px) {
  .navbar-container {
    padding: 0 15px;
  }
  
  .navbar-logo {
    min-width: auto;
  }
  
  .logo-text {
    font-size: 16px;
  }
  
  .navbar-menu :deep(.el-menu-item) {
    padding: 0 10px;
    font-size: 13px;
  }
  
  .username {
    display: none;
  }
}
</style>
