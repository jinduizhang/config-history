-- =====================================================
-- History 独立模块 - 数据库初始化脚本
-- 适用于 MySQL 8.0+
-- =====================================================

-- 通用历史记录表
-- 支持任意实体的变更历史记录
CREATE TABLE IF NOT EXISTS generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    entity_type     VARCHAR(100) NOT NULL COMMENT '实体类型，如 ConfigItem, Order, User 等',
    entity_id       BIGINT NOT NULL COMMENT '实体ID',
    version_no      INT NOT NULL COMMENT '版本号，同一实体从1开始递增',
    snapshot        JSON NOT NULL COMMENT '实体快照，JSON格式存储完整实体数据',
    change_type     VARCHAR(20) NOT NULL COMMENT '变更类型：CREATE-创建，UPDATE-更新，DELETE-删除',
    change_fields   VARCHAR(500) COMMENT '变更字段列表，多个字段逗号分隔',
    operator        VARCHAR(100) COMMENT '操作人',
    operator_ip     VARCHAR(50) COMMENT '操作IP',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引设计说明：
    -- 1. idx_entity_type_id: 按实体查询历史记录（最常用）
    -- 2. idx_entity_version: 按实体+版本号快速定位
    -- 3. idx_created_at: 按时间范围查询（时间维度功能）
    INDEX idx_entity_type_id (entity_type, entity_id),
    INDEX idx_entity_version (entity_type, entity_id, version_no),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用历史记录表';

-- =====================================================
-- 使用说明
-- =====================================================
-- 
-- 1. 实体类型命名规范：
--    建议使用实体类名，如 ConfigItem, Order, User 等
--
-- 2. 版本号规则：
--    - 新实体创建时 version_no = 1
--    - 每次更新 version_no 递增
--    - 同一 entity_type + entity_id 组合唯一
--
-- 3. 变更类型说明：
--    - CREATE: 实体创建
--    - UPDATE: 实体更新
--    - DELETE: 实体删除
--
-- 4. 快照格式：
--    snapshot 字段存储实体的完整 JSON 数据
--    示例：{"id":1,"name":"test","value":"v1"}
--
-- 5. 变更字段：
--    change_fields 记录本次变更涉及的字段
--    示例：name,value
--
-- =====================================================
-- 示例数据
-- =====================================================

-- 插入示例历史记录（可选）
-- INSERT INTO generic_history 
--     (entity_type, entity_id, version_no, snapshot, change_type, change_fields, operator, change_reason)
-- VALUES 
--     ('ConfigItem', 1, 1, '{"id":1,"key":"app.theme","value":"light"}', 'CREATE', 'key,value', 'admin', '初始化主题配置');