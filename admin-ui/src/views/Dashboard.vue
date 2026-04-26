<template>
  <div>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-label">路由规则</div>
        <div class="stat-value">{{ stats.routes }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">API Key</div>
        <div class="stat-value" style="color:#67c23a">{{ stats.keys }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">今日请求</div>
        <div class="stat-value" style="color:#e6a23c">{{ stats.todayLogs }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总请求数</div>
        <div class="stat-value" style="color:#909399">{{ stats.totalLogs }}</div>
      </div>
    </div>

    <el-card>
      <template #header><span>快速开始</span></template>
      <el-steps :active="3" simple style="margin-bottom:20px">
        <el-step title="初始化管理员" />
        <el-step title="创建路由规则" />
        <el-step title="使用代理" />
      </el-steps>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="代理地址">
          <code>http://localhost:8080/api/proxy/{路径}</code>
        </el-descriptions-item>
        <el-descriptions-item label="管理接口">
          <code>http://localhost:8080/api/admin/...</code>
        </el-descriptions-item>
        <el-descriptions-item label="认证方式">
          HTTP Basic Auth + X-API-Key 请求头
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRoutes, getApiKeys, getLogs } from '../api/index.js'

const stats = ref({ routes: 0, keys: 0, todayLogs: 0, totalLogs: 0 })

onMounted(async () => {
  try {
    const [routes, keys, logs] = await Promise.all([
      getRoutes(), getApiKeys(), getLogs({ size: 1 })
    ])
    stats.value.routes = routes.data.length
    stats.value.keys = keys.data.length
    stats.value.totalLogs = logs.data.totalElements || 0
  } catch (e) { /* ignore */ }
})
</script>
