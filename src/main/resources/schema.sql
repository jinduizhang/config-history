CREATE TABLE IF NOT EXISTS config_item (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key      VARCHAR(100) NOT NULL UNIQUE,
    config_name     VARCHAR(200),
    config_value    CLOB,
    description     VARCHAR(500),
    deleted         TINYINT DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS config_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_id       BIGINT NOT NULL,
    version_no      INT NOT NULL,
    config_value    CLOB NOT NULL,
    change_type     VARCHAR(20) NOT NULL,
    operator        VARCHAR(100),
    operator_ip     VARCHAR(50),
    change_reason   VARCHAR(500),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS generic_history (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       BIGINT NOT NULL,
    version_no      INT NOT NULL,
    snapshot        TEXT NOT NULL,
    change_type     VARCHAR(20) NOT NULL,
    change_fields   VARCHAR(500),
    operator        VARCHAR(100),
    operator_ip     VARCHAR(50),
    change_reason   VARCHAR(500),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_generic_history_entity ON generic_history(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_generic_history_version ON generic_history(entity_type, entity_id, version_no);

INSERT INTO config_item (config_key, config_name, config_value, description) 
VALUES ('app.settings', '应用设置', '{"theme": "light", "language": "zh-CN"}', '系统基础配置');

INSERT INTO config_history (config_id, version_no, config_value, change_type, operator, change_reason)
SELECT id, 1, config_value, 'INIT', 'system', '初始化配置'
FROM config_item WHERE config_key = 'app.settings';