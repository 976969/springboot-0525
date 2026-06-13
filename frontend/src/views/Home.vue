<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="课程总数" :value="stats.courseCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="实训任务" :value="stats.taskCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="成果提交" :value="stats.resultCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="已完成评价" :value="stats.evalCount" />
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px">
      <template #header>欢迎使用智能实训评价系统</template>
      <p>本系统基于大模型技术，为软件实训教学提供智能化检查评价服务。</p>
      <el-divider />
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="never">
            <h4>智能核查</h4>
            <p>AI自动核查实训步骤完整性与逻辑漏洞</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <h4>多维评价</h4>
            <p>自定义指标与权重，AI评分+教师评分</p>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never">
            <h4>报表导出</h4>
            <p>生成可视化评价报告，支持Excel/PDF导出</p>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import request from '../utils/request'

const stats = reactive({
  courseCount: 0,
  taskCount: 0,
  resultCount: 0,
  evalCount: 0
})

// 加载统计数据
const loadStats = async () => {
  try {
    const [courses, tasks, results, reports] = await Promise.all([
      request.get('/course/list'),
      request.get('/task/list'),
      request.get('/result/list'),
      request.get('/report/list')
    ])
    
    stats.courseCount = courses.data?.length || 0
    stats.taskCount = tasks.data?.length || 0
    stats.resultCount = results.data?.length || 0
    stats.evalCount = reports.data?.length || 0
  } catch (e) {
    console.error('加载统计数据失败:', e)
  }
}

onMounted(loadStats)
</script>
