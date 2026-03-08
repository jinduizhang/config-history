# 配置历史管理模块 - 功能分析报告

## 1. 核心功能模块

### 1.1 配置项管理模块
- 配置项的增删改查 (CRUD)
- 支持关键字搜索
- 分页查询
- 软删除机制

### 1.2 历史版本管理模块
- 自动记录每次配置变更的历史快照
- 版本号自动递增
- 支持版本回退
- 版本对比功能

### 1.3 变更追踪模块
- 记录操作者信息
- 记录操作IP
- 记录变更原因

## 2. API 端点列表

| 序号 | HTTP方法 | 端点路径 | 功能描述 |
|------|----------|----------|----------|
| 1 | GET | `/api/v1/configs` | 获取配置列表(分页) |
| 2 | GET | `/api/v1/configs/{id}` | 获取配置详情 |
| 3 | POST | `/api/v1/configs` | 新增配置 |
| 4 | PUT | `/api/v1/configs/{id}` | 更新配置 |
| 5 | DELETE | `/api/v1/configs/{id}` | 删除配置(软删除) |
| 6 | GET | `/api/v1/configs/{id}/history` | 获取历史记录 |
| 7 | GET | `/api/v1/configs/{id}/history/{versionId}` | 获取历史版本详情 |
| 8 | POST | `/api/v1/configs/{id}/rollback/{versionId}` | 版本回退 |
| 9 | GET | `/api/v1/configs/{id}/diff` | 版本对比 |

## 3. 数据模型

### 3.1 ConfigItem (配置项实体)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键，自增 |
| configKey | String | 配置键，唯一标识 |
| configName | String | 配置名称 |
| configValue | String | 配置值 |
| description | String | 配置描述 |
| deleted | Integer | 逻辑删除标记 (0=正常, 1=已删除) |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

### 3.2 ConfigHistory (配置历史实体)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 主键，自增 |
| configId | Long | 关联配置项ID |
| versionNo | Integer | 版本号 |
| configValue | String | 配置值快照 |
| changeType | String | 变更类型(INIT/UPDATE/ROLLBACK) |
| operator | String | 操作者 |
| operatorIp | String | 操作者IP |
| changeReason | String | 变更原因 |
| createdAt | LocalDateTime | 创建时间 |

## 4. 业务逻辑流程

### 4.1 创建配置流程
```
接收 ConfigRequest → 创建 ConfigItem → 插入数据库 → 创建历史记录(INIT) → 返回响应
```

### 4.2 更新配置流程
```
接收请求 → 验证配置存在 → 查询最大版本号 → 保存当前值为历史(UPDATE) → 更新配置 → 返回响应
```

### 4.3 版本回退流程
```
接收请求 → 验证版本存在 → 保存当前值为历史(ROLLBACK) → 恢复目标版本值 → 返回响应
```

### 4.4 版本对比流程
```
接收请求 → 查询两个版本 → 解析JSON/文本 → 计算差异(ADD/MODIFY/DELETE) → 返回差异结果
```

## 5. 变更类型枚举

| 变更类型 | 说明 |
|----------|------|
| INIT | 初始化配置 |
| UPDATE | 更新配置 |
| ROLLBACK | 版本回退 |

## 6. 待优化功能

1. **批量操作** - 支持批量导入/导出配置
2. **配置加密** - 敏感配置值加密存储
3. **配置校验** - 支持配置值格式校验规则
4. **变更通知** - 配置变更时触发通知
5. **权限控制** - 基于角色的配置访问控制
6. **审计日志** - 完整的操作审计日志