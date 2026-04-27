<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
      <div>
        <el-button type="success" @click="handleGenerate" :loading="generating">
          <el-icon><Plus /></el-icon> 一键生成用户
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon> 新建用户
        </el-button>
      </div>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6">
        <el-statistic title="用户总数" :value="stats.totalUsers" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="总分配次数" :value="stats.totalRequests" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已使用次数" :value="stats.totalUsed" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="剩余次数" :value="stats.totalRemaining" />
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
      <el-table-column prop="requestCount" label="分配次数" width="90" />
      <el-table-column prop="requestsUsed" label="已用" width="70" />
      <el-table-column prop="requestsRemaining" label="剩余" width="70">
        <template #default="{ row }">
          <span :style="{ color: row.requestsRemaining <= 0 ? '#f56c6c' : '#67c23a' }">
            {{ row.requestsRemaining }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="并发限制" width="90">
        <template #default="{ row }">
          <span>{{ row.concurrentLimit || '无' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="小时限制" width="90">
        <template #default="{ row }">
          <span>{{ row.hourlyLimit || '无' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="过期时间" width="120">
        <template #default="{ row }">
          <span v-if="row.expireAt" :class="{ 'expired-text': isExpired(row.expireAt) }">
            {{ formatExpire(row.expireAt) }}
          </span>
          <span v-else>永久</span>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="70">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button text type="primary" @click="openTokenDialog(row)">分配次数</el-button>
          <el-button text type="success" @click="copyUserInfo(row)">复制信息</el-button>
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
        <el-form-item label="初始次数">
          <el-input-number v-model="createForm.requestCount" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="并发限制">
          <el-input-number v-model="createForm.concurrentLimit" :min="0" :step="1" placeholder="0表示不限制" />
          <span style="color:#909399;font-size:12px;margin-left:8px">同时最大请求数，0为不限制</span>
        </el-form-item>
        <el-form-item label="小时限制">
          <el-input-number v-model="createForm.hourlyLimit" :min="0" :step="100" placeholder="0表示不限制" />
          <span style="color:#909399;font-size:12px;margin-left:8px">每小时最大请求数，0为不限制</span>
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
        <el-form-item label="分配次数">
          <el-input-number v-model="editForm.requestCount" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="并发限制">
          <el-input-number v-model="editForm.concurrentLimit" :min="0" :step="1" placeholder="0表示不限制" />
          <span style="color:#909399;font-size:12px;margin-left:8px">同时最大请求数，0为不限制</span>
        </el-form-item>
        <el-form-item label="小时限制">
          <el-input-number v-model="editForm.hourlyLimit" :min="0" :step="100" placeholder="0表示不限制" />
          <span style="color:#909399;font-size:12px;margin-left:8px">每小时最大请求数，0为不限制</span>
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

    <!-- Generated user dialog -->
    <el-dialog v-model="generatedVisible" title="用户创建成功" width="450px">
      <el-alert type="success" :closable="false" style="margin-bottom:16px">
        请立即保存以下信息，关闭窗口后将无法再次查看密码
      </el-alert>
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input :model-value="generatedUser.username" readonly>
            <template #append>
              <el-button @click="copyToken(generatedUser.username)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input :model-value="generatedUser.password" readonly show-password>
            <template #append>
              <el-button @click="copyToken(generatedUser.password)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用户Token">
          <el-input :model-value="generatedUser.userToken" readonly>
            <template #append>
              <el-button @click="copyToken(generatedUser.userToken)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="generatedVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Package selection dialog -->
    <el-dialog v-model="packageVisible" title="选择套餐" width="500px">
      <el-form label-width="100px">
        <el-form-item label="价格套餐">
          <el-radio-group v-model="selectedPackage" size="large">
            <el-radio-button v-for="pkg in packages" :key="pkg.count" :value="pkg.count">
              {{ pkg.price }}元 / {{ pkg.count }}次
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="并发限制">
          <el-input-number v-model="generateForm.concurrentLimit" :min="1" :max="10" />
          <span style="color:#909399;font-size:12px;margin-left:8px">同时最大请求数</span>
        </el-form-item>
        <el-form-item label="小时限制">
          <el-input-number v-model="generateForm.hourlyLimit" :min="1" :max="200" />
          <span style="color:#909399;font-size:12px;margin-left:8px">每小时最大请求数</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="packageVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmGenerate" :loading="generating">确认生成</el-button>
      </template>
    </el-dialog>

    <!-- Token allocation dialog -->
    <el-dialog v-model="tokenVisible" title="分配请求次数" width="400px">
      <p v-if="tokenUser">
        用户: <strong>{{ tokenUser.username }}</strong>
        &nbsp;当前剩余: <strong :style="{color:'#409eff'}">{{ tokenUser.requestsRemaining }}</strong>
      </p>
      <el-form>
        <el-form-item label="次数">
          <el-input-number v-model="tokenAmount" :min="1" :step="100" />
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

    <!-- Copy user info dialog -->
    <el-dialog v-model="copyInfoVisible" title="复制用户信息" width="600px">
      <el-form label-width="100px">
        <el-form-item label="API地址">
          <el-input v-model="copyInfoForm.apiUrl" readonly>
            <template #append>
              <el-button @click="doCopy(copyInfoForm.apiUrl)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="copyInfoForm.username" readonly>
            <template #append>
              <el-button @click="doCopy(copyInfoForm.username)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="copyInfoForm.password" readonly show-password>
            <template #append>
              <el-button @click="doCopy(copyInfoForm.password)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="用户Token">
          <el-input v-model="copyInfoForm.userToken" readonly>
            <template #append>
              <el-button @click="doCopy(copyInfoForm.userToken)">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="一键复制全部">
          <el-button type="success" @click="doCopy(copyInfoForm.fullText)" style="width:100%">
            复制完整信息给用户
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="copyInfoVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Token records drawer -->
    <el-drawer v-model="drawerVisible" title="请求次数流水" size="400px">
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
import { getUsers, createUser, updateUser, deleteUser, getUserStats, allocateTokens, getUserTokenRecords, generateUser } from '../api/index.js'

const users = ref([])
const loading = ref(false)
const stats = ref({ totalUsers: 0, totalRequests: 0, totalUsed: 0, totalRemaining: 0 })
const creating = ref(false)
const allocating = ref(false)
const generating = ref(false)
const generatedVisible = ref(false)
const generatedUser = ref({ username: '', password: '', userToken: '' })

const packages = [
  { price: 1, count: 100 },
  { price: 5, count: 500 },
  { price: 10, count: 1000 },
  { price: 20, count: 2000 },
  { price: 50, count: 5000 },
  { price: 100, count: 10000 }
]
const selectedPackage = ref(100)
const packageVisible = ref(false)
const generateForm = ref({ concurrentLimit: 2, hourlyLimit: 50 })

const copyInfoVisible = ref(false)
const copyInfoForm = ref({
  apiUrl: '',
  username: '',
  password: '',
  userToken: '',
  fullText: ''
})

// Create dialog
const createVisible = ref(false)
const createForm = ref({ username: '', password: '', role: 'USER', requestCount: 0, concurrentLimit: 0, hourlyLimit: 0 })

// Edit dialog
const editVisible = ref(false)
const editing = ref(false)
const editForm = ref({ id: null, username: '', password: '', role: 'USER', requestCount: 0, enabled: true, concurrentLimit: 0, hourlyLimit: 0 })

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
  createForm.value = { username: '', password: '', role: 'USER', requestCount: 0, concurrentLimit: 0, hourlyLimit: 0 }
  createVisible.value = true
}

function handleGenerate() {
  selectedPackage.value = 100
  generateForm.value = { concurrentLimit: 2, hourlyLimit: 50 }
  packageVisible.value = true
}

async function confirmGenerate() {
  generating.value = true
  try {
    const r = await generateUser(12, selectedPackage.value, generateForm.value.concurrentLimit, generateForm.value.hourlyLimit)
    generatedUser.value = r.data
    packageVisible.value = false
    generatedVisible.value = true
    fetchUsers(); fetchStats()
  } catch (e) {
    ElMessage.error('生成失败')
  }
  generating.value = false
}

function openEditDialog(row) {
  editForm.value = {
    id: row.id,
    username: row.username,
    password: row.password,
    role: row.role,
    requestCount: row.requestCount,
    enabled: row.enabled,
    concurrentLimit: row.concurrentLimit || 0,
    hourlyLimit: row.hourlyLimit || 0
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
      requestCount: editForm.value.requestCount,
      enabled: editForm.value.enabled,
      concurrentLimit: editForm.value.concurrentLimit,
      hourlyLimit: editForm.value.hourlyLimit
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
  tokenAmount.value = 100
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

function copyUserInfo(row) {
  const baseUrl = window.location.origin
  const apiUrl = `${baseUrl}/api/proxy/minimax/v1`
  const fullText = `尊敬的用户，感谢您的使用！以下是您的账号信息，请妥善保管：

🌐 API地址：
${apiUrl}

👤 用户名：
${row.username}

🔐 密码：
${row.password}

🔑 用户Token：
${row.userToken}

📝 使用说明：
1. 在调用API时，通过 X-User-Token 请求头传递 Token 进行认证
2. 请求示例：
   POST ${apiUrl}/chat/completions
   Headers: Content-Type: application/json, X-User-Token: ${row.userToken}
   Body: {"model":"MiniMax-M2.7","messages":[{"role":"user","content":"你好"}]}

祝您使用愉快！`

  copyInfoForm.value = {
    apiUrl,
    username: row.username,
    password: row.password,
    userToken: row.userToken,
    fullText
  }
  copyInfoVisible.value = true
}

function doCopy(text) {
  navigator.clipboard.writeText(text).then(() => ElMessage.success('已复制'))
}

function formatExpire(expireAt) {
  if (!expireAt) return '永久'
  const date = new Date(expireAt)
  const now = new Date()
  const diff = date - now
  if (diff <= 0) return '已过期'
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days > 0) return `${days}天后`
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  if (hours > 0) return `${hours}小时后`
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${minutes}分钟后`
}

function isExpired(expireAt) {
  if (!expireAt) return false
  return new Date(expireAt) <= new Date()
}
</script>

<style scoped>
.expired-text {
  color: #f56c6c;
  font-weight: 600;
}
</style>
