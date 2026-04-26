<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新建用户
      </el-button>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-statistic title="用户总数" :value="stats.totalUsers" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="总分配 Token" :value="stats.totalTokens" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已消耗 Token" :value="stats.totalUsed" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="剩余 Token" :value="stats.totalRemaining" />
      </el-col>
    </el-row>

    <!-- User table -->
    <el-table :data="users" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column label="密码" width="120">
        <template #default="{ row }">
          <el-input :model-value="row.password" readonly size="small" style="width:100px">
            <template #append>
              <el-button @click="copyToken(row.password)" size="small">复制</el-button>
            </template>
          </el-input>
        </template>
      </el-table-column>
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : row.role === 'TEST' ? 'warning' : 'primary'" size="small">
            {{ { ADMIN: '管理员', USER: '用户', TEST: '测试' }[row.role] || row.role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="userToken" label="用户 Token" min-width="280">
        <template #default="{ row }">
          <el-input :model-value="row.userToken" readonly size="small">
            <template #append>
              <el-button @click="copyToken(row.userToken)" :icon="CopyDocument" />
            </template>
          </el-input>
        </template>
      </el-table-column>
      <el-table-column prop="tokenBalance" label="分配额度" width="100" />
      <el-table-column prop="tokenUsed" label="已用" width="80" />
      <el-table-column prop="tokenRemaining" label="剩余" width="80">
        <template #default="{ row }">
          <span :style="{ color: row.tokenRemaining <= 0 ? '#f56c6c' : '#67c23a' }">
            {{ row.tokenRemaining }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button text type="primary" @click="openTokenDialog(row)">分配Token</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create user dialog -->
    <el-dialog v-model="createVisible" title="新建用户" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="createForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="createForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="createForm.role" style="width:100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="测试账号" value="TEST" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始Token">
          <el-input-number v-model="createForm.tokenBalance" :min="0" :step="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">创建</el-button>
      </template>
    </el-dialog>

    <!-- Edit user dialog -->
    <el-dialog v-model="editVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="editForm.password" type="text" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width:100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="测试账号" value="TEST" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="分配额度">
          <el-input-number v-model="editForm.tokenBalance" :min="0" :step="1000" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="editForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editing">保存</el-button>
      </template>
    </el-dialog>

    <!-- Token allocation dialog -->
    <el-dialog v-model="tokenVisible" title="分配 Token" width="400px">
      <p v-if="tokenUser">
        用户: <strong>{{ tokenUser.username }}</strong>
        &nbsp;当前剩余: <strong :style="{color:'#409eff'}">{{ tokenUser.tokenRemaining }}</strong>
      </p>
      <el-form>
        <el-form-item label="数量">
          <el-input-number v-model="tokenAmount" :min="1" :step="1000" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="tokenNote" placeholder="分配的用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tokenVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAllocate" :loading="allocating">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- Token records drawer -->
    <el-drawer v-model="drawerVisible" title="Token 流水" size="400px">
      <el-timeline v-if="records.length > 0">
        <el-timeline-item
          v-for="r in records" :key="r.id"
          :timestamp="r.createdAt"
          :type="r.type === 'ALLOCATE' ? 'primary' : r.type === 'USE' ? 'warning' : 'danger'"
        >
          <p>{{ r.type === 'ALLOCATE' ? '分配' : r.type === 'USE' ? '消耗' : '扣减' }}: {{ r.amount }}</p>
          <p v-if="r.note" style="font-size:12px;color:#999">{{ r.note }}</p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无记录" />
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { CopyDocument } from '@element-plus/icons-vue'
import { getUsers, createUser, updateUser, deleteUser, getUserStats, allocateTokens, getUserTokenRecords } from '../api/index.js'

const users = ref([])
const loading = ref(false)
const stats = ref({ totalUsers: 0, totalTokens: 0, totalUsed: 0, totalRemaining: 0 })
const creating = ref(false)
const allocating = ref(false)

// Create dialog
const createVisible = ref(false)
const createForm = ref({ username: '', password: '', role: 'USER', tokenBalance: 0 })

// Edit dialog
const editVisible = ref(false)
const editing = ref(false)
const editForm = ref({ id: null, username: '', password: '', role: 'USER', tokenBalance: 0, enabled: true })

// Token dialog
const tokenVisible = ref(false)
const tokenUser = ref(null)
const tokenAmount = ref(1000)
const tokenNote = ref('')

// Drawer
const drawerVisible = ref(false)
const records = ref([])

onMounted(() => { fetchUsers(); fetchStats() })

async function fetchUsers() {
  loading.value = true
  try { const r = await getUsers(); users.value = r.data } catch (e) { /* ignore */ }
  loading.value = false
}

async function fetchStats() {
  try { const r = await getUserStats(); stats.value = r.data } catch (e) { /* ignore */ }
}

function openCreateDialog() {
  createForm.value = { username: '', password: '', role: 'USER', tokenBalance: 0 }
  createVisible.value = true
}

function openEditDialog(row) {
  editForm.value = {
    id: row.id,
    username: row.username,
    password: row.password,
    role: row.role,
    tokenBalance: row.tokenBalance,
    enabled: row.enabled
  }
  editVisible.value = true
}

async function handleEdit() {
  editing.value = true
  try {
    await updateUser(editForm.value.id, {
      username: editForm.value.username,
      password: editForm.value.password,
      role: editForm.value.role,
      tokenBalance: editForm.value.tokenBalance
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchUsers(); fetchStats()
  } catch (e) {
    ElMessage.error('保存失败')
  }
  editing.value = false
}

async function handleCreate() {
  creating.value = true
  try {
    await createUser(createForm.value)
    ElMessage.success('创建成功')
    createVisible.value = false
    fetchUsers(); fetchStats()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
  creating.value = false
}

function openTokenDialog(row) {
  tokenUser.value = { ...row }
  tokenAmount.value = 1000
  tokenNote.value = ''
  tokenVisible.value = true
}

async function handleAllocate() {
  allocating.value = true
  try {
    const r = await allocateTokens(tokenUser.value.id, { amount: tokenAmount.value, note: tokenNote.value })
    ElMessage.success('分配成功')
    tokenVisible.value = false
    fetchUsers(); fetchStats()
  } catch (e) {
    ElMessage.error('分配失败')
  }
  allocating.value = false
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '确认')
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUsers(); fetchStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function showRecords(row) {
  try {
    const r = await getUserTokenRecords(row.id)
    records.value = r.data
    drawerVisible.value = true
  } catch (e) { /* ignore */ }
}

function copyToken(val) {
  navigator.clipboard.writeText(val).then(() => ElMessage.success('已复制'))
}
</script>
