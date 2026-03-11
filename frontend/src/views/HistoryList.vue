<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { configApi } from '@/api/config'
import type { DiffResult } from '@/types'

const route = useRoute()
const router = useRouter()

const configId = computed(() => Number(route.params.id))

const loading = ref(false)
const historyRecords = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const diffVisible = ref(false)
const diffLoading = ref(false)
const diffResult = ref<DiffResult | null>(null)
const selectedVersions = ref<number[]>([])

const rollbackVisible = ref(false)
const rollbackVersion = ref<any>(null)
const rollbackReason = ref('')

const columns = [
  { title: 'Version', dataIndex: 'versionNo', width: 80 },
  { title: 'Type', dataIndex: 'changeType', width: 100 },
  { title: 'Operator', dataIndex: 'operator', width: 120 },
  { title: 'Reason', dataIndex: 'changeReason', ellipsis: true },
  { title: 'Created', dataIndex: 'createdAt', width: 180 },
  { title: 'Action', key: 'action', width: 180 }
]

async function fetchHistory() {
  loading.value = true
  try {
    const result = await configApi.getHistory(configId.value, currentPage.value, pageSize.value)
    historyRecords.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number, size: number) {
  currentPage.value = page
  pageSize.value = size
  fetchHistory()
}

async function showDiff() {
  if (selectedVersions.value.length !== 2) {
    message.warning('Please select two versions to compare')
    return
  }
  
  diffLoading.value = true
  diffVisible.value = true
  try {
    diffResult.value = await configApi.getDiff(
      configId.value,
      Math.min(...selectedVersions.value),
      Math.max(...selectedVersions.value)
    )
  } finally {
    diffLoading.value = false
  }
}

function confirmRollback(record: any) {
  rollbackVersion.value = record
  rollbackReason.value = ''
  rollbackVisible.value = true
}

async function handleRollback() {
  if (!rollbackVersion.value) return
  
  try {
    await configApi.rollback(configId.value, rollbackVersion.value.id, 'admin', rollbackReason.value)
    message.success('Rollback success')
    rollbackVisible.value = false
    await fetchHistory()
  } catch (e) {
    message.error('Rollback failed')
  }
}

function goBack() {
  router.push('/config')
}

onMounted(() => {
  fetchHistory()
})
</script>

<template>
  <div class="history-list">
    <a-card>
      <template #title>
        <a-space>
          <a-button @click="goBack">Back</a-button>
          <span>Config #{{ configId }} History</span>
        </a-space>
      </template>
      
      <template #extra>
        <a-button type="primary" :disabled="selectedVersions.length !== 2" @click="showDiff">
          Compare ({{ selectedVersions.length }}/2)
        </a-button>
      </template>
      
      <a-table
        :columns="columns"
        :data-source="historyRecords"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (t: number) => `Total ${t}`
        }"
        :row-selection="{
          selectedRowKeys: selectedVersions,
          onChange: (keys: number[]) => selectedVersions = keys
        }"
        row-key="id"
        @change="(p: any) => handlePageChange(p.current, p.pageSize)"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'changeType'">
            <a-tag :color="record.changeType === 'INIT' ? 'green' : record.changeType === 'UPDATE' ? 'blue' : 'red'">
              {{ record.changeType }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-popconfirm title="Confirm rollback?" @confirm="confirmRollback(record)">
                <a-button type="link" size="small">Rollback</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="diffVisible"
      title="Version Compare"
      width="800px"
      :footer="null"
    >
      <a-spin :spinning="diffLoading">
        <div v-if="diffResult" class="diff-content">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="Version">{{ diffResult.sourceVersion }} vs {{ diffResult.targetVersion }}</a-descriptions-item>
          </a-descriptions>
          
          <h4 style="margin-top: 16px">Differences</h4>
          <a-table
            :columns="[
              { title: 'Field', dataIndex: 'displayName' },
              { title: 'Type', dataIndex: 'type' },
              { title: 'Old Value', dataIndex: 'oldValue' },
              { title: 'New Value', dataIndex: 'newValue' }
            ]"
            :data-source="Object.entries(diffResult.differences).map(([key, item]: [string, any]) => ({
              key,
              ...item,
              oldValue: JSON.stringify(item.oldValue),
              newValue: JSON.stringify(item.newValue)
            }))"
            size="small"
            :pagination="false"
          />
        </div>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:open="rollbackVisible"
      title="Rollback"
      @ok="handleRollback"
    >
      <a-form layout="vertical">
        <a-form-item label="Reason">
          <a-input v-model:value="rollbackReason" placeholder="Enter rollback reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.history-list {
  max-width: 1400px;
  margin: 0 auto;
}

.diff-content {
  max-height: 500px;
  overflow-y: auto;
}
</style>