<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { historyApi } from '@/api/history'
import type { HistoryRecord, DiffResult } from '@/types'

const route = useRoute()
const router = useRouter()

const entityType = 'ConfigItem'
const entityId = computed(() => Number(route.params.id))

const loading = ref(false)
const historyRecords = ref<HistoryRecord[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const timeRange = ref<[string, string] | null>(null)
const sortOrder = ref<'asc' | 'desc'>('desc')

const diffVisible = ref(false)
const diffLoading = ref(false)
const diffResult = ref<DiffResult | null>(null)
const selectedVersions = ref<number[]>([])

const rollbackVisible = ref(false)
const rollbackVersion = ref<HistoryRecord | null>(null)
const rollbackReason = ref('')

const columns = [
  { title: '版本号', dataIndex: 'versionNo', width: 80 },
  { title: '变更类型', dataIndex: 'changeType', width: 100 },
  { title: '变更字段', dataIndex: 'changeFields', width: 200 },
  { title: '操作人', dataIndex: 'operator', width: 120 },
  { title: '变更原因', dataIndex: 'changeReason', ellipsis: true },
  { title: '创建时间', dataIndex: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 180 }
]

async function fetchHistory() {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      pageSize: pageSize.value,
      sortBy: 'createdAt',
      sortOrder: sortOrder.value
    }
    
    if (timeRange.value) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    
    const result = await historyApi.getByTime(entityType, entityId.value, params)
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

function handleSearch() {
  currentPage.value = 1
  fetchHistory()
}

function handleReset() {
  timeRange.value = null
  sortOrder.value = 'desc'
  currentPage.value = 1
  fetchHistory()
}

function viewSnapshot(record: HistoryRecord) {
  const data = JSON.parse(record.snapshot)
  console.log('Snapshot:', data)
}

async function showDiff() {
  if (selectedVersions.value.length !== 2) {
    message.warning('请选择两个版本进行对比')
    return
  }
  
  diffLoading.value = true
  diffVisible.value = true
  try {
    diffResult.value = await historyApi.getDiff(
      entityType, 
      entityId.value,
      Math.min(...selectedVersions.value),
      Math.max(...selectedVersions.value)
    )
  } finally {
    diffLoading.value = false
  }
}

function confirmRollback(record: HistoryRecord) {
  rollbackVersion.value = record
  rollbackReason.value = ''
  rollbackVisible.value = true
}

async function handleRollback() {
  if (!rollbackVersion.value) return
  
  try {
    await historyApi.rollback(entityType, entityId.value, rollbackVersion.value.id, 'admin', rollbackReason.value)
    message.success('回退成功')
    rollbackVisible.value = false
    await fetchHistory()
  } catch (e) {
    message.error('回退失败')
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
          <a-button @click="goBack">返回</a-button>
          <span>配置 #{{ entityId }} 历史记录</span>
        </a-space>
      </template>
      
      <template #extra>
        <a-button type="primary" :disabled="selectedVersions.length !== 2" @click="showDiff">
          版本对比 ({{ selectedVersions.length }}/2)
        </a-button>
      </template>
      
      <div class="search-bar">
        <a-space>
          <a-range-picker
            v-model:value="timeRange"
            format="YYYY-MM-DD HH:mm:ss"
            show-time
          />
          <a-select v-model:value="sortOrder" style="width: 120px">
            <a-select-option value="desc">时间倒序</a-select-option>
            <a-select-option value="asc">时间正序</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
        </a-space>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="historyRecords"
        :loading="loading"
        :pagination="{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (t: number) => `共 ${t} 条`
        }"
        :row-selection="{
          selectedRowKeys: selectedVersions,
          onChange: (keys: number[]) => selectedVersions = keys,
          getCheckboxProps: () => ({ value: 1 })
        }"
        row-key="id"
        @change="(p: any) => handlePageChange(p.current, p.pageSize)"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'changeFields'">
            <a-tag v-for="field in record.changeFields" :key="field" style="margin: 2px">
              {{ field }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'changeType'">
            <a-tag :color="record.changeType === 'CREATE' ? 'green' : record.changeType === 'UPDATE' ? 'blue' : 'red'">
              {{ record.changeType }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewSnapshot(record)">查看快照</a-button>
              <a-popconfirm title="确定回退到此版本?" @confirm="confirmRollback(record)">
                <a-button type="link" size="small">回退</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="diffVisible"
      title="版本对比"
      width="800px"
      :footer="null"
    >
      <a-spin :spinning="diffLoading">
        <div v-if="diffResult" class="diff-content">
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="版本">{{ diffResult.version1 }} vs {{ diffResult.version2 }}</a-descriptions-item>
          </a-descriptions>
          
          <h4 style="margin-top: 16px">差异详情</h4>
          <a-table
            :columns="[
              { title: '字段', dataIndex: 'displayName' },
              { title: '变更类型', dataIndex: 'type' },
              { title: '旧值', dataIndex: 'oldValue' },
              { title: '新值', dataIndex: 'newValue' }
            ]"
            :data-source="Object.entries(diffResult.differences).map(([key, item]) => ({
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
      title="版本回退"
      @ok="handleRollback"
    >
      <a-form layout="vertical">
        <a-form-item label="回退原因">
          <a-input v-model:value="rollbackReason" placeholder="请输入回退原因" />
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

.search-bar {
  margin-bottom: 16px;
}

.diff-content {
  max-height: 500px;
  overflow-y: auto;
}
</style>