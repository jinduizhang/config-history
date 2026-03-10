<script setup lang="ts">
import { ref } from 'vue'
import ShellHistoryPanel from '@/components/ShellHistoryPanel.vue'

// 模拟配置ID（实际使用时从路由或父组件传入）
const configId = ref(1)

// 处理脚本变更
function handleChange(value: string) {
  console.log('Script changed:', value)
}

// 处理回退
function handleRollback(version: any) {
  console.log('Rollback to version:', version)
}
</script>

<template>
  <div class="shell-config-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>Shell Script Configuration</h2>
      <p>Integrate the history panel into your shell script configuration page</p>
    </div>

    <!-- 集成示例 -->
    <div class="integration-demo">
      <a-row :gutter="16">
        <!-- 左侧：配置表单示例 -->
        <a-col :span="8">
          <a-card title="Config Form" size="small">
            <a-form layout="vertical">
              <a-form-item label="Config ID">
                <a-input-number v-model:value="configId" :min="1" style="width: 100%" />
              </a-form-item>
              <a-form-item label="Script Name">
                <a-input placeholder="deploy.sh" />
              </a-form-item>
              <a-form-item label="Description">
                <a-textarea placeholder="Script description..." :rows="2" />
              </a-form-item>
              <a-form-item>
                <a-space>
                  <a-button type="primary">Save</a-button>
                  <a-button>Cancel</a-button>
                </a-space>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>

        <!-- 右侧：Shell历史组件 -->
        <a-col :span="16">
          <div class="history-wrapper">
            <ShellHistoryPanel
              :entity-id="configId"
              entity-type="ShellScript"
              title="Shell Script History"
              :readonly="true"
              @change="handleChange"
              @rollback="handleRollback"
            />
          </div>
        </a-col>
      </a-row>
    </div>

    <!-- 使用说明 -->
    <a-card title="Integration Guide" size="small" style="margin-top: 16px">
      <a-alert type="info" show-icon style="margin-bottom: 16px">
        <template #message>
          <strong>Quick Integration</strong>
        </template>
        <template #description>
          <pre style="margin: 8px 0; font-size: 12px">
&lt;ShellHistoryPanel
  :entity-id="yourConfigId"
  entity-type="ShellScript"
  title="Script History"
  @change="handleScriptChange"
  @rollback="handleRollback"
/&gt;</pre>
        </template>
      </a-alert>

      <a-descriptions :column="1" size="small" bordered>
        <a-descriptions-item label="entityId">
          The ID of your shell script config (required)
        </a-descriptions-item>
        <a-descriptions-item label="entityType">
          Entity type, default: 'ShellScript'
        </a-descriptions-item>
        <a-descriptions-item label="title">
          Panel title, default: 'Script History'
        </a-descriptions-item>
        <a-descriptions-item label="readonly">
          Editor read-only mode, default: true
        </a-descriptions-item>
        <a-descriptions-item label="@change">
          Emitted when script content changes
        </a-descriptions-item>
        <a-descriptions-item label="@rollback">
          Emitted when rollback is performed
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
  </div>
</template>

<style scoped>
.shell-config-page {
  padding: 16px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0 0 4px 0;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.history-wrapper {
  height: 400px;
}
</style>