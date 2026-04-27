<template>
  <div>
    <div class="page-header">
      <h2>控制台</h2>
      <el-button type="primary" @click="passwordVisible = true">修改密码</el-button>
    </div>

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

    <!-- Change password dialog -->
    <el-dialog v-model="passwordVisible" title="修改管理员密码" width="400px">
      <el-form :model="passwordForm" label-width="100px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword" :loading="changing">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoutes, getApiKeys, getLogs, changePassword } from '../api/index.js'

const stats = ref({ routes: 0, keys: 0, todayLogs: 0, totalLogs: 0 })
const passwordVisible = ref(false)
const changing = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

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

async function handleChangePassword() {
  if (!passwordForm.value.oldPassword) {
    ElMessage.error('请输入旧密码')
    return
  }
  if (!passwordForm.value.newPassword) {
    ElMessage.error('请输入新密码')
    return
  }
  if (passwordForm.value.newPassword.length < 3) {
    ElMessage.error('新密码长度不能少于3位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }

  changing.value = true
  try {
    await changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword)
    ElMessage.success('密码修改成功')
    passwordVisible.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    ElMessage.error(e.response?.data?.error || '密码修改失败')
  }
  changing.value = false
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
