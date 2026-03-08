# 通用历史记录管理模块 - 架构设计说明书

## 1. 文档信息

| 项目 | 内容 |
|------|------|
| 项目名称 | 通用历史记录管理模块 (Generic History Audit Module) |
| 版本 | 1.0.0 |
| 文档状态 | 正式发布 |
| 编写日期 | 2026-03-08 |

---

## 2. 概述

### 2.1 项目背景

在企业管理系统中，数据变更的历史追溯是审计合规的重要组成部分。传统的实现方式是为每个业务模块单独开发历史记录功能，导致代码重复、维护困难。

本模块旨在提供一套**通用的历史记录框架**，通过注解驱动的方式，实现任意实体类型的变更历史追踪，降低开发成本，提高代码复用性。

### 2.2 设计目标

| 目标 | 说明 |
|------|------|
| 通用性 | 支持任意实体类型的历史记录 |
| 低侵入 | 业务代码无需感知历史记录逻辑 |
| 可扩展 | 支持自定义快照策略、差异计算 |
| 易集成 | 通过注解即可启用历史追踪 |

### 2.3 适用范围

- 配置管理系统的版本控制
- 审批流程的历史记录
- 数据变更审计
- 任何需要追溯变更历史的业务场景

---

## 3. 系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                           应用层 (Application)                        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │ ConfigController│  │HistoryController│  │  其他业务Controller  │  │
│  │  (配置管理API)   │  │  (通用历史API)   │  │                     │  │
│  └────────┬────────┘  └────────┬────────┘  └─────────────────────┘  │
└───────────┼────────────────────┼─────────────────────────────────────┘
            │                    │
            ▼                    ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           服务层 (Service)                           │
│  ┌─────────────────┐  ┌─────────────────────────────────────────┐   │
│  │  ConfigService  │  │            HistoryService               │   │
│  │  (配置业务逻辑)  │  │         (通用历史查询服务)               │   │
│  └────────┬────────┘  └──────────────────┬──────────────────────┘   │
│           │                               │                          │
│           │    ┌─────────────────────────────────────────────────┐  │
│           │    │              HistoryRecorder                     │  │
│           │    │           (历史记录核心服务)                      │  │
│           │    └──────────────────┬──────────────────────────────┘  │
│           │                       │                                 │
│           │    ┌──────────────────┼──────────────────┐              │
│           │    ▼                  ▼                  ▼              │
│           │ ┌──────────┐   ┌──────────────┐   ┌──────────────┐      │
│           │ │Snapshot  │   │DiffCalculator│   │HistoryAspect │      │
│           │ │Service   │   │              │   │  (AOP切面)   │      │
│           │ │(快照服务) │   │(差异计算器)  │   └──────────────┘      │
│           │ └──────────┘   └──────────────┘                         │
└───────────┼─────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         数据访问层 (Mapper)                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │ConfigItemMapper │  │ConfigHistory    │  │GenericHistoryMapper │  │
│  │                 │  │    Mapper       │  │   (通用历史Mapper)   │  │
│  └────────┬────────┘  └────────┬────────┘  └──────────┬──────────┘  │
└───────────┼────────────────────┼──────────────────────┼─────────────┘
            │                    │                      │
            ▼                    ▼                      ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           数据层 (Database)                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │
│  │  config_item    │  │ config_history  │  │  generic_history    │  │
│  │   (配置项表)     │  │  (配置历史表)   │  │   (通用历史表)       │  │
│  └─────────────────┘  └─────────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 3.2 模块划分

| 模块 | 包路径 | 职责 |
|------|--------|------|
| 注解模块 | `history.annotation` | 定义历史追踪相关注解 |
| 切面模块 | `history.aspect` | AOP切面，自动拦截并记录历史 |
| 控制器模块 | `history.controller` | 提供通用历史查询REST API |
| 服务模块 | `history.service` | 核心业务逻辑实现 |
| 实体模块 | `history.entity` | 数据库实体映射 |
| 数据访问模块 | `history.mapper` | 数据库CRUD操作 |
| DTO模块 | `history.dto` | 数据传输对象 |
| 异常模块 | `common.exception` | 全局异常处理 |

---

## 4. 核心组件设计

### 4.1 注解设计

#### 4.1.1 @HistoryTrack

**用途**: 标记需要记录历史的实体类

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryTrack {
    String entityName();           // 实体名称，如 "ConfigItem"
    String tableName();            // 对应表名，如 "config_item"
    String idField() default "id"; // 主键字段名
}
```

**使用示例**:
```java
@Data
@TableName("config_item")
@HistoryTrack(entityName = "ConfigItem", tableName = "config_item")
public class ConfigItem {
    // ...
}
```

#### 4.1.2 @HistoryField

**用途**: 标记需要追踪的字段

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryField {
    String displayName() default "";   // 字段显示名
    boolean ignore() default false;    // 是否忽略追踪
}
```

**使用示例**:
```java
@HistoryField(displayName = "配置键")
private String configKey;

@HistoryField(ignore = true)  // 不追踪此字段
private LocalDateTime updatedAt;
```

### 4.2 核心服务设计

#### 4.2.1 SnapshotService (快照服务)

**职责**: 生成和恢复实体快照

```java
public interface SnapshotService {
    // 生成实体快照JSON
    <T> String snapshot(T entity);
    
    // 从快照恢复实体
    <T> T restore(String snapshot, Class<T> entityClass);
    
    // 解析快照为Map
    Map<String, Object> parseToMap(String snapshot);
}
```

**实现原理**:
1. 通过反射获取实体所有字段
2. 读取 `@HistoryField` 注解配置
3. 将字段值序列化为JSON

#### 4.2.2 DiffCalculator (差异计算器)

**职责**: 计算两个版本的差异

```java
public interface DiffCalculator {
    Map<String, DiffItem> calculate(String snapshot1, String snapshot2);
}
```

**差异类型**:
| 类型 | 说明 |
|------|------|
| ADD | 新版本新增的字段 |
| MODIFY | 值发生变化的字段 |
| DELETE | 新版本删除的字段 |

#### 4.2.3 HistoryRecorder (历史记录器)

**职责**: 执行历史记录的核心逻辑

```java
public interface HistoryRecorder {
    // 记录创建操作
    <T> void recordCreate(T entity, String operator, String reason);
    
    // 记录更新操作
    <T> void recordUpdate(T oldEntity, T newEntity, String operator, String reason);
    
    // 记录删除操作
    <T> void recordDelete(T entity, String operator, String reason);
    
    // 记录回退操作
    <T> void recordRollback(T entity, int targetVersion, String operator);
}
```

#### 4.2.4 HistoryService (历史查询服务)

**职责**: 提供历史查询、对比、回退功能

```java
public interface HistoryService {
    // 获取实体历史记录列表
    PageResult<HistoryRecord> getHistory(String entityType, Long entityId, 
                                          Integer page, Integer pageSize);
    
    // 获取指定版本详情
    HistoryRecord getVersion(String entityType, Long entityId, Long versionId);
    
    // 版本对比
    DiffResult compareVersions(String entityType, Long entityId, 
                                Long version1, Long version2);
    
    // 版本回退
    void rollback(String entityType, Long entityId, Long versionId, 
                  String operator, String reason);
}
```

### 4.3 AOP切面设计

#### HistoryAspect

**职责**: 自动拦截Service方法，记录历史

**切点定义**:
```java
// 创建操作
@Around("execution(* com.example.config.service..*Service.create*(..)) || " +
        "execution(* com.example.config.service..*Service.save*(..)) || " +
        "execution(* com.example.config.service..*Service.insert*(..))")

// 更新操作
@Around("execution(* com.example.config.service..*Service.update*(..)) || " +
        "execution(* com.example.config.service..*Service.modify*(..))")

// 删除操作
@Around("execution(* com.example.config.service..*Service.delete*(..)) || " +
        "execution(* com.example.config.service..*Service.remove*(..))")
```

---

## 5. 数据模型设计

### 5.1 通用历史表 (generic_history)

```sql
CREATE TABLE generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type     VARCHAR(100) NOT NULL COMMENT '实体类型',
    entity_id       BIGINT NOT NULL COMMENT '实体ID',
    version_no      INT NOT NULL COMMENT '版本号',
    snapshot        CLOB NOT NULL COMMENT '实体快照JSON',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型',
    change_fields   VARCHAR(500) COMMENT '变更字段列表',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_version (entity_type, entity_id, version_no)
);
```

### 5.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT | 是 | 自增主键 |
| entity_type | VARCHAR(100) | 是 | 实体类型，如 `ConfigItem` |
| entity_id | BIGINT | 是 | 实体ID，关联业务表主键 |
| version_no | INT | 是 | 版本号，从1开始递增 |
| snapshot | CLOB | 是 | 实体快照，JSON格式 |
| change_type | VARCHAR(20) | 是 | 变更类型：CREATE/UPDATE/DELETE/ROLLBACK |
| change_fields | VARCHAR(500) | 否 | 变更字段列表，逗号分隔 |
| operator | VARCHAR(100) | 否 | 操作人 |
| operator_ip | VARCHAR(50) | 否 | 操作IP |
| change_reason | VARCHAR(500) | 否 | 变更原因 |
| created_at | TIMESTAMP | 是 | 创建时间 |

### 5.3 索引设计

| 索引名 | 字段 | 说明 |
|--------|------|------|
| PRIMARY | id | 主键索引 |
| idx_entity | entity_type, entity_id | 按实体查询历史 |
| idx_version | entity_type, entity_id, version_no | 按版本号查询 |

---

## 6. API设计

### 6.1 API概览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/history/{entityType}/{entityId}` | 获取历史记录列表 |
| GET | `/api/v1/history/{entityType}/{entityId}/{versionId}` | 获取指定版本详情 |
| GET | `/api/v1/history/{entityType}/{entityId}/diff` | 版本对比 |
| POST | `/api/v1/history/{entityType}/{entityId}/rollback/{versionId}` | 版本回退 |

### 6.2 统一响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {}
}
```

### 6.3 分页响应格式

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [],
        "total": 100,
        "page": 1,
        "pageSize": 10
    }
}
```

---

## 7. 技术选型

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行环境 |
| Spring Boot | 3.2.0 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM框架 |
| Spring AOP | - | 切面编程 |
| Jackson | - | JSON序列化 |
| Lombok | - | 代码简化 |
| SpringDoc | 2.3.0 | API文档 |
| H2 Database | - | 内存数据库(开发) |
| MySQL | 8.x | 生产数据库 |

---

## 8. 扩展设计

### 8.1 自定义快照策略

可通过实现 `SnapshotService` 接口自定义快照生成逻辑：

```java
public class CustomSnapshotService implements SnapshotService {
    @Override
    public <T> String snapshot(T entity) {
        // 自定义序列化逻辑
        // 例如：加密敏感字段、压缩大对象等
    }
}
```

### 8.2 异步记录

可通过 `@Async` 实现异步历史记录，提升性能：

```java
@Async
public <T> void recordCreate(T entity, String operator, String reason) {
    // 异步记录
}
```

### 8.3 事件监听

可发布事件支持扩展：

```java
public class HistoryRecordedEvent {
    private String entityType;
    private Long entityId;
    private Integer versionNo;
    // ...
}

// 监听事件
@EventListener
public void onHistoryRecorded(HistoryRecordedEvent event) {
    // 发送通知、触发其他操作等
}
```

---

## 9. 性能考虑

### 9.1 索引优化

- `entity_type` + `entity_id` 复合索引支持快速查询
- `version_no` 索引支持版本号查询

### 9.2 快照压缩

对于大对象，建议：
1. 使用 `@HistoryField(ignore=true)` 忽略不必要字段
2. 实现自定义快照策略，进行压缩

### 9.3 分页查询

所有历史查询均支持分页，避免大数据量查询

---

## 10. 安全考虑

### 10.1 敏感字段

使用 `@HistoryField(ignore=true)` 排除敏感字段：

```java
@HistoryField(ignore = true)
private String password;
```

### 10.2 操作审计

记录操作人和操作IP，支持审计追踪

---

## 11. 部署架构

```
┌─────────────────────────────────────────────────────────┐
│                    负载均衡器                            │
└─────────────────────────┬───────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│  App Instance │ │  App Instance │ │  App Instance │
│      (1)      │ │      (2)      │ │      (3)      │
└───────┬───────┘ └───────┬───────┘ └───────┬───────┘
        │                 │                 │
        └─────────────────┼─────────────────┘
                          │
                          ▼
              ┌───────────────────────┐
              │     MySQL Cluster     │
              │   (generic_history)   │
              └───────────────────────┘
```

---

## 12. 附录

### 12.1 变更类型枚举

| 类型 | 说明 | 触发场景 |
|------|------|----------|
| CREATE | 创建 | 新增实体 |
| UPDATE | 更新 | 修改实体 |
| DELETE | 删除 | 删除实体 |
| ROLLBACK | 回退 | 版本回退 |

### 12.2 版本号规则

- 版本号从 1 开始
- 每次变更版本号 +1
- 回退操作生成新版本，不修改历史版本

### 12.3 术语表

| 术语 | 说明 |
|------|------|
| 快照 (Snapshot) | 实体在某一时刻的完整状态 |
| 差异 (Diff) | 两个版本之间的变化 |
| 回退 (Rollback) | 恢复到指定历史版本 |
| 审计 (Audit) | 记录操作历史用于追溯 |