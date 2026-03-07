# Plan - 配置历史管理技术方案

## 1. 技术选型

| 类别 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.2.x |
| ORM | MyBatis-Plus | 3.5.x |
| 数据库 | MySQL | 8.0+ |
| JSON处理 | Jackson | 内置 |
| API文档 | SpringDoc OpenAPI | 2.x |
| 单元测试 | JUnit 5 + Mockito | - |

## 2. 项目结构

```
config-history/
├── src/main/java/com/example/config/
│   ├── controller/
│   │   └── ConfigController.java      # API入口
│   ├── service/
│   │   ├── ConfigService.java         # 服务接口
│   │   └── impl/
│   │       └── ConfigServiceImpl.java
│   ├── mapper/
│   │   ├── ConfigItemMapper.java
│   │   └── ConfigHistoryMapper.java
│   ├── entity/
│   │   ├── ConfigItem.java
│   │   └── ConfigHistory.java
│   ├── dto/
│   │   ├── ConfigRequest.java
│   │   ├── ConfigResponse.java
│   │   └── DiffResponse.java
│   └── common/
│       ├── Result.java                # 统一响应
│       └── PageResult.java            # 分页响应
├── src/main/resources/
│   ├── mapper/*.xml
│   └── application.yml
└── src/test/java/...
```

## 3. 核心实现

### 3.1 配置更新与历史记录

```java
@Transactional(rollbackFor = Exception.class)
public void updateConfig(Long id, String newValue, String operator, String reason) {
    // 1. 获取当前配置
    ConfigItem item = configItemMapper.selectById(id);
    
    // 2. 插入历史记录（更新前）
    ConfigHistory history = new ConfigHistory();
    history.setConfigId(id);
    history.setVersionNo(getNextVersionNo(id));
    history.setConfigValue(item.getConfigValue());
    history.setChangeType("UPDATE");
    history.setOperator(operator);
    history.setChangeReason(reason);
    historyMapper.insert(history);
    
    // 3. 更新当前配置
    item.setConfigValue(newValue);
    configItemMapper.updateById(item);
}
```

### 3.2 版本回退

```java
@Transactional(rollbackFor = Exception.class)
public void rollback(Long configId, Long versionId, String operator) {
    // 1. 获取目标历史版本
    ConfigHistory targetVersion = configHistoryMapper.selectById(versionId);
    
    // 2. 记录当前版本到历史（可选，形成完整链）
    ConfigItem current = configItemMapper.selectById(configId);
    saveHistory(configId, current.getConfigValue(), "ROLLBACK", operator, "回退到v" + targetVersion.getVersionNo());
    
    // 3. 恢复到目标版本
    current.setConfigValue(targetVersion.getConfigValue());
    configItemMapper.updateById(current);
}
```

### 3.3 JSON Diff实现

使用Jackson或自定义递归对比：

```java
public DiffResponse compare(Long versionId1, Long versionId2) {
    ConfigHistory v1 = historyMapper.selectById(versionId1);
    ConfigHistory v2 = historyMapper.selectById(versionId2);
    
    Map<String, Object> diff = diffJson(
        parseJson(v1.getConfigValue()),
        parseJson(v2.getConfigValue())
    );
    
    return DiffResponse.builder()
        .version1(v1.getVersionNo())
        .version2(v2.getVersionNo())
        .differences(diff)
        .build();
}
```

## 4. 数据库设计

### 4.1 建表SQL

```sql
CREATE TABLE config_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_key      VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_name     VARCHAR(200) COMMENT '配置名称',
    config_value    JSON COMMENT '配置值',
    description     VARCHAR(500) COMMENT '描述',
    deleted         TINYINT DEFAULT 0 COMMENT '软删除标记',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_deleted (deleted)
) COMMENT '配置项表';

CREATE TABLE config_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_id       BIGINT NOT NULL COMMENT '关联配置ID',
    version_no      INT NOT NULL COMMENT '版本号',
    config_value    JSON NOT NULL COMMENT '配置快照',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_id_version (config_id, version_no),
    INDEX idx_config_id (config_id)
) COMMENT '配置历史表';
```

## 5. API响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}

// 错误响应
{
  "code": 400,
  "message": "参数错误",
  "data": null
}
```

## 6. 风险与对策

| 风险 | 对策 |
|------|------|
| JSON字段索引困难 | 仅对config_key等关键字段建索引，JSON内容不建索引 |
| 历史数据量大 | 定期归档或使用分区表 |
| 并发更新冲突 | 使用乐观锁（version字段）或分布式锁 |

## 7. 实施计划

详见 tasks.md 任务拆分
