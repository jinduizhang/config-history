<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as monaco from 'monaco-editor'
import { configApi } from '@/api/config'
import { message } from 'ant-design-vue'

const props = defineProps<{
  entityId: number
  entityType?: string
  title?: string
  readonly?: boolean
}>()

const emit = defineEmits<{
  (e: 'change', value: string): void
  (e: 'rollback', version: any): void
}>()

// State
const currentContent = ref('')
const historyRecords = ref<any[]>([])
const loading = ref(false)
const selectedHistory = ref<any>(null)
const diffVisible = ref(false)
const diffResult = ref<any>(null)
const selectedVersions = ref<number[]>([])

// Monaco Editor
const editorContainer = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// Columns for history table
const columns = [
  { title: 'Ver', dataIndex: 'versionNo', width: 50 },
  { title: 'Type', dataIndex: 'changeType', width: 70 },
  { title: 'Time', dataIndex: 'createdAt', width: 140 },
  { title: 'Op', key: 'action', width: 60 }
]

// Initialize Monaco Editor
function initEditor() {
  if (!editorContainer.value) return
  
  if (editor) {
    editor.dispose()
  }

  editor = monaco.editor.create(editorContainer.value, {
    value: currentContent.value,
    language: 'shell',
    theme: 'vs-dark',
    readOnly: props.readonly ?? true,
    minimap: { enabled: false },
    fontSize: 13,
    lineNumbers: 'on',
    scrollBeyondLastLine: false,
    automaticLayout: true,
    wordWrap: 'on'
  })
}

// Fetch history
async function fetchHistory() {
  if (!props.entityId) return
  
  loading.value = true
  try {
    const result = await configApi.getHistory(props.entityId, 1, 20)
    historyRecords.value = result.records
  } finally {
    loading.value = false
  }
}

// View history version
function viewVersion(record: any) {
  selectedHistory.value = record
  currentContent.value = record.configValue || record.snapshot || ''
  if (editor) {
    editor.setValue(currentContent.value)
  }
}

// Compare versions
async function compareVersions() {
  if (selectedVersions.value.length !== 2) {
    message.warning('Select 2 versions to compare')
    return
  }
  
  const ids = selectedVersions.value as [number, number]
  loading.value = true
  try {
    diffResult.value = await configApi.getDiff(props.entityId, ids[0], ids[1])
    diffVisible.value = true
  } finally {
    loading.value = false
  }
}

// Rollback to version
async function rollbackToVersion(record: any) {
  try {
    await configApi.rollback(props.entityId, record.id, 'admin', `Rollback to v${record.versionNo}`)
    message.success('Rollback success')
    await fetchHistory()
    emit('rollback', record)
  } catch (e) {
    message.error('Rollback failed')
  }
}

// Watch entity changes
watch(() => props.entityId, () => {
  fetchHistory()
}, { immediate: true })

// Watch content changes
watch(currentContent, (val) => {
  emit('change', val)
})

onMounted(() => {
  initEditor()
})
</script>

<template>
  <div class="shell-history-panel">
    <!-- Header -->
    <div class="panel-header">
      <span class="title">{{ title || 'Script History' }}</span>
      <a-space>
        <a-button 
          size="small" 
          type="primary"
          :disabled="selectedVersions.length !== 2"
          @click="compareVersions"
        >
          Compare ({{ selectedVersions.length }}/2)
        </a-button>
        <a-button size="small" @click="fetchHistory">Refresh</a-button>
      </a-space>
    </div>

    <!-- Main Content -->
    <div class="panel-body">
      <!-- Editor Section -->
      <div class="editor-section">
        <div class="section-header">
          <span>Script Content</span>
          <a-tag v-if="selectedHistory" color="blue">v{{ selectedHistory.versionNo }}</a-tag>
        </div>
        <div ref="editorContainer" class="editor-container"></div>
      </div>

      <!-- History Section -->
      <div class="history-section">
        <div class="section-header">
          <span>History ({{ historyRecords.length }})</span>
        </div>
        <a-table
          :columns="columns"
          :data-source="historyRecords"
          :loading="loading"
          :pagination="false"
          :scroll="{ y: 300 }"
          :row-selection="{
            selectedRowKeys: selectedVersions,
            onChange: (keys: number[]) => selectedVersions = keys
          }"
          row-key="id"
          size="small"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.dataIndex === 'changeType'">
              <a-tag 
                :color="record.changeType === 'INIT' ? 'green' : record.changeType === 'UPDATE' ? 'blue' : 'orange'"
                size="small"
              >
                {{ record.changeType }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <a-space size="small">
                <a-button type="link" size="small" @click="viewVersion(record)">View</a-button>
                <a-popconfirm title="Rollback?" @confirm="rollbackToVersion(record)">
                  <a-button type="link" size="small" danger>Back</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </div>

    <!-- Diff Modal -->
    <a-modal
      v-model:open="diffVisible"
      title="Version Comparison"
      width="900px"
      :footer="null"
    >
      <div v-if="diffResult" class="diff-content">
        <a-descriptions :column="2" size="small" bordered>
          <a-descriptions-item label="Versions">
            v{{ diffResult.version1 }} vs v{{ diffResult.version2 }}
          </a-descriptions-item>
        </a-descriptions>
        
        <div class="diff-list">
          <div v-for="(item, key) in diffResult.differences" :key="key" class="diff-item">
            <a-tag :color="item.type === 'ADD' ? 'green' : item.type === 'DELETE' ? 'red' : 'blue'">
              {{ item.type }}
            </a-tag>
            <span class="field-name">{{ key }}:</span>
            <span class="old-value" v-if="item.oldValue">{{ item.oldValue }}</span>
            <span class="arrow" v-if="item.oldValue && item.newValue">→</span>
            <span class="new-value" v-if="item.newValue">{{ item.newValue }}</span>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.shell-history-panel {
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #d9d9d9;
  background: #fafafa;
}

.panel-header .title {
  font-weight: 500;
  font-size: 14px;
}

.panel-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.editor-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #d9d9d9;
}

.editor-section .section-header {
  padding: 6px 10px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  font-size: 12px;
  color: #666;
}

.editor-container {
  flex: 1;
  min-height: 300px;
}

.history-section {
  width: 320px;
  display: flex;
  flex-direction: column;
}

.history-section .section-header {
  padding: 6px 10px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  font-size: 12px;
  color: #666;
}

.diff-content {
  padding: 16px 0;
}

.diff-list {
  margin-top: 16px;
}

.diff-item {
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  font-family: monospace;
  font-size: 12px;
}

.diff-item .field-name {
  font-weight: 500;
  margin: 0 8px;
}

.diff-item .old-value {
  color: #cf1322;
  text-decoration: line-through;
}

.diff-item .new-value {
  color: #389e0d;
}

.diff-item .arrow {
  margin: 0 8px;
  color: #999;
}
</style>