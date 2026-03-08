package com.example.config.history.service;

import com.example.config.history.annotation.HistoryField;
import com.example.config.history.annotation.HistoryTrack;
import com.example.config.history.entity.GenericHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SnapshotService 单元测试
 * <p>
 * 测试快照生成和恢复功能的所有场景
 * </p>
 */
class SnapshotServiceTest {

    private SnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        snapshotService = new SnapshotService(new ObjectMapper());
    }

    // ==================== 测试实体类 ====================

    @HistoryTrack(entityName = "TestEntity", tableName = "test_entity")
    static class TestEntity {
        @HistoryField(displayName = "名称")
        private String name;

        @HistoryField(displayName = "值")
        private String value;

        @HistoryField(ignore = true)
        private String secret;

        public TestEntity() {}

        public TestEntity(String name, String value, String secret) {
            this.name = name;
            this.value = value;
            this.secret = secret;
        }
    }

    @HistoryTrack(entityName = "ComplexEntity", tableName = "complex_entity")
    static class ComplexEntity {
        @HistoryField(displayName = "ID")
        private Long id;

        @HistoryField(displayName = "配置键")
        private String configKey;

        @HistoryField(displayName = "配置值")
        private String configValue;

        @HistoryField(displayName = "描述")
        private String description;

        @HistoryField(ignore = true)
        private Integer deleted;

        @HistoryField(ignore = true)
        private LocalDateTime createdAt;

        public ComplexEntity() {}

        public ComplexEntity(Long id, String configKey, String configValue, String description) {
            this.id = id;
            this.configKey = configKey;
            this.configValue = configValue;
            this.description = description;
            this.deleted = 0;
            this.createdAt = LocalDateTime.now();
        }
    }

    static class NoAnnotationEntity {
        private String field1;
        private String field2;

        public NoAnnotationEntity(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }

    // ==================== 快照生成测试 ====================

    @Test
    @DisplayName("生成快照 - 正常场景：带注解实体")
    void snapshot_withAnnotation() {
        // Given
        TestEntity entity = new TestEntity("testName", "testValue", "secretValue");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("\"名称\":\"testName\""));
        assertTrue(snapshot.contains("\"值\":\"testValue\""));
        assertFalse(snapshot.contains("secret"));
        assertFalse(snapshot.contains("secretValue"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：复杂实体")
    void snapshot_complexEntity() {
        // Given
        ComplexEntity entity = new ComplexEntity(1L, "app.config", "{\"theme\":\"dark\"}", "应用配置");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("\"ID\":1"));
        assertTrue(snapshot.contains("\"配置键\":\"app.config\""));
        assertTrue(snapshot.contains("\"描述\":\"应用配置\""));
        assertFalse(snapshot.contains("deleted"));
        assertFalse(snapshot.contains("createdAt"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：无注解实体")
    void snapshot_noAnnotation() {
        // Given
        NoAnnotationEntity entity = new NoAnnotationEntity("value1", "value2");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("field1"));
        assertTrue(snapshot.contains("field2"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：字段值为null")
    void snapshot_nullFieldValue() {
        // Given
        TestEntity entity = new TestEntity(null, "value", null);

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("null"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：所有字段为null")
    void snapshot_allFieldsNull() {
        // Given
        TestEntity entity = new TestEntity(null, null, null);

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("null"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：空字符串值")
    void snapshot_emptyStringValue() {
        // Given
        TestEntity entity = new TestEntity("", "", "");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("\"\""));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：JSON格式值")
    void snapshot_jsonValue() {
        // Given
        ComplexEntity entity = new ComplexEntity(1L, "config", "{\"key\":\"value\",\"number\":123}", "desc");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("configValue"));
    }

    @Test
    @DisplayName("生成快照 - 正常场景：特殊字符")
    void snapshot_specialCharacters() {
        // Given
        TestEntity entity = new TestEntity("name\nwith\nnewlines", "value\"with\"quotes", "secret");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
    }

    @Test
    @DisplayName("生成快照 - 正常场景：中文字符")
    void snapshot_chineseCharacters() {
        // Given
        TestEntity entity = new TestEntity("中文名称", "中文值", "密钥");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("中文名称"));
        assertTrue(snapshot.contains("中文值"));
    }

    // ==================== 快照恢复测试 ====================

    @Test
    @DisplayName("恢复实体 - 正常场景：从JSON恢复")
    void restore_success() {
        // Given
        String snapshot = "{\"name\":\"testName\",\"value\":\"testValue\",\"secret\":\"secretValue\"}";

        // When
        TestEntity entity = snapshotService.restore(snapshot, TestEntity.class);

        // Then
        assertNotNull(entity);
        assertEquals("testName", entity.name);
        assertEquals("testValue", entity.value);
        assertEquals("secretValue", entity.secret);
    }

    @Test
    @DisplayName("恢复实体 - 正常场景：部分字段")
    void restore_partialFields() {
        // Given
        String snapshot = "{\"name\":\"onlyName\"}";

        // When
        TestEntity entity = snapshotService.restore(snapshot, TestEntity.class);

        // Then
        assertNotNull(entity);
        assertEquals("onlyName", entity.name);
        assertNull(entity.value);
        assertNull(entity.secret);
    }

    @Test
    @DisplayName("恢复实体 - 正常场景：空JSON")
    void restore_emptyJson() {
        // Given
        String snapshot = "{}";

        // When
        TestEntity entity = snapshotService.restore(snapshot, TestEntity.class);

        // Then
        assertNotNull(entity);
        assertNull(entity.name);
        assertNull(entity.value);
        assertNull(entity.secret);
    }

    @Test
    @DisplayName("恢复实体 - 异常场景：无效JSON")
    void restore_invalidJson() {
        // Given
        String snapshot = "invalid json";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            snapshotService.restore(snapshot, TestEntity.class);
        });
    }

    @Test
    @DisplayName("恢复实体 - 异常场景：null快照")
    void restore_nullSnapshot() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            snapshotService.restore(null, TestEntity.class);
        });
    }

    // ==================== 解析为Map测试 ====================

    @Test
    @DisplayName("解析为Map - 正常场景：有效JSON")
    void parseToMap_validJson() {
        // Given
        String snapshot = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：嵌套JSON")
    void parseToMap_nestedJson() {
        // Given
        String snapshot = "{\"outer\":{\"inner\":\"value\"}}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertTrue(map.containsKey("outer"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：数组值")
    void parseToMap_arrayValue() {
        // Given
        String snapshot = "{\"items\":[\"item1\",\"item2\",\"item3\"]}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertTrue(map.containsKey("items"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：数字值")
    void parseToMap_numericValue() {
        // Given
        String snapshot = "{\"count\":123,\"price\":99.99}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertEquals(123, map.get("count"));
        assertEquals(99.99, map.get("price"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：布尔值")
    void parseToMap_booleanValue() {
        // Given
        String snapshot = "{\"enabled\":true,\"disabled\":false}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertEquals(true, map.get("enabled"));
        assertEquals(false, map.get("disabled"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：非JSON字符串")
    void parseToMap_nonJsonString() {
        // Given
        String snapshot = "plain text";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("plain text", map.get("value"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：空字符串")
    void parseToMap_emptyString() {
        // Given
        String snapshot = "";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("", map.get("value"));
    }

    @Test
    @DisplayName("解析为Map - 正常场景：null值字段")
    void parseToMap_nullField() {
        // Given
        String snapshot = "{\"field\":null}";

        // When
        Map<String, Object> map = snapshotService.parseToMap(snapshot);

        // Then
        assertNotNull(map);
        assertNull(map.get("field"));
    }

    // ==================== 边界场景 ====================

    @Test
    @DisplayName("边界场景：超大JSON")
    void snapshot_largeJson() {
        // Given
        StringBuilder largeValue = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeValue.append("a");
        }
        TestEntity entity = new TestEntity("name", largeValue.toString(), "secret");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.length() > 10000);
    }

    @Test
    @DisplayName("边界场景：Unicode字符")
    void snapshot_unicodeCharacters() {
        // Given
        TestEntity entity = new TestEntity("日本語", "emoji🎉", "中文");

        // When
        String snapshot = snapshotService.snapshot(entity);

        // Then
        assertNotNull(snapshot);
        assertTrue(snapshot.contains("日本語"));
        assertTrue(snapshot.contains("emoji🎉"));
    }
}