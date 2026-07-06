<!--
  评价指标管理页面
  管理员：分区展示（系统标准指标 + 教师自定义指标查看）
  教师：指标CRUD、AI生成、权重重置、配比展示
-->
<template>
  <!-- ==================== 管理员视图 ==================== -->
  <div v-if="userRole === 'admin'">
    <!-- 区域一：系统标准指标 -->
    <el-card style="margin-bottom: 20px;">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>
            <span style="font-weight: bold; font-size: 16px;">系统标准指标</span>
            <el-tag type="danger" size="small" style="margin-left: 8px;">全局</el-tag>
          </div>
          <el-button type="primary" @click="openAddSystem">新增系统指标</el-button>
        </div>
      </template>
      <p style="margin: 0 0 15px; color: #909399; font-size: 13px;">
        系统标准指标为全局模板，教师首次登录时自动复制。管理员可修改系统指标，修改后不影响教师已有的副本。
      </p>
      <el-table :data="systemIndicators" border stripe v-loading="systemLoading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="指标名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="权重" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.defaultWeight }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEditSystem(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDeleteSystem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 区域二：教师自定义指标 -->
    <el-card>
      <template #header>
        <div>
          <span style="font-weight: bold; font-size: 16px;">教师自定义指标</span>
          <el-tag type="success" size="small" style="margin-left: 8px;">查看</el-tag>
        </div>
      </template>
      <p style="margin: 0 0 15px; color: #909399; font-size: 13px;">
        查看各教师自行添加的自定义评价指标。
      </p>
      <div style="margin-bottom: 15px; display: flex; gap: 15px; align-items: center">
        <el-select 
          v-model="selectedTeacher" 
          placeholder="选择教师查看其自定义指标" 
          clearable 
          style="width: 200px"
          @change="loadTeacherCustomIndicators"
        >
          <el-option 
            v-for="teacher in teacherList" 
            :key="teacher.id" 
            :label="teacher.realName" 
            :value="teacher.id" 
          />
        </el-select>
        <el-tag v-if="selectedTeacher && teacherCustomIndicators.length > 0" type="info">
          共 {{ teacherCustomIndicators.length }} 个自定义指标
        </el-tag>
      </div>

      <div v-if="!selectedTeacher" style="text-align: center; padding: 40px; color: #909399;">
        <el-icon style="font-size: 48px; margin-bottom: 10px;"><User /></el-icon>
        <p>请选择教师查看其自定义指标</p>
      </div>

      <el-table v-else :data="teacherCustomIndicators" border stripe v-loading="teacherLoading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="指标名称" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="权重" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.defaultWeight }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>

  <!-- ==================== 教师视图 ==================== -->
  <el-card v-else>
    <template #header>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <span>评价指标管理</span>
        <div style="display: flex; gap: 10px;">
          <el-button type="warning" @click="handleResetWeights">重置权重</el-button>
          <el-button type="primary" @click="openAdd">新增指标</el-button>
        </div>
      </div>
    </template>
    
    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column type="index" label="序号" width="60" align="center" :index="(i) => (pageNum - 1) * pageSize + i + 1" />
      <el-table-column prop="name" label="指标名称" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="权重" width="100" align="center">
        <template #default="{ row }">
          <el-tag size="small">{{ row.defaultWeight }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实际占比" width="200" align="center">
        <template #default="{ row }">
          <div style="display: flex; align-items: center; gap: 6px; width: 100%;">
            <el-progress 
              :percentage="getWeightPercent(row.defaultWeight)" 
              :stroke-width="16" 
              :color="getWeightColor(row.defaultWeight)"
              style="flex: 1;"
            />
            <span style="font-size: 13px; font-weight: bold; color: #303133; min-width: 40px; text-align: right;">
              {{ getWeightPercent(row.defaultWeight) }}%
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="isSystem" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isSystem === 1 ? 'danger' : 'success'">
            {{ row.isSystem === 1 ? '系统' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)" :disabled="row.isSystem === 1">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :page-sizes="[5, 10, 20]"
      :total="total"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end"
      @size-change="loadIndicators"
      @current-change="loadIndicators"
    />
    
    <div style="margin-top: 20px; padding: 12px 16px; background: #f5f7fa; border-radius: 6px; display: flex; align-items: center; gap: 12px;">
      <el-tag type="info" effect="dark">
        权重总和: {{ totalWeight }}
      </el-tag>
      <span style="color: #606266; font-size: 13px;">
        实际占比 = 各指标权重 / {{ totalWeight }}，系统自动归一化计算总分
      </span>
    </div>

    <!-- AI智能生成 -->
    <el-card style="margin-top: 20px;" shadow="never">
      <template #header>
        <div style="display: flex; align-items: center; gap: 8px;">
          <span style="font-weight: bold;">AI 智能生成指标</span>
          <el-tag type="danger" size="small" effect="plain">AI</el-tag>
        </div>
      </template>
      <p style="margin: 0 0 12px; color: #909399; font-size: 13px;">
        描述您的评分需求，AI 将自动调整指标权重并新增合适的评价维度。系统指标会保留，仅调整权重。
      </p>
      <el-input
        v-model="aiRequirements"
        type="textarea"
        :rows="4"
        placeholder="例如：这门课侧重实践操作，代码质量占大头，文档简单即可；另外需要增加一个团队协作的指标，PPT展示也要考核"
        style="margin-bottom: 12px;"
      />
      <div style="display: flex; align-items: center; gap: 10px;">
        <el-button type="primary" @click="handleAiGenerate" :loading="aiLoading">
          {{ aiLoading ? 'AI 生成中...' : 'AI 生成指标配置' }}
        </el-button>
        <span v-if="aiLoading" style="color: #e6a23c; font-size: 13px;">AI 正在分析，请稍候（约10-30秒）...</span>
      </div>

      <div v-if="aiResult" style="margin-top: 16px; padding: 14px; background: #f0f9eb; border-radius: 6px; border: 1px solid #e1f3d8;">
        <div style="font-weight: bold; margin-bottom: 8px; color: #67c23a;">AI 建议</div>
        <p style="margin: 0 0 10px; color: #606266; font-size: 14px;">{{ aiResult.suggestion }}</p>
        <div style="font-weight: bold; margin-bottom: 8px; color: #409eff;">操作记录</div>
        <ul style="margin: 0; padding-left: 20px; color: #606266; font-size: 13px;">
          <li v-for="(action, idx) in aiResult.actions" :key="idx">{{ action }}</li>
        </ul>
      </div>
    </el-card>

    <!-- 权重配比饼图 -->
    <div style="margin-top: 20px;">
      <div style="font-size: 15px; font-weight: bold; margin-bottom: 12px; color: #303133;">权重配比图</div>
      <div ref="chartRef" style="width: 100%; height: 360px;"></div>
    </div>
  </el-card>

  <!-- ==================== 通用对话框 ==================== -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
    <el-form :model="form" label-width="80px">
      <el-form-item label="指标名称" required>
        <el-input v-model="form.name" placeholder="如：代码质量" :disabled="isEditSystemForTeacher" />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-input v-model="form.category" placeholder="如：技术能力" :disabled="isEditSystemForTeacher" />
      </el-form-item>
      <el-form-item label="权重" required>
        <el-input-number v-model="form.defaultWeight" :min="0" :max="100" />
      </el-form-item>
      <el-form-item v-if="userRole === 'admin' && !isEdit" label="类型" required>
        <el-select v-model="form.isSystem" placeholder="选择类型">
          <el-option label="系统指标" :value="1" />
          <el-option label="自定义" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="指标说明" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'

const userRole = ref('')
const teacherList = ref([])

// 管理员相关
const systemIndicators = ref([])
const systemLoading = ref(false)
const selectedTeacher = ref('')
const teacherCustomIndicators = ref([])
const teacherLoading = ref(false)

// 教师相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const isEditSystemForTeacher = ref(false)
const tableData = ref([])
const loading = ref(false)
const submitting = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const chartRef = ref(null)
let chartInstance = null

// AI 相关
const aiRequirements = ref('')
const aiLoading = ref(false)
const aiResult = ref(null)

// 计算权重配比
const totalWeight = computed(() => {
  return tableData.value.reduce((sum, item) => sum + (Number(item.defaultWeight) || 0), 0)
})

const getWeightPercent = (weight) => {
  const tw = totalWeight.value
  if (!tw || tw === 0) return 0
  return Math.round((Number(weight) / tw) * 100)
}

const getWeightColor = (weight) => {
  const percent = getWeightPercent(weight)
  if (percent >= 30) return '#409eff'
  if (percent >= 20) return '#67c23a'
  if (percent >= 10) return '#e6a23c'
  return '#909399'
}

// 饼图渲染
const renderChart = () => {
  if (!chartRef.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)
  
  const data = tableData.value.map(item => ({
    name: item.name,
    value: Number(item.defaultWeight) || 0
  }))
  
  chartInstance.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const pct = ((params.value / totalWeight.value) * 100).toFixed(1)
        return `${params.name}<br/>权重: ${params.value} | 占比: ${pct}%`
      }
    },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemGap: 12 },
    color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c', '#3498db'],
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: {
        show: true,
        formatter: (params) => {
          const pct = ((params.value / totalWeight.value) * 100).toFixed(1)
          return `${params.name}\n${pct}%`
        },
        fontSize: 13,
        lineHeight: 18
      },
      emphasis: {
        label: { show: true, fontSize: 15, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.3)' }
      },
      data: data
    }]
  })
}

window.addEventListener('resize', () => chartInstance?.resize())
onBeforeUnmount(() => {
  window.removeEventListener('resize', () => chartInstance?.resize())
  chartInstance?.dispose()
})

const defaultForm = {
  id: null,
  name: '',
  category: '',
  defaultWeight: 20,
  isSystem: 0,
  description: ''
}
const form = reactive({ ...defaultForm })

const dialogTitle = computed(() => {
  if (userRole.value === 'admin') {
    return isEdit.value ? '编辑系统指标' : '新增系统指标'
  }
  return isEdit.value ? '编辑评价指标' : '新增评价指标'
})

// 加载教师列表
const loadTeachers = async () => {
  try {
    const res = await request.get('/user/teacher/list')
    teacherList.value = res.data || []
  } catch (e) {
    console.error('加载教师列表失败:', e)
  }
}

// ========== 管理员方法 ==========

const loadSystemIndicators = async () => {
  systemLoading.value = true
  try {
    const res = await request.get('/indicator/system')
    systemIndicators.value = res.data || []
  } catch (e) {
    console.error('加载系统指标失败:', e)
  } finally {
    systemLoading.value = false
  }
}

const loadTeacherCustomIndicators = async () => {
  if (!selectedTeacher.value) {
    teacherCustomIndicators.value = []
    return
  }
  teacherLoading.value = true
  try {
    const res = await request.get(`/indicator/by-teacher/${selectedTeacher.value}`)
    teacherCustomIndicators.value = res.data || []
  } catch (e) {
    console.error('加载教师自定义指标失败:', e)
  } finally {
    teacherLoading.value = false
  }
}

const openAddSystem = () => {
  isEdit.value = false
  Object.assign(form, { ...defaultForm, isSystem: 1 })
  dialogVisible.value = true
}

const openEditSystem = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDeleteSystem = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该系统指标？删除后新教师登录将不再自动获得此指标。', '提示', { type: 'warning' })
    await request.delete(`/indicator/${id}`)
    ElMessage.success('删除成功')
    loadSystemIndicators()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
  }
}

// ========== 教师方法 ==========

const loadIndicators = async () => {
  loading.value = true
  try {
    const res = await request.get('/indicator/page', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value
      }
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
    await nextTick()
    renderChart()
  } catch (e) {
    console.error('加载评价指标失败:', e)
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  isEdit.value = false
  isEditSystemForTeacher.value = false
  Object.assign(form, { ...defaultForm })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  isEditSystemForTeacher.value = row.isSystem === 1
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.name || !form.category) {
    ElMessage.warning('请填写必填项')
    return
  }
  
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put('/indicator', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/indicator', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    if (userRole.value === 'admin') {
      loadSystemIndicators()
    } else {
      loadIndicators()
    }
  } catch (e) {
    console.error('操作失败:', e)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const handleResetWeights = async () => {
  try {
    await ElMessageBox.confirm(
      '重置后：系统指标恢复原始权重，自定义指标权重归零。是否继续？',
      '重置权重确认',
      { type: 'warning' }
    )
    await request.post('/indicator/reset')
    ElMessage.success('权重已重置：系统指标恢复原始值，自定义指标已归零')
    loadIndicators()
  } catch (e) {
    if (e !== 'cancel') console.error('重置失败:', e)
  }
}

const handleAiGenerate = async () => {
  if (!aiRequirements.value.trim()) {
    ElMessage.warning('请输入您的评分需求')
    return
  }
  aiLoading.value = true
  aiResult.value = null
  try {
    const res = await request.post('/indicator/ai-generate', {
      requirements: aiRequirements.value
    }, {
      timeout: 180000
    })
    aiResult.value = res.data
    ElMessage.success('AI 指标配置生成完成')
    loadIndicators()
  } catch (e) {
    console.error('AI生成失败:', e)
    ElMessage.error('AI生成失败，请稍后重试')
  } finally {
    aiLoading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该指标？', '提示', { type: 'warning' })
    await request.delete(`/indicator/${id}`)
    ElMessage.success('删除成功')
    loadIndicators()
  } catch (e) {
    if (e !== 'cancel') console.error('删除失败:', e)
  }
}

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  userRole.value = userInfo.role
  if (userRole.value === 'admin') {
    loadTeachers()
    loadSystemIndicators()
  } else {
    loadIndicators()
  }
})
</script>
