/**
 * Vue Router路由配置（角色权限控制、动态布局、守卫拦截）
 */
import { createRouter, createWebHistory } from 'vue-router'
import axios from 'axios'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  // 管理员专用路由(侧边栏布局)
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/login',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页', roles: ['student'] }
      },
      {
        path: 'admin-home',
        name: 'AdminHome',
        component: () => import('../views/AdminDashboard.vue'),
        meta: { title: '系统总览', roles: ['admin'] }
      },
      {
        path: 'teacher-home',
        name: 'TeacherHome',
        component: () => import('../views/TeacherHome.vue'),
        meta: { title: '首页', roles: ['teacher'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'user-manage',
        name: 'UserManage',
        component: () => import('../views/UserManage.vue'),
        meta: { title: '用户管理', roles: ['admin'] }
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('../views/MyCourses.vue'),
        meta: { title: '我的课程', roles: ['student'] }
      },
      {
        path: 'course-select',
        name: 'CourseSelect',
        component: () => import('../views/CourseSelect.vue'),
        meta: { title: '选课中心', roles: ['student'] }
      },
      {
        path: 'my-schedule',
        name: 'MySchedule',
        component: () => import('../views/MySchedule.vue'),
        meta: { title: '我的课程表', roles: ['student'] }
      },
      {
        path: 'my-scores',
        name: 'MyScores',
        component: () => import('../views/MyScores.vue'),
        meta: { title: '我的成绩', roles: ['student'] }
      },
      {
        path: 'ai-practice',
        name: 'AiPractice',
        component: () => import('../views/AiPractice.vue'),
        meta: { title: 'AI练习', roles: ['student'] }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('../views/Course.vue'),
        meta: { title: '课程管理', roles: ['admin', 'teacher'] }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('../views/Task.vue'),
        meta: { title: '实训任务', roles: ['admin', 'teacher'] }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('../views/Upload.vue'),
        meta: { title: '成果上传', roles: ['student'] }
      },
      {
        path: 'evaluate',
        name: 'Evaluate',
        component: () => import('../views/Evaluate.vue'),
        meta: { title: '评价管理', roles: ['admin', 'teacher'] }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('../views/Report.vue'),
        meta: { title: '报表中心', roles: ['admin', 'teacher'] }
      },
      {
        path: 'indicator',
        name: 'Indicator',
        component: () => import('../views/Indicator.vue'),
        meta: { title: '评价指标', roles: ['admin', 'teacher'] }
      },
      {
        path: 'banner-manage',
        name: 'BannerManage',
        component: () => import('../views/BannerManage.vue'),
        meta: { title: '大屏管理', roles: ['admin'] }
      },
      {
        path: 'schedule-manage',
        name: 'ScheduleManage',
        component: () => import('../views/ScheduleManage.vue'),
        meta: { title: '课程表管理', roles: ['admin'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 未登录或 token 失效时跳转登录页
let tokenChecked = false  // 标记本次会话是否已验证过 token

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title || '智能实训评价系统'
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userRole = userInfo.role
  
  // 访问根路径时的处理
  if (to.path === '/') {
    if (token) {
      // 有token,根据角色跳转到对应首页
      if (userRole === 'teacher') {
        next('/teacher-home')
      } else if (userRole === 'admin') {
        next('/admin-home')
      } else {
        next('/home')
      }
    } else {
      // 没有token,跳转到登录页
      next('/login')
    }
    return
  }
  
  // 没有 token 且不是访问登录页，跳转登录页
  if (!token && to.path !== '/login') {
    tokenChecked = false
    next('/login')
    return
  }
  
  // 已有 token，访问登录页则跳转首页
  if (token && to.path === '/login') {
    if (userRole === 'teacher') {
      next('/teacher-home')
    } else if (userRole === 'admin') {
      next('/admin-home')
    } else {
      next('/home')
    }
    return
  }
  
  // 首次进入页面时，向后端验证 token 是否有效
  if (!tokenChecked && token && to.path !== '/login') {
    try {
      await axios.get('/api/auth/info', {
        headers: { Authorization: token }
      })
      tokenChecked = true  // token 有效，后续不再重复验证
    } catch (e) {
      // token 已失效（后端重启、Session 过期等），清除本地数据
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      tokenChecked = false
      next('/login')
      return
    }
  }
  
  // 角色权限校验
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userRole || !to.meta.roles.includes(userRole)) {
      // 无权限访问,跳转到对应首页(防止循环跳转)
      if (userRole === 'teacher') {
        // 教师不能访问学生页面,跳转到教师首页
        if (to.path !== '/teacher-home') {
          next('/teacher-home')
        } else {
          next() // 已经在教师首页,允许访问
        }
      } else if (userRole === 'student') {
        // 学生不能访问教师页面,跳转到学生首页
        if (to.path !== '/home') {
          next('/home')
        } else {
          next() // 已经在学生首页,允许访问
        }
      } else if (userRole === 'admin') {
        // 管理员跳转到系统总览
        if (to.path !== '/admin-home') {
          next('/admin-home')
        } else {
          next()
        }
      } else {
        // 未知角色,跳转到登录页
        next('/login')
      }
      return
    }
  }
  
  next()
})

export default router
