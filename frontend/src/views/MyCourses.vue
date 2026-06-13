<template>
  <el-card>
    <template #header>我的课程</template>
    <el-table :data="courseList" border stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="teacherName" label="授课教师" width="120" />
      <el-table-column prop="createTime" label="选课时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewTasks(row.courseId)">查看任务</el-button>
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
      @size-change="loadCourses"
      @current-change="loadCourses"
    />
    
    <el-empty v-if="courseList.length === 0 && !loading" description="暂无选课信息" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const courseList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadCourses = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/my-courses/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    courseList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('加载课程失败:', e)
  } finally {
    loading.value = false
  }
}

const viewTasks = (courseId) => {
  router.push({ path: '/upload', query: { courseId } })
}

onMounted(loadCourses)
</script>
