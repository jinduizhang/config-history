<script setup lang="ts">
import { ref } from 'vue'
import ShellHistoryPanel from '@/components/ShellHistoryPanel.vue'

const configId = ref(2)

function handleChange(value: string) {
  console.log('Script changed:', value)
}

function handleRollback(version: any) {
  console.log('Rollback to version:', version)
}
</script>

<template>
  <div class="demo-page">
    <div class="page-header">
      <h1>Shell Script Configuration</h1>
      <p>Manage your shell scripts with version history</p>
    </div>

    <div class="page-content">
      <a-row :gutter="24">
        <a-col :span="6">
          <div class="side-panel">
            <div class="panel-section">
              <h3>Configuration</h3>
              <a-form layout="vertical">
                <a-form-item label="Script ID">
                  <a-input-number 
                    v-model:value="configId" 
                    :min="1" 
                    style="width: 100%"
                  />
                </a-form-item>
                <a-form-item label="Script Name">
                  <a-input value="deploy.sh" readonly />
                </a-form-item>
              </a-form>
            </div>

            <div class="panel-section">
              <h3>Quick Actions</h3>
              <a-space direction="vertical" style="width: 100%">
                <a-button type="primary" block>Save Changes</a-button>
                <a-button block>Run Script</a-button>
              </a-space>
            </div>

            <div class="panel-section tips">
              <h3>Tips</h3>
              <ul>
                <li>Click history item to view</li>
                <li>Select 2 versions to compare</li>
                <li>Click rollback icon to restore</li>
              </ul>
            </div>
          </div>
        </a-col>

        <a-col :span="18">
          <div class="main-panel">
            <ShellHistoryPanel
              :entity-id="configId"
              entity-type="ShellScript"
              title="Deploy Script"
              :readonly="true"
              @change="handleChange"
              @rollback="handleRollback"
            />
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<style scoped>
.demo-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  padding: 20px 32px;
  background: #ffffff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.page-header h1 {
  margin: 0 0 6px 0;
  color: #262626;
  font-size: 22px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.page-content {
  padding: 24px;
}

.side-panel {
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.panel-section {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-section:last-child {
  border-bottom: none;
}

.panel-section h3 {
  margin: 0 0 16px 0;
  color: #262626;
  font-size: 14px;
  font-weight: 600;
}

.panel-section :deep(.ant-form-item-label label) {
  color: #595959;
}

.tips ul {
  margin: 0;
  padding-left: 20px;
  color: #8c8c8c;
  font-size: 13px;
  line-height: 2;
}

.main-panel {
  height: calc(100vh - 140px);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>