<template>
  <div>
    <div class="page-header">
      <h2>API Key 管理</h2>
      <el-button type="primary" @click="openCreateDialog">
        <el-icon><Plus /></el-icon> 新建 Key
      </el-button>
    </div>

    <el-alert
      title="API Key 用于调用需要认证的代理接口，通过 X-API-Key 请求头发送"
      type="info" show-icon closable style="margin-bottom:16px"
    />

    <el-table :data="keys" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="150" />
      <el-table-column prop="keyValue" label="Key 值" min-width="280">
        <template #default="{ row }">
          <el-input :model-value="row.keyValue" readonly size="small">
            <template #append>
              <el-button @click="copyKey(row.keyValue)" :icon="CopyDocument" />
            </template>
          </el-input>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
            {{ row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button text type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑 API Key' : '新建 API Key'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="给你的 Key 起个名字" />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="Key值" v-if="isEdit">
          <el-input :model-value="form.keyValueMasked" readonly />
          <span style="color:#909399;font-size:12px">如需更换Key，请删除后重新创建</span>
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
import { CopyDocument } from '@element-plus/icons-vue'
import { getApiKeys, createApiKey, updateApiKey, deleteApiKey } from '../api/index.js'

const keys = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editId = ref(null)

const form = ref({
  name: '',
  enabled: true,
  keyValueMasked: ''
})

onMounted(() => fetchKeys())

async function fetchKeys() {
  loading.value = true
  try {
    const res = await getApiKeys()
    keys.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
}

function openCreateDialog() {
  isEdit.value = false
  editId.value = null
  form.value = { name: '', enabled: true, keyValueMasked: '' }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  editId.value = row.id
  form.value = { name: row.name, enabled: row.enabled, keyValueMasked: row.keyValueMasked }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateApiKey(editId.value, { name: form.value.name, enabled: form.value.enabled })
      ElMessage.success('更新成功')
    } else {
      const res = await createApiKey(form.value.name)
      ElMessage.success(`创建成功: ${res.data.keyValue}`)
    }
    dialogVisible.value = false
    fetchKeys()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
  saving.value = false
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 Key "${row.name}"？删除后将无法使用。`, '确认')
    await deleteApiKey(row.id)
    ElMessage.success('删除成功')
    fetchKeys()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

function copyKey(val) {
  navigator.clipboard.writeText(val).then(() => ElMessage.success('已复制'))
}
</script>