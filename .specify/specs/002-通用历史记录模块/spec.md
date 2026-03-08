# 规格文档：通用历史记录管理模块

## 1. 需求概述

### 1.1 背景

当前项目是一个专用于配置项的历史记录管理模块，代码与 `ConfigItem` 实体强耦合。为了提高代码复用性，需要将其重构为**通用历史记录管理模块**，支持任意实体的变更历史追踪。

### 1.2 目标

- 将历史记录功能**泛型化**，支持任意实体类型
- 通过**注解驱动**方式标记需要记录历史的实体
- 使用 **AOP切面**实现无侵入式的历史记录
- 保持现有API兼容性，新增通用历史查询API

### 1.3 范围

**包含：**
- 通用历史记录框架核心代码
- 注解定义与AOP切面实现
- 通用历史记录API
- 配置项模块迁移示例

**不包含：**
- 前端界面开发
- 权限控制实现
- 消息通知功能

---

## 2. 功能需求

### 2.1 核心功能

| 功能编号 | 功能名称 | 说明 |
|----------|----------|------|
| F-001 | 注解标记 | 通过注解标记需要记录历史的实体和字段 |
| F-002 | 自动快照 | 实体变更时自动生成快照并保存 |
| F-003 | 版本管理 | 支持版本号自增、版本查询、版本对比 |
| F-004 | 版本回退 | 支持回退到任意历史版本 |
| F-005 | 通用API | 提供通用的历史记录查询API |

### 2.2 注解设计

```java
// 类级别注解：标记实体需要记录历史
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryTrack {
    String entityName();          // 实体名称
    String tableName();           // 对应表名
}

// 字段级别注解：标记需要追踪的字段
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryField {
    String displayName() default "";  // 字段显示名
    boolean ignore() default false;   // 是否忽略
}
```

### 2.3 通用历史表设计

```sql
CREATE TABLE generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type     VARCHAR(100) NOT NULL,    -- 实体类型（如 ConfigItem）
    entity_id       BIGINT NOT NULL,          -- 实体ID
    version_no      INT NOT NULL,             -- 版本号
    snapshot        CLOB NOT NULL,            -- 实体快照（JSON）
    change_type     VARCHAR(20) NOT NULL,     -- 变更类型
    change_fields   VARCHAR(500),             -- 变更字段列表
    operator        VARCHAR(100),             -- 操作人
    operator_ip     VARCHAR(50),              -- 操作IP
    change_reason   VARCHAR(500),             -- 变更原因
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_version (entity_type, entity_id, version_no)
);
```

### 2.4 API设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/history/{entityType}/{entityId}` | 获取实体历史记录 |
| GET | `/api/v1/history/{entityType}/{entityId}/{versionId}` | 获取指定版本 |
| GET | `/api/v1/history/{entityType}/{entityId}/diff` | 版本对比 |
| POST | `/api/v1/history/{entityType}/{entityId}/rollback/{versionId}` | 版本回退 |

---

## 3. 非功能需求

### 3.1 性能要求

- 历史记录保存：同步完成，响应时间 < 50ms
- 历史查询：分页查询响应时间 < 100ms
- 支持异步记录模式（可选配置）

### 3.2 可扩展性

- 支持自定义快照序列化策略
- 支持自定义变更检测策略
- 预留扩展点：事件监听、回调机制

### 3.3 兼容性

- 保持现有 `/api/v1/configs` API 不变
- 新增通用API与现有API并存

---

## 4. 约束条件

### 4.1 技术约束

- 必须基于 Spring Boot 3.x
- 必须使用 MyBatis-Plus 3.5.x
- JDK 17+

### 4.2 设计约束

- 不引入额外重量级框架
- 核心代码不依赖具体业务实体
- 保持代码简洁，避免过度设计

---

## 5. 验收标准

### 5.1 功能验收

- [ ] 注解可以正确标记实体
- [ ] 实体变更自动生成历史记录
- [ ] 历史查询API正常工作
- [ ] 版本对比正确计算差异
- [ ] 版本回退功能正常

### 5.2 质量验收

- [ ] 单元测试覆盖率 > 70%
- [ ] 集成测试通过
- [ ] API文档完整
- [ ] 代码通过静态检查