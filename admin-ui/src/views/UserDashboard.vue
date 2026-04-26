<template>
  <!-- 用户登录表单 -->
  <div class="user-page" v-if="!isLoggedIn">
    <el-card class="login-card">
      <template #header>
        <h2>用户登录</h2>
      </template>
      <el-form :model="loginForm" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width:100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="loginError" class="login-error">{{ loginError }}</div>
      <div style="text-align:center;margin-top:12px">
        还没有账号？
        <router-link to="/register" style="color:#409eff">立即注册</router-link>
      </div>
    </el-card>
  </div>

  <!-- 用户仪表板 -->
  <div class="user-page" v-else>
    <div class="page-header">
      <h2>用户面板</h2>
      <el-button @click="logout">退出登录</el-button>
    </div>

    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="8">
        <el-card>
          <template #header>用户名</template>
          <span style="font-size:20px;font-weight:bold">{{ userInfo.username }}</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>已分配 Token</template>
          <span style="font-size:20px;font-weight:bold;color:#409eff">{{ userInfo.tokenBalance }}</span>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>剩余 Token</template>
          <span style="font-size:20px;font-weight:bold;color:#67c23a">{{ userInfo.tokenRemaining }}</span>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-bottom:16px">
      <template #header>认证信息</template>
      <p style="font-size:13px;color:#999;margin-bottom:12px">
        在调用 API 时，通过 <code>X-User-Token</code> 请求头传递此 Token 进行认证。
      </p>
      <el-input :model-value="userInfo.userToken" readonly>
        <template #append>
          <el-button @click="copyToken">复制</el-button>
        </template>
      </el-input>
    </el-card>

    <el-card style="margin-bottom:16px">
      <template #header>
        <span>可用的 API 端点</span>
        <el-button size="small" @click="fetchRoutes" style="float:right">刷新</el-button>
      </template>
      <el-table :data="routes" stripe size="small" v-loading="routesLoading">
        <el-table-column prop="name" label="名称" width="150" />
        <el-table-column prop="pathPattern" label="请求路径" min-width="250">
          <template #default="{ row }">
            <code>{{ row.pathPattern.replace('**', 'v1/chat/completions') }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="allowedModels" label="支持的模型" min-width="200">
          <template #default="{ row }">
            <span v-if="row.allowedModels" style="font-size:12px">{{ row.allowedModels }}</span>
            <span v-else style="color:#909399">全部模型</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="copyApiExample(row)">复制示例</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!routesLoading && routes.length === 0" description="暂无可用的 API 端点" />
    </el-card>

    <!-- API 调用示例弹窗 -->
    <el-dialog v-model="exampleDialogVisible" title="API 调用示例" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="端点名称">{{ currentRoute?.name }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">
          <code>{{ apiBaseUrl }}{{ currentRoute?.pathPattern?.replace('**', 'v1/chat/completions') }}</code>
          <el-button size="small" @click="copyText(apiBaseUrl + currentRoute?.pathPattern?.replace('**', 'v1/chat/completions'))">复制</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="请求头">
          <code>Content-Type: application/json</code><br/>
          <code>X-User-Token: {{ userInfo.userToken }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="请求体">
          <div class="code-block">{{ getExampleBody(currentRoute) }}</div>
          <el-button size="small" @click="copyText(getExampleBody(currentRoute))">复制</el-button>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userLogin as userLoginApi, getPublicRoutes } from '../api/index.js'

const router = useRouter()
const isLoggedIn = ref(false)
const loading = ref(false)
const loginError = ref('')
const routes = ref([])
const routesLoading = ref(false)
const exampleDialogVisible = ref(false)
const currentRoute = ref(null)

const loginForm = ref({ username: '', password: '' })

const userInfo = ref({
  username: '',
  role: '',
  userToken: '',
  tokenBalance: 0,
  tokenUsed: 0,
  tokenRemaining: 0
})

const apiBaseUrl = computed(() => {
  return window.location.origin
})

onMounted(() => {
  const raw = localStorage.getItem('user_auth')
  if (raw) {
    try {
      userInfo.value = JSON.parse(raw)
      isLoggedIn.value = true
      fetchRoutes()
    } catch {
      logout()
    }
  }
})

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
    userInfo.value.tokenRemaining = userInfo.value.tokenBalance - userInfo.value.tokenUsed
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
  localStorage.removeItem('user_auth')
  isLoggedIn.value = false
  loginForm.value = { username: '', password: '' }
  router.push('/user')
}
</script>

<style scoped>
.user-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
code {
  background: #f4f4f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.login-card {
  max-width: 400px;
  margin: 100px auto;
}
.login-error {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  margin-top: 8px;
}
.code-block {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 8px 0;
}
</style>