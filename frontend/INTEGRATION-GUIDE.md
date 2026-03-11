# ShellHistoryModal 组件移植指南

## 概述

`ShellHistoryModal` 是一个可复用的 Vue 3 弹出框组件，用于展示配置项的历史记录，支持版本对比和版本回退功能。

**主要功能：**
- 弹出框形式展示历史记录列表
- 支持选择2个版本进行对比（逐行差异显示）
- 支持版本回退（带确认提示）
- 通过 ref 调用 `open()` 方法打开弹框

---

## 安装依赖

确保项目已安装以下依赖：

```bash
npm install ant-design-vue @ant-design/icons-vue
```

---

## 组件导入

```typescript
import ShellHistoryModal from '@/components/ShellHistoryModal.vue'
import { ref } from 'vue'
```

---

## Props 属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| title | `string` | `'历史记录'` | Modal 标题 |
| entityType | `string` | `'config'` | 实体类型，用于扩展支持其他实体 |
| maxSelect | `number` | `2` | 最大可选版本数，用于版本对比 |

---

## Events 事件

| 事件名 | 参数 | 说明 |
|--------|------|------|
| rollback | `(version: any)` | 回退成功后触发，返回回退的版本信息 |
| refresh | `void` | 数据变更后触发，父组件应刷新数据 |

---

## Expose 方法

通过 `ref` 调用组件暴露的方法：

| 方法名 | 参数 | 说明 |
|--------|------|------|
| open | `(entityId: number)` | 打开 Modal 并加载指定实体的历史记录 |
| close | `void` | 关闭 Modal |

---

## 基础用法

```vue
<template>
  <div>
    <!-- 触发按钮 -->
    <a-button type="primary" @click="openHistory(123)">
      查看历史
    </a-button>
    
    <!-- Modal 组件 -->
    <ShellHistoryModal 
      ref="historyModal" 
      @refresh="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ShellHistoryModal from '@/components/ShellHistoryModal.vue'

const historyModal = ref<InstanceType<typeof ShellHistoryModal>>()

function openHistory(entityId: number) {
  historyModal.value?.open(entityId)
}

function loadData() {
  // 刷新数据逻辑
  console.log('Data refreshed')
}
</script>
```

---

## 完整示例（列表页面集成）

以下是在配置列表页面中集成历史记录功能的完整示例：

```vue
<template>
  <div class="config-list-page">
    <a-card title="配置列表">
      <a-table 
        :data-source="configs" 
        :loading="loading"
        row-key="id"
      >
        <a-table-column title="ID" data-index="id" width="80px" />
        <a-table-column title="配置键" data-index="configKey" />
        <a-table-column title="配置名称" data-index="configName" />
        <a-table-column title="操作" width="200px">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" size="small" @click="editConfig(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="openHistory(record)">
                <HistoryOutlined />
                历史
              </a-button>
              <a-button type="link" size="small" danger @click="deleteConfig(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
    </a-card>
    
    <!-- 历史记录弹出框 -->
    <ShellHistoryModal 
      ref="historyModal"
      title="配置历史记录"
      @rollback="onRollback"
      @refresh="loadConfigs"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { HistoryOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import ShellHistoryModal from '@/components/ShellHistoryModal.vue'
import { configApi } from '@/api/config'
import type { ConfigItem } from '@/types'

// State
const configs = ref<ConfigItem[]>([])
const loading = ref(false)
const historyModal = ref<InstanceType<typeof ShellHistoryModal>>()

// Load config list
async function loadConfigs() {
  loading.value = true
  try {
    const result = await configApi.list(1, 20)
    configs.value = result.records
  } finally {
    loading.value = false
  }
}

// Open history modal
function openHistory(record: ConfigItem) {
  historyModal.value?.open(record.id)
}

// Handle rollback
function onRollback(version: any) {
  message.success(`已回退到版本 v${version.versionNo}`)
}

// Edit config
function editConfig(record: ConfigItem) {
  // 编辑逻辑
}

// Delete config
function deleteConfig(record: ConfigItem) {
  // 删除逻辑
}

// Initialize
onMounted(() => {
  loadConfigs()
})
</script>
```

---

## 注意事项

1. **entityId 必须有效**：调用 `open(entityId)` 时，entityId 必须是数据库中存在的配置ID，否则会加载失败。

2. **刷新数据**：回退操作会触发 `refresh` 事件，父组件应监听此事件并刷新列表数据。

3. **版本对比**：版本对比需要选择恰好 2 个版本，否则"版本对比"按钮会被禁用。

4. **组件引用**：使用 `ref` 引用组件时，建议使用 `InstanceType<typeof ShellHistoryModal>` 类型以获得类型提示。

5. **样式隔离**：组件使用 `scoped` CSS，不会影响父组件样式。

---

## API 依赖

组件依赖以下后端 API 接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/configs/{id}/history` | GET | 获取配置历史记录列表 |
| `/api/v1/configs/{id}/diff` | GET | 版本对比，参数：`from`, `to` |
| `/api/v1/configs/{id}/rollback/{versionId}` | POST | 版本回退，参数：`operator`, `reason` |

**响应格式示例：**

```json
// GET /api/v1/configs/{id}/history
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "versionNo": 1,
        "changeType": "INIT",
        "operator": "admin",
        "changeReason": "初始化配置",
        "createdAt": "2024-01-15 10:30:00"
      }
    ],
    "total": 10
  }
}

// GET /api/v1/configs/{id}/diff?from=1&to=2
{
  "code": 200,
  "message": "success",
  "data": {
    "version1": 1,
    "version2": 2,
    "value1": "#!/bin/bash\necho v1",
    "value2": "#!/bin/bash\necho v2"
  }
}
```

---

## 扩展到其他实体

如需将此组件用于其他实体（如 Shell 脚本、系统配置等），只需：

1. 确保后端提供相同的 API 接口格式
2. 修改 `entityType` 属性
3. 在 API 层添加对应的接口调用

```vue
<ShellHistoryModal 
  ref="historyModal"
  entity-type="shell-script"
  title="脚本历史记录"
  @refresh="loadScripts"
/>
```

---

## 常见问题

### Q: 点击"历史"按钮后没有反应？
检查：
1. `entityId` 是否有效
2. 后端服务是否正常运行
3. 浏览器控制台是否有错误信息

### Q: 版本对比显示空白？
检查：
1. 是否选择了2个版本
2. 后端 diff 接口是否正常返回数据

### Q: 回退后数据没有更新？
确保父组件监听了 `@refresh` 事件并刷新数据。