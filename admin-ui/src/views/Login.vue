<template>
  <div class="login-page">
    <el-card class="login-card" v-if="!needInit">
      <template #header>
        <h2>管理员登录</h2>
      </template>

      <el-form :model="form" @submit.prevent="adminLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="adminLogin" :loading="loading" style="width:100%">
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="loginError" class="login-error">{{ loginError }}</div>

      <div style="text-align:center;margin-top:16px">
        <router-link to="/user" style="color:#67c23a">我是普通用户，点击这里登录</router-link>
      </div>
    </el-card>

    <!-- Init admin card (only on first run) -->
    <el-card class="login-card" v-else>
      <template #header><h2>初始化管理员</h2></template>
      <el-alert title="首次使用，请创建管理员账号" type="info" show-icon style="margin-bottom:16px" />
      <el-form :model="initForm" @submit.prevent="doInit">
        <el-form-item label="用户名">
          <el-input v-model="initForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="initForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="doInit" :loading="loading" style="width:100%">
            创建管理员
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="initError" class="login-error">{{ initError }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { initAdmin } from '../api/index.js'
import api from '../api/index.js'

const router = useRouter()
const loading = ref(false)
const needInit = ref(false)
const loginError = ref('')
const initError = ref('')

const form = ref({ username: 'admin', password: 'admin123' })
const initForm = ref({ username: 'admin', password: 'admin123' })

async function adminLogin() {
  loading.value = true
  loginError.value = ''
  const { username, password } = form.value
  const auth = btoa(`${username}:${password}`)
  try {
    await api.get('/admin/routes', {
      headers: { Authorization: 'Basic ' + auth }
    })
    localStorage.setItem('admin_auth', auth)
    localStorage.removeItem('user_auth')
    router.push('/dashboard')
  } catch (e) {
    if (e.response?.status === 401) {
      loginError.value = '用户名或密码错误'
    } else {
      needInit.value = true
    }
  }
  loading.value = false
}

async function doInit() {
  loading.value = true
  initError.value = ''
  try {
    const { username, password } = initForm.value
    await initAdmin(username, password)
    const auth = btoa(`${username}:${password}`)
    localStorage.setItem('admin_auth', auth)
    router.push('/dashboard')
  } catch (e) {
    initError.value = e.response?.data?.error || '初始化失败'
  }
  loading.value = false
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
}
.login-error {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  margin-top: 8px;
}
</style>