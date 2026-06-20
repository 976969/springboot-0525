/**
 * Axios请求封装（Token自动携带、409错误透传、统一错误处理）
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000
})

/** 请求拦截器 - 自动携带Token */
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
}, error => {
  return Promise.reject(error)
})

/** 响应拦截器 - 统一处理错误（401跳转登录、409透传业务层） */
request.interceptors.response.use(response => {
  // 如果是blob类型响应(文件下载),直接返回
  if (response.config.responseType === 'blob') {
    return response
  }
  
  const res = response.data
  if (res.code !== 200) {
    // 409冲突错误(如重复提交),不在这里显示错误,交给业务层处理
    if (res.code === 409) {
      return Promise.reject({ 
        isConflict: true, 
        message: res.message, 
        response: { data: res } 
      })
    }
    
    ElMessage.error(res.message || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  }
  return res
}, error => {
  // HTTP 401 也清除登录状态
  if (error.response && error.response.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  } else if (!error.isConflict) {
    // 非409冲突错误才显示错误消息
    ElMessage.error(error.message || '网络异常')
  }
  return Promise.reject(error)
})

export default request
