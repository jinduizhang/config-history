<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { configApi } from '@/api/config'
import type { ConfigItem, ConfigRequest } from '@/types'

const router = useRouter()

const loading = ref(false)
const configs = ref<ConfigItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const modalVisible = ref(false)
const modalLoading = ref(false)
const editingConfig = ref<ConfigItem | null>(null)
const formData = ref<ConfigRequest>({
  configKey: '',
  configName: '',
  configValue: '',
  description: ''
})

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '配置键', dataIndex: 'configKey', width: 150 },
  { title: '配置名称', dataIndex: 'configName', width: 150 },
  { title: '配置值', dataIndex: 'configValue', ellipsis: true },
  { title: '描述', dataIndex: 'description', ellipsis: true },
  { title: '创建时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 250 }
]

async function fetchConfigs() {
  loading.value = true
  try {
    const result = await configApi.list(currentPage.value, pageSize.value)
    configs.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingConfig.value = null
  formData.value = { configKey: '', configName: '', configValue: '', description: '' }
  modalVisible.value = true
}

function handleEdit(record: ConfigItem) {
  editingConfig.value = record
  formData.value = {
    configKey: record.configKey,
    configName: record.configName,
    configValue: record.configValue,
    description: record.description
  }
  modalVisible.value = true
}

async function handleDelete(id: number) {
  await configApi.delete(id)
  message.success('删除成功')
  await fetchConfigs()
}

async function handleSubmit() {
  modalLoading.value = true
  try {
    if (editingConfig.value) {
      await configApi.update(editingConfig.value.id, formData.value)
      message.success('更新成功')
    } else {
      await configApi.create(formData.value)
      message.success('创建成功')
    }
    modalVisible.value = false
    await fetchConfigs()
  } finally {
    modalLoading.value = false
  }
}

function handlePageChange(page: number, size: number) {
  currentPage.value = page
  pageSize.value = size
  fetchConfigs()
}

function viewHistory(id: number) {
  router.push(`/config/${id}/history`)
}

onMounted(() => {
  fetchConfigs()
})
</script>

<template>
  <div class="config-list">
    <a-card title="配置列表">
      <template #extra>
        <a-button type="primary" @click="handleAdd">新增配置</a-button>
      </template>
      
      <a-table
        :columns="columns"
        :data-source="configs"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`
        }"
        row-key="id"
        @change="(p: any) => handlePageChange(p.current, p.pageSize)"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewHistory(record.id)">
                历史
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-popconfirm title="确定删除?" @confirm="handleDelete(record.id)">
                <a-button type="link" size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="modalVisible"
      :title="editingConfig ? '编辑配置' : '新增配置'"
      :confirm-loading="modalLoading"
      @ok="handleSubmit"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="配置键" required>
          <a-input v-model:value="formData.configKey" placeholder="请输入配置键" />
        </a-form-item>
        <a-form-item label="配置名称" required>
          <a-input v-model:value="formData.configName" placeholder="请输入配置名称" />
        </a-form-item>
        <a-form-item label="配置值" required>
          <a-textarea v-model:value="formData.configValue" placeholder="请输入配置值" :rows="4" />
        </a-form-item>
        <a-form-item label="描述">
          <a-input v-model:value="formData.description" placeholder="请输入描述" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.config-list {
  max-width: 1400px;
  margin: 0 auto;
}
</style>