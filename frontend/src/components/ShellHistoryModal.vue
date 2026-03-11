<script setup lang="ts">
import { ref, computed } from 'vue'
import { configApi } from '@/api/config'
import { message, Modal } from 'ant-design-vue'
import { h } from 'vue'
import { 
  DiffOutlined, 
  RollbackOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined
} from '@ant-design/icons-vue'

// Props
const props = withDefaults(defineProps<{
  title?: string
  entityType?: string
  maxSelect?: number
}>(), {
  title: '历史记录',
  entityType: 'config',
  maxSelect: 2
})

// Events
const emit = defineEmits<{
  (e: 'rollback', version: any): void
  (e: 'refresh'): void
}>()

// State
const visible = ref(false)
const currentEntityId = ref<number>(0)
const historyRecords = ref<any[]>([])
const loading = ref(false)
const selectedVersions = ref<number[]>([])
const diffVisible = ref(false)
const diffResult = ref<any>(null)

// Fetch history records
async function fetchHistory() {
  if (!currentEntityId.value) return
  
  loading.value = true
  try {
    const result = await configApi.getHistory(currentEntityId.value, 1, 50)
    historyRecords.value = result.records || []
  } catch (e) {
    message.error('加载历史记录失败')
  } finally {
    loading.value = false
  }
}

// Open modal with entityId
function open(entityId: number) {
  currentEntityId.value = entityId
  selectedVersions.value = []
  visible.value = true
  fetchHistory()
}

// Close modal
function close() {
  visible.value = false
  selectedVersions.value = []
}

// Toggle version selection
function toggleVersion(id: number) {
  const index = selectedVersions.value.indexOf(id)
  if (index > -1) {
    selectedVersions.value.splice(index, 1)
  } else if (selectedVersions.value.length < props.maxSelect) {
    selectedVersions.value.push(id)
  }
}

// Compare selected versions
async function compareVersions() {
  if (selectedVersions.value.length !== 2) {
    message.warning(`请选择${props.maxSelect}个版本进行对比`)
    return
  }
  
  const v1 = selectedVersions.value[0]
  const v2 = selectedVersions.value[1]
  if (!v1 || !v2 || !currentEntityId.value) return
  
  loading.value = true
  try {
    diffResult.value = await configApi.getDiff(
      currentEntityId.value, 
      v1,
      v2
    )
    diffVisible.value = true
  } catch (e) {
    message.error('版本对比失败')
  } finally {
    loading.value = false
  }
}

// Rollback to specific version
function rollbackToVersion(record: any) {
  Modal.confirm({
    title: '确认回退',
    icon: () => h(ExclamationCircleOutlined),
    content: `确定要回退到版本 v${record.versionNo} 吗？此操作不可撤销。`,
    okText: '确认回退',
    cancelText: '取消',
    okType: 'danger',
    async onOk() {
      try {
        await configApi.rollback(
          currentEntityId.value, 
          record.id, 
          'admin', 
          `回退到版本 v${record.versionNo}`
        )
        message.success('回退成功')
        await fetchHistory()
        emit('rollback', record)
        emit('refresh')
      } catch (e) {
        message.error('回退失败')
      }
    }
  })
}

// Get version number by id
function getVersionNo(id: number | undefined) {
  if (!id) return ''
  return historyRecords.value.find(r => r.id === id)?.versionNo || ''
}

// Compute line-by-line diff
const lineDiff = computed(() => {
  if (!diffResult.value) return []
  
  const sourceContent = diffResult.value.sourceValue || ''
  const targetContent = diffResult.value.targetValue || ''
  
  const sourceLines = sourceContent.split('\n')
  const targetLines = targetContent.split('\n')
  
  const result: Array<{
    type: 'same' | 'add' | 'delete' | 'modify'
    line1?: number
    line2?: number
    content: string
    oldContent?: string
  }> = []
  
  const maxLines = Math.max(sourceLines.length, targetLines.length)
  
  for (let i = 0; i < maxLines; i++) {
    const sourceLine = sourceLines[i]
    const targetLine = targetLines[i]
    
    if (sourceLine === undefined) {
      result.push({ type: 'add', line2: i + 1, content: targetLine })
    } else if (targetLine === undefined) {
      result.push({ type: 'delete', line1: i + 1, content: sourceLine })
    } else if (sourceLine === targetLine) {
      result.push({ type: 'same', line1: i + 1, line2: i + 1, content: sourceLine })
    } else {
      result.push({ type: 'modify', line1: i + 1, line2: i + 1, content: targetLine, oldContent: sourceLine })
    }
  }
  
  return result
})

// Get change type color
function getChangeTypeColor(type: string) {
  switch (type) {
    case 'INIT': return '#52c41a'
    case 'UPDATE': return '#1890ff'
    case 'ROLLBACK': return '#fa8c16'
    default: return '#8c8c8c'
  }
}

// Expose methods
defineExpose({ open, close })
</script>

<template>
  <!-- Main History Modal -->
  <a-modal
    v-model:open="visible"
    :title="title"
    width="900px"
    :footer="null"
    @cancel="close"
  >
    <div class="history-modal-content">
      <!-- Toolbar -->
      <div class="toolbar">
        <div class="toolbar-left">
          <a-button 
            type="primary"
            :disabled="selectedVersions.length !== maxSelect"
            @click="compareVersions"
          >
            <template #icon><DiffOutlined /></template>
            版本对比 ({{ selectedVersions.length }}/{{ maxSelect }})
          </a-button>
        </div>
        <div class="toolbar-right">
          <span class="record-count">共 {{ historyRecords.length }} 条记录</span>
        </div>
      </div>

      <!-- History Table -->
      <a-table
        :data-source="historyRecords"
        :loading="loading"
        :pagination="{ pageSize: 10, showSizeChanger: false }"
        size="small"
        row-key="id"
        :row-class-name="(record: any) => selectedVersions.includes(record.id) ? 'selected-row' : ''"
      >
        <a-table-column title="选择" width="60px" align="center">
          <template #default="{ record }">
            <a-checkbox
              :checked="selectedVersions.includes(record.id)"
              :disabled="!selectedVersions.includes(record.id) && selectedVersions.length >= maxSelect"
              @change="toggleVersion(record.id)"
            />
          </template>
        </a-table-column>
        
        <a-table-column title="版本" data-index="versionNo" width="80px">
          <template #default="{ record }">
            <span class="version-tag">v{{ record.versionNo }}</span>
          </template>
        </a-table-column>
        
        <a-table-column title="类型" data-index="changeType" width="100px">
          <template #default="{ record }">
            <a-tag :color="getChangeTypeColor(record.changeType)" size="small">
              {{ record.changeType }}
            </a-tag>
          </template>
        </a-table-column>
        
        <a-table-column title="操作人" data-index="operator" width="100px">
          <template #default="{ record }">
            {{ record.operator || '-' }}
          </template>
        </a-table-column>
        
        <a-table-column title="变更原因" data-index="changeReason" ellipsis>
          <template #default="{ record }">
            <span :title="record.changeReason">{{ record.changeReason || '-' }}</span>
          </template>
        </a-table-column>
        
        <a-table-column title="时间" data-index="createdAt" width="170px">
          <template #default="{ record }">
            <span class="time-text">
              <ClockCircleOutlined style="margin-right: 4px; color: #8c8c8c" />
              {{ record.createdAt }}
            </span>
          </template>
        </a-table-column>
        
        <a-table-column title="操作" width="80px" align="center">
          <template #default="{ record }">
            <a-button 
              type="link" 
              size="small"
              danger
              @click="rollbackToVersion(record)"
            >
              <template #icon><RollbackOutlined /></template>
              回退
            </a-button>
          </template>
        </a-table-column>
      </a-table>
    </div>
  </a-modal>

  <!-- Diff Modal -->
  <a-modal
    v-model:open="diffVisible"
    title="版本对比"
    width="95%"
    :footer="null"
    class="diff-modal"
  >
    <div v-if="diffResult" class="diff-result">
      <div class="diff-header">
        <a-tag color="#52c41a">v{{ getVersionNo(selectedVersions[0]) }}</a-tag>
        <span class="diff-arrow">→</span>
        <a-tag color="#1890ff">v{{ getVersionNo(selectedVersions[1]) }}</a-tag>
        <span class="diff-stats">
          {{ lineDiff.filter(l => l.type === 'add').length }} 行新增,
          {{ lineDiff.filter(l => l.type === 'delete').length }} 行删除,
          {{ lineDiff.filter(l => l.type === 'modify').length }} 行修改
        </span>
      </div>
      
      <div class="diff-container">
        <div class="diff-side-by-side">
          <!-- Old Version -->
          <div class="diff-pane">
            <div class="diff-pane-header">v{{ getVersionNo(selectedVersions[0]) }}</div>
            <div class="diff-pane-content">
              <div 
                v-for="(line, idx) in lineDiff" 
                :key="'old-' + idx"
                class="diff-line"
                :class="line.type"
              >
                <span class="line-num">{{ line.line1 || '' }}</span>
                <pre class="line-content">{{ line.type === 'add' ? '' : (line.oldContent || line.content) }}</pre>
              </div>
            </div>
          </div>
          
          <!-- New Version -->
          <div class="diff-pane">
            <div class="diff-pane-header">v{{ getVersionNo(selectedVersions[1]) }}</div>
            <div class="diff-pane-content">
              <div 
                v-for="(line, idx) in lineDiff" 
                :key="'new-' + idx"
                class="diff-line"
                :class="line.type"
              >
                <span class="line-num">{{ line.line2 || '' }}</span>
                <pre class="line-content">{{ line.type === 'delete' ? '' : line.content }}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<style scoped>
.history-modal-content {
  min-height: 400px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  display: flex;
  gap: 8px;
}

.toolbar-right {
  color: #8c8c8c;
  font-size: 13px;
}

.record-count {
  color: #8c8c8c;
}

.version-tag {
  color: #1890ff;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-weight: 600;
}

.time-text {
  color: #595959;
  font-size: 12px;
}

:deep(.selected-row) {
  background-color: #e6f7ff;
}

:deep(.selected-row:hover > td) {
  background-color: #bae7ff !important;
}

/* Diff Modal Styles */
.diff-modal :deep(.ant-modal-body) {
  padding: 0;
  max-height: calc(90vh - 110px);
  overflow: hidden;
}

.diff-result {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.diff-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
}

.diff-arrow {
  color: #8c8c8c;
  font-size: 16px;
}

.diff-stats {
  color: #8c8c8c;
  font-size: 12px;
  margin-left: auto;
}

.diff-container {
  flex: 1;
  overflow: hidden;
}

.diff-side-by-side {
  display: flex;
  height: 500px;
}

.diff-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e8e8e8;
}

.diff-pane:last-child {
  border-right: none;
}

.diff-pane-header {
  padding: 8px 16px;
  background: #f5f5f5;
  border-bottom: 1px solid #e8e8e8;
  color: #595959;
  font-size: 12px;
  font-weight: 500;
}

.diff-pane-content {
  flex: 1;
  overflow: auto;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  background: #fafafa;
}

.diff-line {
  display: flex;
  min-height: 24px;
}

.diff-line.same {
  background: transparent;
}

.diff-line.add {
  background: #f6ffed;
}

.diff-line.delete {
  background: #fff2f0;
}

.diff-line.modify {
  background: #e6f7ff;
}

.line-num {
  width: 50px;
  min-width: 50px;
  padding: 0 8px;
  text-align: right;
  color: #bfbfbf;
  background: #f5f5f5;
  user-select: none;
  font-size: 12px;
  line-height: 24px;
}

.line-content {
  margin: 0;
  padding: 0 16px;
  white-space: pre;
  overflow-x: auto;
  color: #434343;
  line-height: 24px;
}

.diff-line.add .line-content {
  color: #52c41a;
}

.diff-line.delete .line-content {
  color: #ff4d4f;
}

.diff-line.modify .line-content {
  color: #1890ff;
}

/* Scrollbar */
.diff-pane-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.diff-pane-content::-webkit-scrollbar-track {
  background: transparent;
}

.diff-pane-content::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.diff-pane-content::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>