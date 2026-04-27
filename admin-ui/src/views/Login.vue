<template>
  <div class="login-page">
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
      <div class="login-footer">
        <p>还没有账号？请联系管理员获取</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userLogin as userLoginApi } from '../api/index.js'
import { User, Lock, Warning } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const loginError = ref('')

const loginForm = ref({ username: '', password: '' })

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
    ElMessage.success('登录成功')
    router.push('/user')
  } catch (e) {
    if (e.response?.status === 401) {
      loginError.value = '用户名或密码错误'
    } else {
      loginError.value = e.response?.data?.error || '登录失败'
    }
  }
  loading.value = false
}
</script>

<style scoped>
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

.login-card :deep(.el-card__body) {
  padding: 32px 24px;
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

.login-btn:hover {
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.4);
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

.login-footer {
  text-align: center;
  margin-top: 24px;
}

.login-footer p {
  color: #9ca3af;
  font-size: 13px;
}
</style>
