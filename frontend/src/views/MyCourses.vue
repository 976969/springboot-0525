<!--
  学生端我的课程（课程列表、任务查看、权重图展示）
-->
<template>
  <el-card>
    <template #header>我的课程</template>
    <el-table :data="courseList" border stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="teacherName" label="授课教师" width="120" />
      <el-table-column prop="createTime" label="选课时间" width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewTasks(row.courseId, row.courseName)">查看任务</el-button>
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

    <!-- 任务列表弹框 -->
    <el-dialog v-model="taskDialogVisible" :title="'课程任务 - ' + currentCourseName" width="750px" top="5vh">
      <el-table :data="taskList" border stripe v-loading="taskLoading" max-height="400">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="任务名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="description" label="任务描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="截止时间" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.deadline">{{ row.deadline }}</span>
            <el-tag v-else type="info" size="small">无截止</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="isExpired(row.deadline)" type="info" size="small">已截止</el-tag>
            <el-tag v-else type="success" size="small">进行中</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="taskList.length === 0 && !taskLoading" description="该课程暂无实训任务" />
      
      <!-- 评分权重图 -->
      <el-divider content-position="left">课程评分标准</el-divider>
      <div v-loading="indicatorLoading" style="min-height: 300px;">
        <div v-if="filteredIndicatorList.length > 0">
          <div ref="indicatorChartRef" style="width: 100%; height: 300px;"></div>
          <el-table :data="filteredIndicatorList" border stripe size="small" style="margin-top: 16px;">
            <el-table-column prop="name" label="指标名称" />
            <el-table-column prop="category" label="分类" width="100" />
            <el-table-column label="权重" width="80" align="center">
              <template #default="{ row }">
                <el-tag size="small">{{ row.defaultWeight }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">
                {{ getIndicatorPercent(row.defaultWeight) }}%
              </template>
            </el-table-column>
            <el-table-column prop="isSystem" label="类型" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.isSystem === 1 ? 'danger' : 'success'" size="small">
                  {{ row.isSystem === 1 ? '系统' : '自定义' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else-if="!indicatorLoading" description="暂无评分标准" />
      </div>
      
      <template #footer>
        <el-button @click="taskDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import request from '../utils/request'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const courseList = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 任务弹框
const taskDialogVisible = ref(false)
const taskList = ref([])
const taskLoading = ref(false)
const currentCourseName = ref('')

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

const viewTasks = async (courseId, courseName) => {
  currentCourseName.value = courseName || ''
  taskDialogVisible.value = true
  taskLoading.value = true
  indicatorLoading.value = true
  try {
    const res = await request.get(`/task/course/${courseId}`)
    taskList.value = res.data || []
  } catch (e) {
    console.error('加载任务列表失败:', e)
    taskList.value = []
  } finally {
    taskLoading.value = false
  }
  
  // 加载评分标准
  try {
    const res = await request.get('/indicator/list')
    const allIndicators = res.data || []
    // 过滤掉权重为0的指标
    indicatorList.value = allIndicators.filter(item => Number(item.defaultWeight) > 0)
    await nextTick()
    renderIndicatorChart()
  } catch (e) {
    console.error('加载指标失败:', e)
    indicatorList.value = []
  } finally {
    indicatorLoading.value = false
  }
}

const isExpired = (deadline) => {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

// 评分标准相关
const indicatorList = ref([])
const indicatorLoading = ref(false)
const indicatorChartRef = ref(null)
let indicatorChartInstance = null

const indicatorTotalWeight = ref(0)

// 过滤后的指标列表（权重>0）
const filteredIndicatorList = computed(() => {
  return indicatorList.value.filter(item => Number(item.defaultWeight) > 0)
})

const getIndicatorPercent = (weight) => {
  if (!indicatorTotalWeight.value || indicatorTotalWeight.value === 0) return '0.0'
  return ((weight / indicatorTotalWeight.value) * 100).toFixed(1)
}

const renderIndicatorChart = () => {
  if (!indicatorChartRef.value) return
  
  // 如果没有有效指标，销毁图表实例
  if (filteredIndicatorList.value.length === 0) {
    if (indicatorChartInstance) {
      indicatorChartInstance.dispose()
      indicatorChartInstance = null
    }
    return
  }
  
  if (indicatorChartInstance) {
    indicatorChartInstance.dispose()
  }
  
  indicatorChartInstance = echarts.init(indicatorChartRef.value)
  
  const data = filteredIndicatorList.value.map(item => ({
    name: item.name,
    value: item.defaultWeight
  }))
  
  indicatorTotalWeight.value = filteredIndicatorList.value.reduce((sum, item) => sum + (Number(item.defaultWeight) || 0), 0)
  
  indicatorChartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle'
    },
    series: [{
      name: '评价指标权重',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 10,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: true
      },
      data: data
    }]
  })
}



onBeforeUnmount(() => {
  indicatorChartInstance?.dispose()
})

onMounted(loadCourses)
</script>
