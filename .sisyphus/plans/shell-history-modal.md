# Shell 历史记录弹出框组件开发计划

## TL;DR

> 创建可移植的弹出框组件，集成到配置列表页面，并提供完整的移植文档。
> 
> **交付物**：
> - `ShellHistoryModal.vue` - 可移植的弹出框组件
> - 更新 `ConfigList.vue` - 集成历史按钮
> - `INTEGRATION-GUIDE.md` - 移植文档
> 
> **预计工作量**：Medium
> **并行执行**：YES - 组件和文档可并行

---

## Context

### 用户需求
1. 在 Config 页面点击"历史"按钮弹出 Modal
2. Modal 内支持版本对比、回退操作
3. 组件需要可移植，方便后续集成到其他配置页面
4. 提供移植文档供其他 agent 使用

### 目标页面
- `ConfigList.vue` - 配置列表页面，每行添加"历史"按钮

---

## Work Objectives

### Core Objective
创建高度可复用的弹出框组件，并提供详细的移植指南。

### Concrete Deliverables
- `frontend/src/components/ShellHistoryModal.vue` - 可移植组件
- `frontend/src/views/ConfigList.vue` - 集成历史按钮
- `frontend/INTEGRATION-GUIDE.md` - 移植文档

### Definition of Done
- [ ] Config 列表每行有"历史"按钮
- [ ] 点击按钮弹出历史记录 Modal
- [ ] Modal 内表格显示历史记录
- [ ] 支持选择2个版本对比
- [ ] 支持版本回退
- [ ] 移植文档完整清晰

---

## TODOs

- [ ] 1. 创建 ShellHistoryModal.vue 可移植组件

  **What to do**:
  - 创建 `frontend/src/components/ShellHistoryModal.vue`
  - 使用 `a-modal` 作为主容器
  - 使用 `a-table` 展示历史记录列表
  - 列：选择(checkbox)、版本号、变更类型、操作人、变更原因、时间、操作
  - 版本对比功能：选择2个版本后点击"版本对比"按钮
  - 版本回退功能：点击"回退"按钮，弹出确认框
  - 通过 `defineExpose` 暴露 `open(entityId)` 方法
  - 通过 props 支持自定义标题

  **Props 设计**:
  ```typescript
  interface Props {
    title?: string           // Modal 标题，默认"历史记录"
    entityType?: string      // 实体类型，默认 'config'
    maxSelect?: number       // 最大选择数，默认 2
  }
  ```

  **Expose 方法**:
  ```typescript
  {
    open: (entityId: number) => void  // 打开并加载指定实体的历史
    close: () => void                 // 关闭 Modal
  }
  ```

  **Events**:
  ```typescript
  {
    rollback: (version: any) => void  // 回退成功后触发
    refresh: () => void               // 数据变更后触发
  }
  ```

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
  - **Skills**: [`frontend-ui-ux`]

- [ ] 2. 更新 ConfigList.vue 集成历史按钮

  **What to do**:
  - 在表格操作列添加"历史"按钮
  - 导入 `ShellHistoryModal` 组件
  - 点击按钮调用 `modalRef.open(record.id)`
  - 监听 `refresh` 事件刷新列表

  **代码示例**:
  ```vue
  <template>
    <!-- 表格操作列 -->
    <a-button type="link" size="small" @click="openHistory(record)">
      <HistoryOutlined />
      历史
    </a-button>
    
    <!-- Modal 组件 -->
    <ShellHistoryModal 
      ref="historyModal" 
      title="配置历史记录"
      @refresh="loadConfigs"
    />
  </template>
  
  <script setup>
  const historyModal = ref()
  
  function openHistory(record) {
    historyModal.value?.open(record.id)
  }
  </script>
  ```

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: []

- [ ] 3. 编写移植文档

  **What to do**:
  - 创建 `frontend/INTEGRATION-GUIDE.md`
  - 包含：组件介绍、Props 说明、Events 说明、使用示例、完整代码示例、注意事项

  **文档结构**:
  ```markdown
  # ShellHistoryModal 组件移植指南
  
  ## 概述
  ## 安装依赖
  ## Props
  ## Events
  ## Expose 方法
  ## 基础用法
  ## 完整示例
  ## 注意事项
  ## API 依赖
  ```

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: []

---

## Execution Strategy

### Parallel Waves

```
Wave 1 (并行):
├── Task 1: 创建 ShellHistoryModal.vue [visual-engineering]
└── Task 3: 编写移植文档 [writing]

Wave 2 (顺序):
└── Task 2: 更新 ConfigList.vue [quick]
```

---

## Success Criteria

### Verification Commands
```bash
# 启动前后端
cd frontend && npm run dev
mvn spring-boot:run

# 访问页面
# http://localhost:3000
# 1. 进入配置列表页面
# 2. 点击任意配置行的"历史"按钮
# 3. 验证 Modal 弹出并显示历史记录
# 4. 选择2个版本进行对比
# 5. 点击回退按钮测试回退功能
```

### Final Checklist
- [ ] Config 列表页面有"历史"按钮
- [ ] 点击按钮弹出 Modal
- [ ] 历史记录表格正确显示
- [ ] 版本对比功能正常
- [ ] 版本回退功能正常
- [ ] 移植文档完整