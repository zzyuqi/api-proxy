<template>
  <div class="app-container">
    <!-- Sidebar -->
    <div class="sidebar">
      <div class="sidebar-header">API 中转系统</div>
      <div class="sidebar-menu">
        <router-link
          v-for="item in menu"
          :key="item.path"
          :to="item.path"
          class="menu-item"
          active-class="active"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </div>
    </div>

    <!-- Main -->
    <div class="main-area">
      <div class="topbar">
        <div class="topbar-title">{{ currentTitle }}</div>
        <div class="topbar-status">
          <span class="status-dot"></span>
          服务运行中
          <el-button text type="primary" @click="logout" style="margin-left:16px">
            退出登录
          </el-button>
        </div>
      </div>
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const menu = [
  { path: '/dashboard', label: '总览', icon: 'DataBoard' },
  { path: '/routes', label: '路由管理', icon: 'Guide' },
  { path: '/api-keys', label: 'API Key 管理', icon: 'Key' },
  { path: '/models', label: '模型配置', icon: 'Cpu' },
  { path: '/logs', label: '请求日志', icon: 'Document' },
  { path: '/users', label: '用户管理', icon: 'User' }
]

const titleMap = {
  Dashboard: '总览',
  Routes: '路由管理',
  ApiKeys: 'API Key 管理',
  Models: '模型配置',
  Logs: '请求日志',
  Users: '用户管理'
}

const currentTitle = computed(() => titleMap[route.name] || '')

function logout() {
  localStorage.removeItem('admin_auth')
  router.push('/login')
}
</script>
