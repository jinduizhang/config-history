<script setup lang="ts">
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import * as monaco from 'monaco-editor'
import { configApi } from '@/api/config'
import { message } from 'ant-design-vue'
import { 
  HistoryOutlined, 
  DiffOutlined, 
  RollbackOutlined,
  CodeOutlined,
  ClockCircleOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined
} from '@ant-design/icons-vue'

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
const isFullscreen = ref(false)
const editorHeight = ref(400)

// Monaco Editor
const editorContainer = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

// Initialize Monaco Editor
async function initEditor() {
  await nextTick()
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
    fontSize: 14,
    fontFamily: "'Fira Code', 'Cascadia Code', 'JetBrains Mono', Consolas, monospace",
    lineNumbers: 'on',
    scrollBeyondLastLine: false,
    automaticLayout: true,
    wordWrap: 'on',
    renderLineHighlight: 'all',
    cursorBlinking: 'smooth',
    smoothScrolling: true,
    folding: false,
    lineNumbersMinChars: 3,
    glyphMargin: false,
    padding: { top: 0, bottom: 0 },
    renderWhitespace: 'none',
    overviewRulerLanes: 0,
    hideCursorInOverviewRuler: true,
    overviewRulerBorder: false,
  })
}

// Fetch history
async function fetchHistory() {
  if (!props.entityId) return
  
  loading.value = true
  try {
    const result = await configApi.getHistory(props.entityId, 1, 20)
    historyRecords.value = result.records
    if (result.records.length > 0 && !selectedHistory.value) {
      viewVersion(result.records[0])
    }
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

// Compare versions - 字符级对比
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

// Compute line-by-line diff for display
const lineDiff = computed(() => {
  if (!diffResult.value) return []
  
  const v1 = diffResult.value.value1 || ''
  const v2 = diffResult.value.value2 || ''
  
  const lines1 = v1.split('\n')
  const lines2 = v2.split('\n')
  
  const result: Array<{
    type: 'same' | 'add' | 'delete' | 'modify'
    line1?: number
    line2?: number
    content: string
    oldContent?: string
  }> = []
  
  // Simple line-by-line comparison
  const maxLines = Math.max(lines1.length, lines2.length)
  
  for (let i = 0; i < maxLines; i++) {
    const line1 = lines1[i]
    const line2 = lines2[i]
    
    if (line1 === undefined) {
      result.push({ type: 'add', line2: i + 1, content: line2 })
    } else if (line2 === undefined) {
      result.push({ type: 'delete', line1: i + 1, content: line1 })
    } else if (line1 === line2) {
      result.push({ type: 'same', line1: i + 1, line2: i + 1, content: line1 })
    } else {
      result.push({ type: 'modify', line1: i + 1, line2: i + 1, content: line2, oldContent: line1 })
    }
  }
  
  return result
})

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

// Toggle fullscreen
function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  if (isFullscreen.value) {
    editorHeight.value = window.innerHeight - 200
  } else {
    editorHeight.value = 400
  }
}

// Start resize
function startResize(e: MouseEvent) {
  const startY = e.clientY
  const startHeight = editorHeight.value
  
  const onMouseMove = (e: MouseEvent) => {
    editorHeight.value = Math.max(200, Math.min(800, startHeight + e.clientY - startY))
  }
  
  const onMouseUp = () => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }
  
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

// Get change type color
function getChangeTypeColor(type: string) {
  switch (type) {
    case 'INIT': return '#52c41a'
    case 'UPDATE': return '#1890ff'
    case 'ROLLBACK': return '#fa8c16'
    default: return '#8c8c8c'
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
  <div class="shell-panel" :class="{ fullscreen: isFullscreen }">
    <!-- Header -->
    <div class="panel-header">
      <div class="header-left">
        <CodeOutlined class="header-icon" />
        <span class="header-title">{{ title || 'Script History' }}</span>
        <a-tag v-if="selectedHistory" color="#1890ff" class="version-tag">
          v{{ selectedHistory.versionNo }}
        </a-tag>
      </div>
      <div class="header-actions">
        <a-button 
          type="primary"
          ghost
          size="small"
          :disabled="selectedVersions.length !== 2"
          @click="compareVersions"
        >
          <template #icon><DiffOutlined /></template>
          Compare ({{ selectedVersions.length }}/2)
        </a-button>
        <a-tooltip :title="isFullscreen ? 'Exit Fullscreen' : 'Fullscreen'">
          <a-button size="small" @click="toggleFullscreen">
            <template #icon>
              <FullscreenExitOutlined v-if="isFullscreen" />
              <FullscreenOutlined v-else />
            </template>
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <!-- Main Content -->
    <div class="panel-content">
      <!-- Editor Section - 可拉伸 -->
      <div class="editor-section" :style="{ height: editorHeight + 'px' }">
        <div class="editor-toolbar">
          <span class="toolbar-title">
            <CodeOutlined /> Script Content
          </span>
          <div class="toolbar-info">
            <span v-if="selectedHistory" class="info-item">
              <ClockCircleOutlined />
              {{ selectedHistory.createdAt }}
            </span>
          </div>
        </div>
        <div ref="editorContainer" class="monaco-editor"></div>
        
        <!-- Resize Handle -->
        <div 
          class="resize-handle"
          @mousedown="startResize"
        ></div>
      </div>

      <!-- History Section -->
      <div class="history-section">
        <div class="history-header">
          <HistoryOutlined />
          <span>History</span>
          <a-badge :count="historyRecords.length" :overflow-count="99" />
        </div>
        
        <div class="history-list">
          <div 
            v-for="record in historyRecords" 
            :key="record.id"
            class="history-item"
            :class="{ 
              active: selectedHistory?.id === record.id,
              selected: selectedVersions.includes(record.id)
            }"
            @click="viewVersion(record)"
          >
            <div class="item-main">
              <a-checkbox
                :checked="selectedVersions.includes(record.id)"
                @click.stop="selectedVersions.includes(record.id) 
                  ? selectedVersions = selectedVersions.filter(id => id !== record.id)
                  : selectedVersions.length < 2 && selectedVersions.push(record.id)"
              />
              <span class="version-no">v{{ record.versionNo }}</span>
              <a-tag 
                :color="getChangeTypeColor(record.changeType)" 
                size="small"
                class="change-type"
              >
                {{ record.changeType }}
              </a-tag>
            </div>
            <div class="item-meta">
              <span class="meta-time">{{ record.createdAt?.split(' ')[1] || '' }}</span>
            </div>
            <div class="item-actions">
              <a-tooltip title="Rollback">
                <a-button 
                  type="text" 
                  size="small" 
                  danger
                  @click.stop="rollbackToVersion(record)"
                >
                  <template #icon><RollbackOutlined /></template>
                </a-button>
              </a-tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Diff Modal - 字符级对比 -->
    <a-modal
      v-model:open="diffVisible"
      title="Version Comparison (Line-by-Line)"
      width="95%"
      :footer="null"
      class="diff-modal"
    >
      <div v-if="diffResult" class="diff-result">
        <div class="diff-header">
          <a-tag color="#52c41a">v{{ diffResult.version1 }}</a-tag>
          <span class="diff-arrow">→</span>
          <a-tag color="#1890ff">v{{ diffResult.version2 }}</a-tag>
          <span class="diff-stats">
            {{ lineDiff.filter(l => l.type === 'add').length }} added,
            {{ lineDiff.filter(l => l.type === 'delete').length }} deleted,
            {{ lineDiff.filter(l => l.type === 'modify').length }} modified
          </span>
        </div>
        
        <div class="diff-container">
          <!-- Side by Side Diff -->
          <div class="diff-side-by-side">
            <!-- Old Version -->
            <div class="diff-pane">
              <div class="diff-pane-header">v{{ diffResult.version1 }}</div>
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
              <div class="diff-pane-header">v{{ diffResult.version2 }}</div>
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
  </div>
</template>

<style scoped>
.shell-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.shell-panel.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  border-radius: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon {
  color: #1890ff;
  font-size: 18px;
}

.header-title {
  color: #262626;
  font-size: 15px;
  font-weight: 600;
}

.version-tag {
  font-family: 'Fira Code', monospace;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.panel-content {
  display: flex;
  flex: 1;
  min-height: 0;
  flex-direction: column;
}

/* Editor Section - 暗色主题 */
.editor-section {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
  position: relative;
  min-height: 200px;
  max-height: 800px;
  background: #1e1e1e;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 16px;
  background: #252526;
  border-bottom: 1px solid #3c3c3c;
}

.toolbar-title {
  color: #858585;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.toolbar-info {
  display: flex;
  gap: 16px;
}

.info-item {
  color: #858585;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.monaco-editor {
  flex: 1;
  min-height: 150px;
  overflow: hidden;
}

.monaco-editor :deep(.monaco-editor) {
  padding: 0 !important;
}

.monaco-editor :deep(.margin) {
  background-color: #1e1e1e !important;
}

.monaco-editor :deep(.monaco-scrollable-element) {
  padding-top: 0 !important;
}

.resize-handle {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 6px;
  background: transparent;
  cursor: ns-resize;
  z-index: 10;
}

.resize-handle:hover {
  background: #1890ff;
}

/* History Section - 白色主题 */
.history-section {
  flex: 1;
  min-height: 150px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border-top: 1px solid #e8e8e8;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: #262626;
  font-size: 13px;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #f5f5f5;
}

.history-item.active {
  background: #e6f7ff;
  border-color: #1890ff;
}

.history-item.selected {
  background: #f0f5ff;
}

.item-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.version-no {
  color: #1890ff;
  font-family: 'Fira Code', monospace;
  font-size: 13px;
  font-weight: 600;
}

.change-type {
  font-size: 10px;
}

.item-meta {
  width: 100%;
  margin-top: 4px;
  padding-left: 24px;
}

.meta-time {
  color: #8c8c8c;
  font-size: 11px;
}

.item-actions {
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .item-actions {
  opacity: 1;
}

/* Diff Modal */
.diff-modal :deep(.ant-modal-content) {
  background: #ffffff;
  max-height: 90vh;
}

.diff-modal :deep(.ant-modal-header) {
  background: #ffffff;
  border-bottom-color: #e8e8e8;
}

.diff-modal :deep(.ant-modal-title) {
  color: #262626;
}

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
  height: 100%;
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
}

.line-content {
  margin: 0;
  padding: 0 16px;
  white-space: pre;
  overflow-x: auto;
  color: #434343;
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
.history-list::-webkit-scrollbar,
.diff-pane-content::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.history-list::-webkit-scrollbar-track,
.diff-pane-content::-webkit-scrollbar-track {
  background: transparent;
}

.history-list::-webkit-scrollbar-thumb,
.diff-pane-content::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.history-list::-webkit-scrollbar-thumb:hover,
.diff-pane-content::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>