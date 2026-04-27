<template>
  <!-- 用户登录表单 -->
  <div class="login-page" v-if="!isLoggedIn">
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    <div class="login-container">
      <div class="login-header">
        <div class="logo">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>用户登录</h1>
        <p>欢迎回来，请登录您的账号</p>
      </div>
      <el-card class="login-card" shadow="hover">
        <el-form :model="loginForm" @submit.prevent="handleLogin">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" class="login-btn">
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div v-if="loginError" class="login-error">
          <el-icon><Warning /></el-icon>
          {{ loginError }}
        </div>
      </el-card>
    </div>
  </div>

  <!-- 用户仪表板 -->
  <div class="dashboard" v-else>
    <div class="dashboard-container">
      <header class="dashboard-header">
        <div class="header-left">
          <div class="logo-sm">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h2>用户面板</h2>
        </div>
        <div class="header-right">
          <span class="welcome-text">欢迎，{{ userInfo.username }}</span>
          <el-button type="danger" plain @click="logout" :icon="SwitchButton">退出登录</el-button>
        </div>
      </header>

      <div class="stats-grid">
        <div class="stat-card stat-card-1">
          <div class="stat-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">用户名</div>
            <div class="stat-value">{{ userInfo.username }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-2">
          <div class="stat-icon">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已分配次数</div>
            <div class="stat-value">{{ userInfo.requestCount }}</div>
          </div>
        </div>
        <div class="stat-card stat-card-3">
          <div class="stat-icon">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">剩余次数</div>
            <div class="stat-value" :class="{ 'low': userInfo.requestsRemaining <= 0 }">
              {{ userInfo.requestsRemaining }}
            </div>
          </div>
        </div>
        <div class="stat-card stat-card-4">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">账号过期剩余</div>
            <div class="stat-value" :class="{ 'expired': expireCountdown === '已过期' }">{{ expireCountdown }}</div>
          </div>
        </div>
      </div>

      <el-card class="token-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>
              <el-icon><Key /></el-icon>
              认证信息
            </span>
          </div>
        </template>
        <div class="token-content">
          <p class="token-tip">
            在调用 API 时，通过 <code>X-User-Token</code> 请求头传递此 Token 进行认证
          </p>
          <div class="token-display">
            <el-input :model-value="userInfo.userToken" readonly class="token-input">
              <template #append>
                <el-button @click="copyToken" :icon="DocumentCopy">复制</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </el-card>

      <el-card class="routes-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>
              <el-icon><Memo /></el-icon>
              可用的 API 端点
            </span>
            <el-button size="small" @click="fetchRoutes" :icon="Refresh">刷新</el-button>
          </div>
        </template>
        <el-table :data="routes" stripe v-loading="routesLoading" class="routes-table">
          <el-table-column prop="name" label="名称" width="150">
            <template #default="{ row }">
              <div class="route-name">
                <el-icon><Grid /></el-icon>
                {{ row.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="pathPattern" label="请求路径" min-width="280">
            <template #default="{ row }">
              <code class="path-code">{{ row.pathPattern.replace('**', 'v1/chat/completions') }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="allowedModels" label="支持的模型" min-width="180">
            <template #default="{ row }">
              <el-tag v-if="row.allowedModels" size="small" type="info">{{ row.allowedModels }}</el-tag>
              <el-tag v-else size="small" type="success">全部模型</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" plain @click="copyApiExample(row)">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!routesLoading && routes.length === 0" description="暂无可用的 API 端点" />
      </el-card>

      <!-- API 调用示例弹窗 -->
      <el-dialog v-model="exampleDialogVisible" title="API 调用示例" width="700px">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="端点名称">
            <el-tag type="primary">{{ currentRoute?.name }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="请求地址">
            <code class="inline-code">{{ apiBaseUrl }}{{ currentRoute?.pathPattern?.replace('**', 'v1/chat/completions') }}</code>
            <el-button size="small" @click="copyText(apiBaseUrl + currentRoute?.pathPattern?.replace('**', 'v1/chat/completions'))">复制</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="请求头">
            <code class="inline-code">Content-Type: application/json</code>
            <br/>
            <code class="inline-code">X-User-Token: {{ userInfo.userToken }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="请求体">
            <div class="code-block">{{ getExampleBody(currentRoute) }}</div>
            <el-button size="small" @click="copyText(getExampleBody(currentRoute))">复制请求体</el-button>
          </el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, Lock, Warning, Key, Coin, CircleCheck, Timer,
  DocumentCopy, Refresh, SwitchButton, Grid, Memo
} from '@element-plus/icons-vue'
import { userLogin as userLoginApi, getPublicRoutes, userMe } from '../api/index.js'

const router = useRouter()
const isLoggedIn = ref(false)
const loading = ref(false)
const loginError = ref('')
const routes = ref([])
const routesLoading = ref(false)
const exampleDialogVisible = ref(false)
const currentRoute = ref(null)
let refreshTimer = null
let idleTimer = null
const IDLE_TIMEOUT = 3600000

const loginForm = ref({ username: '', password: '' })

const userInfo = ref({
  username: '',
  role: '',
  userToken: '',
  requestCount: 0,
  requestsUsed: 0,
  requestsRemaining: 0,
  expireAt: null
})

const expireCountdown = ref('--')
let countdownTimer = null

const apiBaseUrl = computed(() => {
  return window.location.origin
})

function updateExpireCountdown() {
  if (!userInfo.value.expireAt) {
    expireCountdown.value = '永久'
    return
  }
  const expireTime = new Date(userInfo.value.expireAt).getTime()
  const now = Date.now()
  const diff = expireTime - now
  if (diff <= 0) {
    expireCountdown.value = '已过期'
    return
  }
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  if (days > 0) {
    expireCountdown.value = `${days}天${hours}小时`
  } else if (hours > 0) {
    expireCountdown.value = `${hours}小时${minutes}分钟`
  } else {
    expireCountdown.value = `${minutes}分钟`
  }
}

onMounted(() => {
  const raw = localStorage.getItem('user_auth')
  if (raw) {
    try {
      userInfo.value = JSON.parse(raw)
      isLoggedIn.value = true
      fetchRoutes()
      startRefreshTimer()
      startIdleTimer()
      startCountdownTimer()
    } catch {
      logout()
    }
  }
})

onUnmounted(() => {
  stopRefreshTimer()
  stopIdleTimer()
  stopCountdownTimer()
})

function startRefreshTimer() {
  stopRefreshTimer()
  refreshTimer = setInterval(() => {
    if (isLoggedIn.value) {
      fetchUserInfo()
    }
  }, 5000)
}

function stopRefreshTimer() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

function startIdleTimer() {
  stopIdleTimer()
  resetIdleTimer()
  document.addEventListener('mousemove', resetIdleTimer)
  document.addEventListener('keydown', resetIdleTimer)
  document.addEventListener('click', resetIdleTimer)
  document.addEventListener('scroll', resetIdleTimer)
}

function stopIdleTimer() {
  if (idleTimer) {
    clearTimeout(idleTimer)
    idleTimer = null
  }
  document.removeEventListener('mousemove', resetIdleTimer)
  document.removeEventListener('keydown', resetIdleTimer)
  document.removeEventListener('click', resetIdleTimer)
  document.removeEventListener('scroll', resetIdleTimer)
}

function resetIdleTimer() {
  if (idleTimer) {
    clearTimeout(idleTimer)
  }
  idleTimer = setTimeout(() => {
    if (isLoggedIn.value) {
      ElMessage.warning('登录已过期，请重新登录')
      logout()
    }
  }, IDLE_TIMEOUT)
}

function startCountdownTimer() {
  stopCountdownTimer()
  updateExpireCountdown()
  countdownTimer = setInterval(updateExpireCountdown, 60000)
}

function stopCountdownTimer() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

async function fetchUserInfo() {
  try {
    const res = await userMe()
    userInfo.value = {
      ...userInfo.value,
      requestCount: res.data.requestCount,
      requestsUsed: res.data.requestsUsed,
      requestsRemaining: res.data.requestsRemaining,
      expireAt: res.data.expireAt
    }
    localStorage.setItem('user_auth', JSON.stringify(userInfo.value))
    updateExpireCountdown()
  } catch (e) {
    // ignore
  }
}

async function fetchRoutes() {
  routesLoading.value = true
  try {
    const res = await getPublicRoutes()
    routes.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch routes:', e)
  }
  routesLoading.value = false
}

async function handleLogin() {
  loginError.value = ''
  if (!loginForm.value.username || !loginForm.value.password) {
    loginError.value = '用户名和密码不能为空'
    return
  }

  loading.value = true
  try {
    const res = await userLoginApi({
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    localStorage.setItem('user_auth', JSON.stringify(res.data))
    localStorage.removeItem('admin_auth')
    userInfo.value = res.data
    isLoggedIn.value = true
    ElMessage.success('登录成功')
    fetchRoutes()
  } catch (e) {
    if (e.response?.status === 401) {
      loginError.value = '用户名或密码错误'
    } else {
      loginError.value = e.response?.data?.error || '登录失败'
    }
  }
  loading.value = false
}

function copyToken() {
  navigator.clipboard.writeText(userInfo.value.userToken)
    .then(() => ElMessage.success('已复制'))
}

function copyText(text) {
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('已复制'))
}

function copyApiExample(route) {
  currentRoute.value = route
  exampleDialogVisible.value = true
}

function getExampleBody(route) {
  const model = route?.allowedModels?.split(',')[0]?.trim() || 'MiniMax-M2.7'
  return `{
  "model": "${model}",
  "messages": [
    {
      "role": "user",
      "content": "你好"
    }
  ]
}`
}

function logout() {
  stopIdleTimer()
  localStorage.removeItem('user_auth')
  isLoggedIn.value = false
  loginForm.value = { username: '', password: '' }
  router.push('/login')
}
</script>

<style scoped>
/* ========== 登录页面样式 ========== */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 10%;
  background: linear-gradient(135deg, rgba(118, 75, 162, 0.08) 0%, rgba(102, 126, 234, 0.05) 100%);
}

.login-container {
  position: relative;
  z-index: 1;
  width: 400px;
  padding: 20px;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.logo svg {
  width: 32px;
  height: 32px;
  color: #fff;
}

.login-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 600;
  color: #1a1a2e;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.login-card {
  border: none;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.login-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 6px;
}

/* ========== 仪表板样式 ========== */
.dashboard {
  min-height: 100vh;
  background: #f5f7fa;
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-sm {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-sm svg {
  width: 22px;
  height: 22px;
  color: #fff;
}

.dashboard-header h2 {
  margin: 0;
  color: #1a1a2e;
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-text {
  color: #6b7280;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.stat-card-1 .stat-icon { background: rgba(102, 126, 234, 0.1); color: #667eea; }
.stat-card-2 .stat-icon { background: rgba(118, 75, 162, 0.1); color: #764ba2; }
.stat-card-3 .stat-icon { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.stat-card-4 .stat-icon { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }

.stat-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
}

.stat-value.low {
  color: #ef4444;
}

.stat-value.expired {
  color: #ef4444;
  font-size: 18px;
}

.stat-value.secondary {
  color: #6b7280;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1a1a2e;
}

.token-card,
.routes-card {
  border-radius: 16px;
  background: #fff;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.token-card :deep(.el-card__header),
.routes-card :deep(.el-card__header) {
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px;
}

.token-tip {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 16px;
}

.token-tip code {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  color: #667eea;
  font-family: 'Courier New', monospace;
}

.path-code {
  background: #f5f5f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: #374151;
}

.inline-code {
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  color: #667eea;
}

.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 8px 0;
}

.route-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #667eea;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .header-right {
    flex-direction: column;
  }
}
</style>
