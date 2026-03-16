# OpenCode 主代理(Primary) vs 子代理(Subagent) 深度对比

> 基于官方文档、GitHub Issues 和社区实践总结

---

## 📋 核心概念

### 主代理(Primary Agent)
主代理是用户**直接交互**的主助手，通过 **Tab 键** 或 `switch_agent` 快捷键切换。它处理你的主要对话流，可以访问完整的工具集（受权限控制）。

### 子代理(Subagent)
子代理是**专业化助手**，由主代理调用或用户通过 **`@mention`** 手动调用。用于执行特定任务，创建独立的子会话。

---

## 🔍 详细对比

| 特性 | 主代理(Primary) | 子代理(Subagent) |
|------|----------------|------------------|
| **调用方式** | `Tab` 键切换<br>快捷键切换 | `@agent_name` 提及<br>主代理自动调用 |
| **配置位置** | `opencode.json` 或 `.opencode/agents/*.md` | `opencode.json` 或 `.opencode/agents/*.md` |
| **交互模式** | 直接对话 | 独立子会话 |
| **工具访问** | 完整（受 permission 控制） | 完整（受 permission 控制） |
| **会话关系** | 主会话 | 子会话（可嵌套） |
| **导航** | Tab 循环切换 | `Leader+Down` 进入子会话<br>`Left/Right` 切换子会话<br>`Up` 返回父会话 |
| **可见性** | UI 可见（Tab 切换器） | `@` 自动完成菜单<br>可设置 `hidden: true` |

---

## ✅ 主代理优势

### 1. **配置可靠，即时生效**
- 主代理配置在 **Tab 切换时立即加载**
- 不会被子代理继承问题困扰
- 配置隔离性好，不受其他代理影响

### 2. **用户体验直观**
- 通过 **Tab 键** 直接切换，操作简单
- 视觉反馈明确（颜色、图标）
- 无需记忆 `@` 命令

### 3. **权限控制稳定**
- `tools.bash: false` 等配置在主代理中**生效更稳定**
- 不会被主-子代理继承链覆盖
- 适合严格的安全控制场景（如禁用 mvn test）

### 4. **模型配置明确**
- 指定的 `model` 会**真正使用**
- 不会出现子代理"继承主代理模型"的问题

---

## ❌ 主代理劣势

### 1. **切换开销**
- 需要手动按 Tab 切换
- 不支持程序化/自动切换
- 一次只能有一个主代理处于活动状态

### 2. **不适合并行任务**
- 主代理是**顺序执行**的
- 不适合同时运行多个独立任务
- 需要等待当前任务完成才能切换

### 3. **上下文隔离弱**
- 主代理共享同一会话上下文
- 不同任务之间可能有干扰
- 需要手动清理或管理上下文

---

## ✅ 子代理优势

### 1. **并行执行**
- 可以同时调用**多个子代理**
- 适合大规模并行探索（如 explore + librarian 同时搜索）
- 每个子代理有**独立会话**

### 2. **自动调用**
- 主代理可根据任务描述**自动选择**合适的子代理
- 无需用户手动干预
- 适合构建智能路由（Orchestrator 模式）

### 3. **专业化分工**
- 每个子代理专注于特定领域
- 可以配置专门的 prompt 和工具集
- 代码更清晰，职责更明确

### 4. **上下文隔离**
- 子代理创建**独立会话**
- 不影响主会话上下文
- 任务完成后可独立关闭

---

## ❌ 子代理劣势（重要！）

### 1. **配置继承问题 ⚠️**
**GitHub Issue #10431, #17595, Reddit 讨论**

- **子代理经常忽略自己的配置**，继承主代理的模型和参数
- 现象：配置了轻量级模型，实际却使用主代理的重型模型
- 原因：子代理配置加载时机和继承链问题

```javascript
// 配置了子代理使用轻量级模型
{
  "agent": {
    "dtagent": {
      "mode": "subagent",
      "model": "anthropic/claude-haiku-4-5"  // ❌ 经常被忽略
    }
  }
}

// 实际运行时却使用主代理的模型
// Primary Agent: anthropic/claude-sonnet-4-5
// Subagent dtagent: anthropic/claude-sonnet-4-5 ❌ 不是 haiku！
```

### 2. **权限控制不稳定 ⚠️**
**GitHub Issue #10431, 用户实践经验**

- `tools.bash: false` 在子代理中可能**不生效**
- `permission.bash: deny` 可能被主代理权限覆盖
- 需要多层保险（tools + permission + prompt）才能确保安全

```javascript
// 子代理中配置了禁用 bash
{
  "agent": {
    "dtagent": {
      "tools": { "bash": false },
      "permission": { "bash": "deny" }
    }
  }
}

// 实际测试时仍可能执行 bash 命令！
// ❌ $ mvn test -Dtest=HistoryAspectTest
```

### 3. **调用复杂度高**
- 需要记住 `@agent_name` 语法
- 子会话导航复杂（Leader+Down/Left/Right/Up）
- 不适合简单、快速的任务切换

### 4. **调试困难**
- 子代理问题可能源于配置继承链
- 难以确定实际使用的是哪个模型/配置
- 需要查看多个层级（全局、项目、主代理、子代理）

---

## 🎯 使用建议

### 什么时候用主代理？

1. **需要严格的权限控制**
   - 如禁用 mvn test、禁止 bash 等
   - 安全敏感场景

2. **用户直接交互为主**
   - 高频使用场景
   - 需要快速切换

3. **配置必须可靠生效**
   - 模型选择关键
   - 工具限制必须强制执行

4. **单任务、顺序执行**
   - 不需要并行
   - 任务之间有依赖关系

### 什么时候用子代理？

1. **需要并行探索**
   - 同时搜索代码、文档、外部资源
   - 大规模信息收集

2. **自动路由/编排**
   - 构建智能代理系统
   - 主代理负责决策，子代理负责执行

3. **任务隔离**
   - 每个任务独立上下文
   - 避免上下文污染

4. **专业化分工**
   - 不同团队维护不同子代理
   - 复用现有子代理（如 explore, librarian）

---

## 🛠️ 实践配置示例

### 主代理配置（推荐用于权限控制）

```json
// opencode.json
{
  "agent": {
    "ut-generator": {
      "description": "Java单元测试生成主代理(只生成代码，不执行测试)",
      "mode": "primary",
      "model": "bailian-coding-plan/kimi-k2.5",
      "temperature": 0.1,
      "tools": {
        "write": true,
        "edit": true,
        "read": true,
        "bash": false,      // ❌ 完全禁用
        "skill": true,
        "task": false,      // ❌ 不能创建子代理
        "lsp": true,
        "grep": true,
        "glob": true,
        "mcp": true
      },
      "permission": {
        "edit": "allow",
        "read": "allow",
        "bash": "deny",     // ❌ 拒绝所有 bash
        "mcp": {
          "*": "deny",
          "playwright_*": "allow"  // ✅ 只允许浏览器自动化
        },
        "skill": {
          "generate-java-ut": "allow",
          "fix-java-ut": "allow",
          "java-coverage": "allow"
        },
        "task": "deny"
      },
      "prompt": "你是UTGenerator...",
      "color": "#FF6B6B"
    }
  }
}
```

### 子代理配置（用于并行任务）

```json
// opencode.json
{
  "agent": {
    "explore": {
      "description": "并行探索代码库",
      "mode": "subagent",
      "model": "anthropic/claude-haiku-4-5",
      "tools": {
        "read": true,
        "grep": true,
        "glob": true,
        "write": false,     // 只读
        "edit": false,
        "bash": false
      }
    },
    "librarian": {
      "description": "并行搜索文档",
      "mode": "subagent",
      "model": "anthropic/claude-haiku-4-5",
      "tools": {
        "websearch": true,
        "webfetch": true
      }
    }
  }
}

// 使用方式：
// task(subagent_type="explore", ...)
// task(subagent_type="librarian", ...)
// 两者并行执行
```

---

## ⚠️ 已知问题和最佳实践

### 问题1：子代理配置不生效

**症状**：配置了子代理模型，实际使用主代理模型

**解决**：
- 升级到最新版 OpenCode
- 使用主代理替代关键场景
- 在 prompt 中反复强调配置限制

### 问题2：子代理权限被继承

**症状**：子代理 `bash: false` 不生效，仍能执行命令

**解决**：
- 多层防护：tools + permission + prompt
- 使用主代理进行严格权限控制
- AGENTS.md 中添加全局规则提醒

### 问题3：子代理调用复杂

**症状**：忘记如何进入/退出子代理会话

**解决**：
- 使用主代理进行简单任务
- 配置快捷键：
  ```json
  {
    "keybind": {
      "session_child_first": "ctrl+down",
      "session_child_cycle": "right",
      "session_parent": "up"
    }
  }
  ```

---

## 📚 参考资源

- [OpenCode 官方文档 - Agents](https://opencode.ai/docs/agents)
- [GitHub Issue #10431](https://github.com/anomalyco/opencode/issues/10431) - subtask: false 被忽略
- [GitHub Issue #17595](https://github.com/anomalyco/opencode/issues/17595) - 运行时模型覆盖
- [Reddit 讨论](https://www.reddit.com/r/opencodeCLI/comments/1rme4c9/) - 子代理配置被忽略

---

## 💡 总结

| 场景 | 推荐选择 |
|------|----------|
| **权限控制严格**（如禁止 mvn test）| ✅ 主代理 |
| **用户高频交互** | ✅ 主代理 |
| **配置必须可靠** | ✅ 主代理 |
| **并行探索** | ⚠️ 子代理（需注意配置问题） |
| **自动路由/编排** | ⚠️ 子代理（复杂场景） |
| **简单任务切换** | ✅ 主代理 |

**一句话建议**：
> 如果配置可靠性比并行能力更重要，选择**主代理**；如果需要大规模并行且能接受配置不确定性，选择**子代理**。
