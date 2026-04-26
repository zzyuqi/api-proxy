<template>
  <div>
    <div class="page-header">
      <h2>模型配置</h2>
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加模型
      </el-button>
    </div>

    <el-alert
      title="配置不同的模型提供商，如 OpenAI、MiniMax、Claude 等。路由可绑定指定模型。"
      type="info" show-icon closable style="margin-bottom:16px"
    />

    <el-table :data="models" stripe v-loading="loading" style="width:100%">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="120">
        <template #default="{ row }">
          <span style="font-weight:500">{{ row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="provider" label="提供商" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ row.provider }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="apiUrl" label="API 地址" min-width="200">
        <template #default="{ row }">
          <code style="font-size:12px">{{ row.apiUrl }}</code>
        </template>
      </el-table-column>
      <el-table-column prop="apiKeyMasked" label="API Key" width="150">
        <template #default="{ row }">
          <span style="color:#909399;font-family:monospace">{{ row.apiKeyMasked }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="defaultModelId" label="默认模型" width="150">
        <template #default="{ row }">
          {{ row.defaultModelId || '-' }}
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

    <!-- Create/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '添加模型'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="如：MiniMax 生产环境" />
        </el-form-item>
        <el-form-item label="提供商" required>
          <el-select v-model="form.provider" placeholder="选择提供商" style="width:100%">
            <el-option label="MiniMax" value="minimax" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="Azure OpenAI" value="azure" />
            <el-option label="Anthropic" value="anthropic" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API 地址" required>
          <el-input v-model="form.apiUrl" placeholder="https://api.minimaxi.com" />
        </el-form-item>
        <el-form-item label="API Key" required>
          <el-input v-model="form.apiKey" type="password" show-password placeholder="sk-..." />
        </el-form-item>
        <el-form-item label="默认模型">
          <el-input v-model="form.defaultModelId" placeholder="如：MiniMax-M2.7" />
        </el-form-item>
        <el-form-item label="可用模型">
          <el-input
            v-model="form.models"
            type="textarea"
            :rows="2"
            placeholder="多个模型用逗号分隔，如：MiniMax-M2.7,MiniMax-Text-01"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.displayOrder" :min="0" :max="999" />
          <span style="margin-left:8px;color:#909399">数字越小越靠前</span>
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
import { getModels, createModel, updateModel, deleteModel } from '../api/index.js'

const models = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)

const defaultForm = () => ({
  name: '',
  provider: 'minimax',
  apiUrl: '',
  apiKey: '',
  defaultModelId: '',
  models: '',
  displayOrder: 0,
  status: 'ENABLED'
})

const form = ref(defaultForm())

onMounted(() => fetchModels())

async function fetchModels() {
  loading.value = true
  try {
    const res = await getModels()
    models.value = res.data
  } catch (e) { /* ignore */ }
  loading.value = false
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.id
    form.value = {
      name: row.name,
      provider: row.provider,
      apiUrl: row.apiUrl,
      apiKey: '',
      defaultModelId: row.defaultModelId || '',
      models: row.models || '',
      displayOrder: row.displayOrder || 0,
      status: row.status
    }
  } else {
    isEdit.value = false
    editId.value = null
    form.value = defaultForm()
  }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  if (!form.value.apiUrl.trim()) {
    ElMessage.warning('请输入 API 地址')
    return
  }
  if (!form.value.apiKey.trim()) {
    ElMessage.warning('请输入 API Key')
    return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateModel(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createModel(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchModels()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
  saving.value = false
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除模型 "${row.name}"？`, '确认')
    await deleteModel(row.id)
    ElMessage.success('删除成功')
    fetchModels()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}
</script>