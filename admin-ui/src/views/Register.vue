<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header><h2>用户注册</h2></template>
      <el-form :model="form" @submit.prevent="handleRegister">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="至少3位" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="error" class="register-error">{{ error }}</div>
      <div style="text-align:center;margin-top:12px">
        已有账号？
        <router-link to="/login" style="color:#409eff">去登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userRegister } from '../api/index.js'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = ref({ username: '', password: '', confirmPassword: '' })

async function handleRegister() {
  error.value = ''
  if (!form.value.username || !form.value.password) {
    error.value = '用户名和密码不能为空'
    return
  }
  if (form.value.password.length < 3) {
    error.value = '密码长度不能少于3位'
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    error.value = '两次密码输入不一致'
    return
  }

  loading.value = true
  try {
    await userRegister({ username: form.value.username, password: form.value.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    error.value = e.response?.data?.error || '注册失败'
  }
  loading.value = false
}
</script>

<style scoped>
.register-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 400px;
}
.register-error {
  color: #f56c6c;
  font-size: 13px;
  text-align: center;
  margin-top: 8px;
}
</style>
