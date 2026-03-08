# 技术方案设计：通用历史记录管理模块

## 1. 整体架构

### 1.1 模块分层

```
┌─────────────────────────────────────────────────────────────┐
│                     API Layer (Controller)                   │
│  HistoryController - 通用历史记录API                         │
│  ConfigController - 配置管理API (保持现有)                    │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Service Layer                            │
│  HistoryService    - 通用历史服务 (新增)                      │
│  ConfigService     - 配置服务 (保持现有)                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Core Layer (新增)                        │
│  @HistoryTrack    - 实体标记注解                              │
│  @HistoryField    - 字段标记注解                              │
│  HistoryAspect    - AOP切面 (自动记录历史)                    │
│  HistoryRecorder  - 历史记录器                                │
│  SnapshotService  - 快照服务                                  │
│  DiffCalculator   - 差异计算器                                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer (Mapper)                      │
│  GenericHistoryMapper - 通用历史数据访问                      │
│  ConfigItemMapper     - 配置项数据访问                        │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 核心组件

| 组件 | 职责 | 类型 |
|------|------|------|
| `@HistoryTrack` | 标记需要记录历史的实体 | 注解 |
| `@HistoryField` | 标记需要追踪的字段 | 注解 |
| `HistoryAspect` | 拦截Service方法，自动记录历史 | AOP切面 |
| `HistoryRecorder` | 执行历史记录的核心逻辑 | 服务类 |
| `SnapshotService` | 生成实体快照JSON | 服务类 |
| `DiffCalculator` | 计算两个版本的差异 | 工具类 |
| `GenericHistoryMapper` | 通用历史数据CRUD | Mapper |

---

## 2. 详细设计

### 2.1 注解设计

```java
package com.example.config.history.annotation;

/**
 * 标记需要记录历史的实体类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryTrack {
    String entityName();           // 实体名称，如 "ConfigItem"
    String tableName();            // 表名，如 "config_item"
    String idField() default "id"; // 主键字段名
}

/**
 * 标记需要追踪变更的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryField {
    String displayName() default "";   // 字段显示名
    boolean ignore() default false;    // 是否忽略追踪
}
```

### 2.2 实体类示例

```java
@Data
@HistoryTrack(entityName = "ConfigItem", tableName = "config_item")
@TableName("config_item")
public class ConfigItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @HistoryField(displayName = "配置键")
    private String configKey;
    
    @HistoryField(displayName = "配置名称")
    private String configName;
    
    @HistoryField(displayName = "配置值")
    private String configValue;
    
    @HistoryField(displayName = "描述")
    private String description;
    
    @HistoryField(ignore = true)  // 不追踪
    private Integer deleted;
    
    @HistoryField(ignore = true)
    private LocalDateTime createdAt;
    
    @HistoryField(ignore = true)
    private LocalDateTime updatedAt;
}
```

### 2.3 AOP切面设计

```java
package com.example.config.history.aspect;

@Aspect
@Component
@RequiredArgsConstructor
public class HistoryAspect {
    
    private final HistoryRecorder historyRecorder;
    
    /**
     * 拦截所有Service的save/update/delete方法
     */
    @Around("execution(* com.example.config.service..*Service.*(..))")
    public Object recordHistory(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 解析方法参数，获取实体对象
        // 2. 检查实体是否有 @HistoryTrack 注解
        // 3. 执行原方法
        // 4. 记录历史（如果是创建/更新/删除操作）
        Object result = joinPoint.proceed();
        return result;
    }
}
```

### 2.4 核心服务设计

```java
package com.example.config.history.service;

/**
 * 历史记录器接口
 */
public interface HistoryRecorder {
    
    /**
     * 记录创建操作
     */
    <T> void recordCreate(T entity, String operator, String reason);
    
    /**
     * 记录更新操作
     */
    <T> void recordUpdate(T oldEntity, T newEntity, String operator, String reason);
    
    /**
     * 记录删除操作
     */
    <T> void recordDelete(T entity, String operator, String reason);
    
    /**
     * 记录回退操作
     */
    <T> void recordRollback(T entity, int targetVersion, String operator);
}

/**
 * 快照服务接口
 */
public interface SnapshotService {
    
    /**
     * 生成实体快照JSON
     */
    <T> String snapshot(T entity);
    
    /**
     * 从快照恢复实体
     */
    <T> T restore(String snapshot, Class<T> entityClass);
}

/**
 * 差异计算器接口
 */
public interface DiffCalculator {
    
    /**
     * 计算两个快照的差异
     */
    Map<String, DiffItem> calculate(String snapshot1, String snapshot2);
}
```

### 2.5 通用历史服务设计

```java
package com.example.config.history.service;

/**
 * 通用历史服务接口
 */
public interface HistoryService {
    
    /**
     * 获取实体的历史记录列表
     */
    PageResult<HistoryRecord> getHistory(String entityType, Long entityId, 
                                          Integer page, Integer pageSize);
    
    /**
     * 获取指定版本详情
     */
    HistoryRecord getVersion(String entityType, Long entityId, Long versionId);
    
    /**
     * 版本对比
     */
    DiffResult compareVersions(String entityType, Long entityId, 
                                Long version1, Long version2);
    
    /**
     * 版本回退
     */
    void rollback(String entityType, Long entityId, Long versionId, 
                  String operator, String reason);
}
```

---

## 3. 数据库设计

### 3.1 通用历史表

```sql
CREATE TABLE generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    entity_type     VARCHAR(100) NOT NULL COMMENT '实体类型',
    entity_id       BIGINT NOT NULL COMMENT '实体ID',
    version_no      INT NOT NULL COMMENT '版本号',
    snapshot        CLOB NOT NULL COMMENT '实体快照JSON',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型: CREATE/UPDATE/DELETE/ROLLBACK',
    change_fields   VARCHAR(500) COMMENT '变更字段列表',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_version (entity_type, entity_id, version_no),
    INDEX idx_created_at (created_at)
) COMMENT='通用历史记录表';
```

### 3.2 实体类型注册表（可选）

```sql
CREATE TABLE history_entity_registry (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type     VARCHAR(100) NOT NULL UNIQUE COMMENT '实体类型',
    entity_name     VARCHAR(200) NOT NULL COMMENT '实体名称',
    table_name      VARCHAR(100) NOT NULL COMMENT '对应表名',
    class_name      VARCHAR(255) NOT NULL COMMENT '完整类名',
    enabled         TINYINT DEFAULT 1 COMMENT '是否启用',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) COMMENT '历史记录实体注册表';
```

---

## 4. API设计

### 4.1 通用历史API

```yaml
# 获取历史记录列表
GET /api/v1/history/{entityType}/{entityId}
参数: page, pageSize
响应: PageResult<HistoryRecord>

# 获取指定版本
GET /api/v1/history/{entityType}/{entityId}/{versionId}
响应: HistoryRecord

# 版本对比
GET /api/v1/history/{entityType}/{entityId}/diff
参数: from, to (版本ID)
响应: DiffResult

# 版本回退
POST /api/v1/history/{entityType}/{entityId}/rollback/{versionId}
参数: operator, reason
响应: Result<Void>
```

### 4.2 响应DTO

```java
@Data
public class HistoryRecord {
    private Long id;
    private String entityType;
    private Long entityId;
    private Integer versionNo;
    private String snapshot;
    private String changeType;
    private List<String> changeFields;
    private String operator;
    private String operatorIp;
    private String changeReason;
    private LocalDateTime createdAt;
}

@Data
public class DiffResult {
    private Integer version1;
    private Integer version2;
    private String snapshot1;
    private String snapshot2;
    private Map<String, DiffItem> differences;
}

@Data
@Builder
public class DiffItem {
    private String type;      // ADD/MODIFY/DELETE
    private Object oldValue;
    private Object newValue;
    private String displayName;
}
```

---

## 5. 实现策略

### 5.1 阶段一：核心框架

1. 创建注解 `@HistoryTrack`、`@HistoryField`
2. 创建 `GenericHistory` 实体和Mapper
3. 实现 `SnapshotService` 快照服务
4. 实现 `DiffCalculator` 差异计算

### 5.2 阶段二：AOP集成

1. 实现 `HistoryAspect` 切面
2. 实现 `HistoryRecorder` 记录器
3. 实现 `HistoryService` 通用服务

### 5.3 阶段三：API与迁移

1. 创建 `HistoryController` 通用API
2. 迁移 `ConfigItem` 使用新框架
3. 更新测试用例

---

## 6. 目录结构

```
src/main/java/com/example/config/
├── ConfigHistoryApplication.java
├── common/                          # 公共类
│   ├── Result.java
│   ├── PageResult.java
│   └── exception/                   # 异常处理 (新增)
│       ├── GlobalExceptionHandler.java
│       └── BusinessException.java
├── history/                         # 通用历史模块 (新增)
│   ├── annotation/
│   │   ├── HistoryTrack.java
│   │   └── HistoryField.java
│   ├── aspect/
│   │   └── HistoryAspect.java
│   ├── entity/
│   │   └── GenericHistory.java
│   ├── mapper/
│   │   └── GenericHistoryMapper.java
│   ├── service/
│   │   ├── HistoryService.java
│   │   ├── HistoryRecorder.java
│   │   ├── SnapshotService.java
│   │   ├── DiffCalculator.java
│   │   └── impl/
│   │       ├── HistoryServiceImpl.java
│   │       ├── HistoryRecorderImpl.java
│   │       ├── SnapshotServiceImpl.java
│   │       └── DiffCalculatorImpl.java
│   ├── controller/
│   │   └── HistoryController.java
│   └── dto/
│       ├── HistoryRecord.java
│       └── DiffResult.java
├── config/                          # 配置模块 (保持现有)
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── entity/
│   └── dto/
└── ...
```

---

## 7. 技术选型

| 功能 | 技术方案 | 说明 |
|------|----------|------|
| 依赖注入 | Spring IoC | 构造器注入 |
| AOP | Spring AOP | 切面编程 |
| JSON序列化 | Jackson | ObjectMapper |
| ORM | MyBatis-Plus | BaseMapper |
| 分页 | MyBatis-Plus Page | 内置分页 |
| 参数校验 | Spring Validation | JSR-303 |

---

## 8. 风险与对策

| 风险 | 影响 | 对策 |
|------|------|------|
| AOP切面性能影响 | 中 | 支持异步记录，配置开关 |
| 快照JSON过大 | 低 | 压缩存储，字段过滤 |
| 实体变更检测复杂 | 中 | 反射+注解，缓存元数据 |