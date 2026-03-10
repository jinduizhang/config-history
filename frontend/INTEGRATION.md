# Shell 脚本历史管理组件对接文档

## 概述

`ShellHistoryPanel` 是一个可嵌入的 Vue 3 组件，用于管理 Shell 脚本的历史版本。支持版本查看、版本对比、版本回退等功能。

## 技术栈

- Vue 3 + TypeScript
- Ant Design Vue
- Monaco Editor

## 快速集成

### 1. 安装依赖

```bash
npm install ant-design-vue @ant-design/icons-vue monaco-editor
```

### 2. 注册组件

```typescript
// main.ts
import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'

const app = createApp(App)
app.use(Antd)
app.mount('#app')
```

### 3. 使用组件

```vue
<template>
  <ShellHistoryPanel
    :entity-id="scriptId"
    entity-type="config"
    title="Deploy Script"
    @change="handleContentChange"
    @rollback="handleRollback"
  />
</template>

<script setup lang="ts">
import ShellHistoryPanel from '@/components/ShellHistoryPanel.vue'

const scriptId = 2  // 脚本ID

function handleContentChange(content: string) {
  console.log('Content changed:', content)
}

function handleRollback(version: any) {
  console.log('Rolled back to:', version)
}
</script>
```

## Props

| 属性 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| entityId | number | ✓ | - | 实体ID（脚本ID） |
| entityType | string | ✗ | 'config' | 实体类型 |
| title | string | ✗ | 'Script History' | 面板标题 |
| readonly | boolean | ✗ | true | 编辑器是否只读 |

## Events

| 事件 | 参数 | 说明 |
|------|------|------|
| change | (content: string) | 内容变化时触发 |
| rollback | (version: any) | 版本回退时触发 |

## API 接口要求

组件依赖以下后端 API：

### 获取历史记录列表

```
GET /api/v1/history/{entityType}/{entityId}?page=1&size=20
```

**响应格式：**
```json
{
  "records": [
    {
      "id": 1,
      "versionNo": 1,
      "configValue": "#!/bin/bash\necho 'Hello'",
      "snapshot": "#!/bin/bash\necho 'Hello'",
      "changeType": "INIT",
      "createdAt": "2024-01-15 10:30:00",
      "changedBy": "admin"
    }
  ],
  "total": 10
}
```

### 获取版本对比

```
GET /api/v1/history/{entityType}/{entityId}/diff?fromVersionId=1&toVersionId=2
```

**响应格式：**
```json
{
  "version1": 1,
  "version2": 2,
  "value1": "#!/bin/bash\necho 'Hello'",
  "value2": "#!/bin/bash\necho 'World'"
}
```

### 版本回退

```
POST /api/v1/history/{entityType}/{entityId}/rollback/{versionId}
```

**请求体：**
```json
{
  "operator": "admin",
  "reason": "Rollback to v1"
}
```

## 功能特性

### 1. 脚本展示框

- 使用 Monaco Editor 显示脚本内容
- 支持 Shell 语法高亮
- 深色主题，等宽字体
- 支持拉伸调整高度
- 支持全屏模式

### 2. 历史版本列表

- 显示所有历史版本
- 显示版本号、变更类型、变更时间
- 支持选择两个版本进行对比
- 支持一键回退

### 3. 版本对比

- 逐行对比显示
- 标记新增、删除、修改的行
- 并排展示两个版本

## 样式定制

组件使用 CSS 变量，可通过覆盖样式定制：

```css
/* 编辑器区域高度 */
.shell-panel .editor-section {
  min-height: 200px;
  max-height: 800px;
}

/* 历史列表宽度 */
.shell-panel .history-section {
  min-width: 200px;
}
```

## 完整示例

参考 `src/views/ShellConfigDemo.vue`：

```vue
<template>
  <div class="demo-page">
    <h1>Shell 脚本配置管理</h1>
    
    <div class="config-container">
      <!-- 左侧：脚本选择 -->
      <div class="script-list">
        <a-list :data-source="scripts">
          <template #renderItem="{ item }">
            <a-list-item 
              :class="{ active: selectedScript === item.id }"
              @click="selectedScript = item.id"
            >
              {{ item.name }}
            </a-list-item>
          </template>
        </a-list>
      </div>
      
      <!-- 右侧：历史管理面板 -->
      <div class="history-panel">
        <ShellHistoryPanel
          v-if="selectedScript"
          :entity-id="selectedScript"
          title="Deploy Script"
        />
      </div>
    </div>
  </div>
</template>
```

## 注意事项

1. **后端服务**：确保后端 API 服务已启动并配置正确的 CORS
2. **Monaco Editor**：组件会自动加载 Monaco Editor，首次加载可能需要几秒钟
3. **响应式布局**：组件支持响应式布局，但建议最小宽度 800px
4. **全屏模式**：全屏模式下会覆盖整个视口，按 ESC 或点击按钮退出

## 常见问题

### Q: 编辑器显示空白？

检查：
1. 后端 API 是否正常返回数据
2. `entityId` 是否正确
3. 控制台是否有错误信息

### Q: 跨域请求失败？

确保后端配置了 CORS：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### Q: 如何自定义编辑器主题？

修改 `ShellHistoryPanel.vue` 中的 Monaco 配置：

```typescript
editor = monaco.editor.create(editorContainer.value, {
  theme: 'vs-light',  // 或自定义主题
  // ...
})
```