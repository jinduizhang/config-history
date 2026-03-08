package com.example.config.history.service;

import com.example.config.history.service.impl.DiffCalculatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DiffCalculator 单元测试
 * <p>
 * 测试差异计算功能的所有场景
 * </p>
 */
class DiffCalculatorTest {

    private DiffCalculator diffCalculator;

    @BeforeEach
    void setUp() {
        diffCalculator = new DiffCalculatorImpl(new SnapshotService(new com.fasterxml.jackson.databind.ObjectMapper()));
    }

    // ==================== MODIFY 场景 ====================

    @Test
    @DisplayName("差异计算 - MODIFY：字段值变化")
    void calculate_modifyFieldValue() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"zh-CN\"}";
        String snapshot2 = "{\"theme\":\"dark\",\"language\":\"zh-CN\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertTrue(diff.containsKey("theme"));
        assertEquals("MODIFY", diff.get("theme").getType());
        assertEquals("light", diff.get("theme").getOldValue());
        assertEquals("dark", diff.get("theme").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - MODIFY：多字段变化")
    void calculate_modifyMultipleFields() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"zh-CN\",\"size\":12}";
        String snapshot2 = "{\"theme\":\"dark\",\"language\":\"en-US\",\"size\":14}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(3, diff.size());
        assertEquals("MODIFY", diff.get("theme").getType());
        assertEquals("MODIFY", diff.get("language").getType());
        assertEquals("MODIFY", diff.get("size").getType());
    }

    @Test
    @DisplayName("差异计算 - MODIFY：数字类型变化")
    void calculate_modifyNumericValue() {
        // Given
        String snapshot1 = "{\"count\":100}";
        String snapshot2 = "{\"count\":200}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("count").getType());
        assertEquals(100, diff.get("count").getOldValue());
        assertEquals(200, diff.get("count").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - MODIFY：布尔值变化")
    void calculate_modifyBooleanValue() {
        // Given
        String snapshot1 = "{\"enabled\":false}";
        String snapshot2 = "{\"enabled\":true}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("enabled").getType());
        assertEquals(false, diff.get("enabled").getOldValue());
        assertEquals(true, diff.get("enabled").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - MODIFY：null变为有值")
    void calculate_modifyNullToValue() {
        // Given
        String snapshot1 = "{\"field\":null}";
        String snapshot2 = "{\"field\":\"value\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("field").getType());
        assertNull(diff.get("field").getOldValue());
        assertEquals("value", diff.get("field").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - MODIFY：有值变为null")
    void calculate_modifyValueToNull() {
        // Given
        String snapshot1 = "{\"field\":\"value\"}";
        String snapshot2 = "{\"field\":null}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("field").getType());
        assertEquals("value", diff.get("field").getOldValue());
        assertNull(diff.get("field").getNewValue());
    }

    // ==================== ADD 场景 ====================

    @Test
    @DisplayName("差异计算 - ADD：新增字段")
    void calculate_addField() {
        // Given
        String snapshot1 = "{\"theme\":\"light\"}";
        String snapshot2 = "{\"theme\":\"light\",\"language\":\"en-US\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("ADD", diff.get("language").getType());
        assertNull(diff.get("language").getOldValue());
        assertEquals("en-US", diff.get("language").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - ADD：新增多个字段")
    void calculate_addMultipleFields() {
        // Given
        String snapshot1 = "{\"field1\":\"value1\"}";
        String snapshot2 = "{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":\"value3\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(2, diff.size());
        assertEquals("ADD", diff.get("field2").getType());
        assertEquals("ADD", diff.get("field3").getType());
    }

    @Test
    @DisplayName("差异计算 - ADD：从空JSON添加字段")
    void calculate_addToEmptyJson() {
        // Given
        String snapshot1 = "{}";
        String snapshot2 = "{\"newField\":\"newValue\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("ADD", diff.get("newField").getType());
    }

    // ==================== DELETE 场景 ====================

    @Test
    @DisplayName("差异计算 - DELETE：删除字段")
    void calculate_deleteField() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"en-US\"}";
        String snapshot2 = "{\"theme\":\"light\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("DELETE", diff.get("language").getType());
        assertEquals("en-US", diff.get("language").getOldValue());
        assertNull(diff.get("language").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - DELETE：删除多个字段")
    void calculate_deleteMultipleFields() {
        // Given
        String snapshot1 = "{\"field1\":\"value1\",\"field2\":\"value2\",\"field3\":\"value3\"}";
        String snapshot2 = "{\"field1\":\"value1\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(2, diff.size());
        assertEquals("DELETE", diff.get("field2").getType());
        assertEquals("DELETE", diff.get("field3").getType());
    }

    @Test
    @DisplayName("差异计算 - DELETE：删除到空JSON")
    void calculate_deleteToEmpty() {
        // Given
        String snapshot1 = "{\"field\":\"value\"}";
        String snapshot2 = "{}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("DELETE", diff.get("field").getType());
    }

    // ==================== 无差异场景 ====================

    @Test
    @DisplayName("差异计算 - 无差异：相同JSON")
    void calculate_noDifference() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"zh-CN\"}";
        String snapshot2 = "{\"theme\":\"light\",\"language\":\"zh-CN\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertTrue(diff.isEmpty());
    }

    @Test
    @DisplayName("差异计算 - 无差异：两个空JSON")
    void calculate_twoEmptyJsons() {
        // Given
        String snapshot1 = "{}";
        String snapshot2 = "{}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertTrue(diff.isEmpty());
    }

    @Test
    @DisplayName("差异计算 - 无差异：字段顺序不同")
    void calculate_differentOrder() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"zh-CN\"}";
        String snapshot2 = "{\"language\":\"zh-CN\",\"theme\":\"light\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertTrue(diff.isEmpty());
    }

    // ==================== 混合场景 ====================

    @Test
    @DisplayName("差异计算 - 混合：ADD/MODIFY/DELETE同时存在")
    void calculate_mixedChanges() {
        // Given
        String snapshot1 = "{\"theme\":\"light\",\"language\":\"zh-CN\",\"oldField\":\"deleted\"}";
        String snapshot2 = "{\"theme\":\"dark\",\"language\":\"zh-CN\",\"newField\":\"added\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(3, diff.size());
        assertEquals("MODIFY", diff.get("theme").getType());
        assertEquals("DELETE", diff.get("oldField").getType());
        assertEquals("ADD", diff.get("newField").getType());
    }

    // ==================== 非JSON场景 ====================

    @Test
    @DisplayName("差异计算 - 非JSON：纯文本对比")
    void calculate_plainText() {
        // Given
        String snapshot1 = "plain text 1";
        String snapshot2 = "plain text 2";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("value").getType());
        assertEquals("plain text 1", diff.get("value").getOldValue());
        assertEquals("plain text 2", diff.get("value").getNewValue());
    }

    @Test
    @DisplayName("差异计算 - 非JSON：一个JSON一个纯文本")
    void calculate_oneJsonOneText() {
        // Given
        String snapshot1 = "{\"key\":\"value\"}";
        String snapshot2 = "not json";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertNotNull(diff);
    }

    // ==================== 边界场景 ====================

    @Test
    @DisplayName("差异计算 - 边界：null快照")
    void calculate_nullSnapshots() {
        // When & Then
        assertThrows(Exception.class, () -> {
            diffCalculator.calculate(null, null);
        });
    }

    @Test
    @DisplayName("差异计算 - 边界：嵌套JSON")
    void calculate_nestedJson() {
        // Given
        String snapshot1 = "{\"config\":{\"theme\":\"light\"}}";
        String snapshot2 = "{\"config\":{\"theme\":\"dark\"}}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertNotNull(diff);
    }

    @Test
    @DisplayName("差异计算 - 边界：数组字段")
    void calculate_arrayField() {
        // Given
        String snapshot1 = "{\"items\":[1,2,3]}";
        String snapshot2 = "{\"items\":[1,2,3,4]}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertNotNull(diff);
    }

    @Test
    @DisplayName("差异计算 - 边界：特殊字符")
    void calculate_specialCharacters() {
        // Given
        String snapshot1 = "{\"text\":\"hello\\nworld\"}";
        String snapshot2 = "{\"text\":\"hello\\nworld\\nnew\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertNotNull(diff);
    }

    @Test
    @DisplayName("差异计算 - 边界：Unicode字符")
    void calculate_unicodeCharacters() {
        // Given
        String snapshot1 = "{\"text\":\"你好\"}";
        String snapshot2 = "{\"text\":\"世界\"}";

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(snapshot1, snapshot2);

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("text").getType());
    }

    @Test
    @DisplayName("差异计算 - 边界：超大JSON")
    void calculate_largeJson() {
        // Given
        StringBuilder sb1 = new StringBuilder("{\"data\":\"");
        StringBuilder sb2 = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 1000; i++) {
            sb1.append("a");
            sb2.append("b");
        }
        sb1.append("\"}");
        sb2.append("\"}");

        // When
        Map<String, DiffCalculator.DiffItem> diff = diffCalculator.calculate(sb1.toString(), sb2.toString());

        // Then
        assertEquals(1, diff.size());
        assertEquals("MODIFY", diff.get("data").getType());
    }
}