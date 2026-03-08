-- 配置历史管理模块 - 数据库建表SQL
-- 适用于 MySQL 8.0+

-- 配置项表
CREATE TABLE IF NOT EXISTS config_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_key      VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_name     VARCHAR(200) COMMENT '配置名称',
    config_value    JSON COMMENT '配置值',
    description     VARCHAR(500) COMMENT '描述',
    deleted         TINYINT DEFAULT 0 COMMENT '软删除标记：0-正常，1-删除',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_config_key (config_key),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置项表';

-- 配置历史表
CREATE TABLE IF NOT EXISTS config_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    config_id       BIGINT NOT NULL COMMENT '关联配置ID',
    version_no      INT NOT NULL COMMENT '版本号',
    config_value    JSON NOT NULL COMMENT '配置快照',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型：INIT-初始化，UPDATE-更新，ROLLBACK-回退',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_config_id_version (config_id, version_no),
    INDEX idx_config_id (config_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置历史表';

-- 通用历史记录表（history模块）
CREATE TABLE IF NOT EXISTS generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    entity_type     VARCHAR(100) NOT NULL COMMENT '实体类型',
    entity_id       BIGINT NOT NULL COMMENT '实体ID',
    version_no      INT NOT NULL COMMENT '版本号',
    snapshot        TEXT NOT NULL COMMENT '实体快照',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型：CREATE-创建，UPDATE-更新，DELETE-删除',
    change_fields   VARCHAR(500) COMMENT '变更字段列表，逗号分隔',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_entity_type_id (entity_type, entity_id),
    INDEX idx_entity_version (entity_type, entity_id, version_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用历史记录表';

-- 初始化一条示例配置
INSERT INTO config_item (config_key, config_name, config_value, description) 
VALUES ('app.settings', '应用设置', '{"theme": "light", "language": "zh-CN"}', '系统基础配置');

-- 初始化历史记录
INSERT INTO config_history (config_id, version_no, config_value, change_type, operator, change_reason)
SELECT id, 1, config_value, 'INIT', 'system', '初始化配置'
FROM config_item WHERE config_key = 'app.settings';
