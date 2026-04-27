<template>
  <div>
    <div class="page-header">
      <h2>请求日志</h2>
      <el-button @click="fetchLogs()" :icon="Refresh">刷新</el-button>
      <el-button type="danger" @click="handleDeleteAll" :loading="deleting">
        <el-icon><Delete /></el-icon> 清空日志
      </el-button>
    </div>

    <el-card style="margin-bottom:16px">
      <el-form :model="filters" inline>
        <el-form-item label="用户名">
          <el-input v-model="filters.username" placeholder="模糊搜索用户名" clearable style="width:150px" />
        </el-form-item>
        <el-form-item label="路由ID">
          <el-input-number v-model="filters.routeId" :min="0" :step="1" placeholder="不限" clearable />
        </el-form-item>
        <el-form-item label="状态码">
          <el-select v-model="filters.statusCode" clearable placeholder="不限" style="width:120px">
            <el-option label="2xx" :value="200" />
            <el-option label="3xx" :value="300" />
            <el-option label="4xx" :value="400" />
            <el-option label="5xx" :value="500" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table :data="logs" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户" width="120">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <span v-else-if="row.apiKeyId" style="color:#909399">API Key</span>
          <span v-else style="color:#c0c0c0">未认证</span>
        </template>
      </el-table-column>
      <el-table-column prop="requestMethod" label="方法" width="70">
        <template #default="{ row }">
          <el-tag size="small">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestPath" label="请求路径" min-width="300">
        <template #default="{ row }">
          <code>{{ row.requestPath }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="responseStatus" label="状态" width="70">
        <template #default="{ row }">
          <el-tag
            :type="row.responseStatus < 300 ? 'success' : row.responseStatus < 400 ? 'warning' : 'danger'"
            size="small"
          >
            {{ row.responseStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="responseTimeMs" label="耗时(ms)" width="100">
        <template #default="{ row }">
          <span :style="{ color: row.responseTimeMs > 3000 ? '#f56c6c' : '#67c23a' }">
            {{ row.responseTimeMs }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="clientIp" label="客户端IP" width="130" />
      <el-table-column prop="routeId" label="路由ID" width="70" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button text type="danger" size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display:flex;justify-content:center;margin-top:16px">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="fetchLogs"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLogs, deleteLog, deleteLogs } from '../api/index.js'

const logs = ref([])
const loading = ref(false)
const deleting = ref(false)
const page = ref(0)
const size = ref(20)
const total = ref(0)

const filters = ref({ routeId: null, statusCode: null, username: '' })

onMounted(() => fetchLogs())

async function fetchLogs() {
  loading.value = true
  try {
    const params = {
      page: Math.max(0, page.value - 1),
      size: size.value
    }
    if (filters.value.routeId) params.routeId = filters.value.routeId
    if (filters.value.statusCode) params.statusCode = filters.value.statusCode
    if (filters.value.username) params.username = filters.value.username

    const res = await getLogs(params)
    logs.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (e) { /* ignore */ }
  loading.value = false
}

function handleFilter() {
  page.value = 0
  fetchLogs()
}

function handleReset() {
  filters.value = { routeId: null, statusCode: null, username: '' }
  page.value = 0
  fetchLogs()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除这条日志？`, '确认')
    await deleteLog(row.id)
    ElMessage.success('删除成功')
    fetchLogs()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleDeleteAll() {
  try {
    await ElMessageBox.confirm(`确定清空所有日志？这操作不可恢复。`, '警告', { type: 'warning' })
    deleting.value = true
    await deleteLogs()
    ElMessage.success('清空成功')
    fetchLogs()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清空失败')
  }
  deleting.value = false
}
</script>