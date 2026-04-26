<template>
  <div>
    <div class="page-header">
      <h2>路由规则</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新建路由
      </el-button>
    </div>

    <el-table :data="routes" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="120" />
      <el-table-column prop="pathPattern" label="路径匹配" min-width="200">
        <template #default="{ row }">
          <code>{{ row.pathPattern }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="targetUrl" label="目标地址" min-width="250">
        <template #default="{ row }">
          <code>{{ row.targetUrl }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="method" label="方法" width="80" />
      <el-table-column prop="requiresAuth" label="需认证" width="80">
        <template #default="{ row }">
          <el-tag :type="row.requiresAuth ? 'danger' : 'info'" size="small">
            {{ row.requiresAuth ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rateLimit" label="限流" width="80">
        <template #default="{ row }">
          {{ row.rateLimit || '不限' }}
        </template>
      </el-table-column>
      <el-table-column prop="allowedModels" label="允许模型" width="150">
        <template #default="{ row }">
          <span v-if="row.allowedModels" style="font-size:12px;color:#67C23A">{{ row.allowedModels }}</span>
          <span v-else style="color:#909399">不限制</span>
        </template>
      </el-table-column>
      <el-table-column prop="userVisible" label="用户可见" width="90">
        <template #default="{ row }">
          <el-tag :type="row.userVisible ? 'success' : 'info'" size="small">
            {{ row.userVisible ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑路由' : '新建路由'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="路径匹配">
          <el-input v-model="form.pathPattern" placeholder="/api/proxy/example/**" />
        </el-form-item>
        <el-form-item label="目标地址">
          <el-input v-model="form.targetUrl" placeholder="https://api.example.com" />
        </el-form-item>
        <el-form-item label="限制方法">
          <el-select v-model="form.method" clearable placeholder="不限" style="width:100%">
            <el-option label="不限" value="" />
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="需要认证">
          <el-switch v-model="form.requiresAuth" />
        </el-form-item>
        <el-form-item label="限流(次/分)">
          <el-input-number v-model="form.rateLimit" :min="0" :max="10000" />
          <span style="margin-left:8px;color:#909399">0=不限</span>
        </el-form-item>
        <el-form-item label="允许模型">
          <el-input
            v-model="form.allowedModels"
            placeholder="如：MiniMax-M2.7,abab6.5-chat，多个逗号分隔"
          />
          <span style="margin-left:8px;color:#909399">留空则不限制模型</span>
        </el-form-item>
        <el-form-item label="用户可见">
          <el-switch v-model="form.userVisible" />
          <span style="margin-left:8px;color:#909399">关闭后用户将看不到此端点</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.status"
            active-value="ENABLED"
            inactive-value="DISABLED"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoutes, createRoute, updateRoute, deleteRoute } from '../api/index.js'

const routes = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  pathPattern: '',
  targetUrl: '',
  method: '',
  requiresAuth: false,
  rateLimit: 0,
  allowedModels: '',
  userVisible: true,
  status: 'ENABLED'
})

onMounted(() => fetchRoutes())

async function fetchRoutes() {
  loading.value = true
  try {
    const res = await getRoutes()
    routes.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.value = { ...row, method: row.method || '' }
  } else {
    isEdit.value = false
    editId.value = null
    form.value = { name: '', pathPattern: '', targetUrl: '', method: '', requiresAuth: false, rateLimit: 0, allowedModels: '', userVisible: true, status: 'ENABLED' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (isEdit.value) {
      await updateRoute(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createRoute(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchRoutes()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
  saving.value = false
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除路由 "${row.name}"？`, '确认')
    await deleteRoute(row.id)
    ElMessage.success('删除成功')
    fetchRoutes()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>
