package com.example.config.history.controller;

import com.example.config.history.common.PageResult;
import com.example.config.history.common.Result;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.service.DiffCalculator;
import com.example.config.history.service.HistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HistoryController 集成测试
 * <p>
 * 测试通用历史记录控制器的所有API场景
 * </p>
 */
@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistoryService historyService;

    @Autowired
    private ObjectMapper objectMapper;

    private HistoryRecord mockRecord;

    @BeforeEach
    void setUp() {
        mockRecord = new HistoryRecord();
        mockRecord.setId(1L);
        mockRecord.setEntityType("ConfigItem");
        mockRecord.setEntityId(1L);
        mockRecord.setVersionNo(1);
        mockRecord.setSnapshot("{\"key\":\"value\"}");
        mockRecord.setChangeType("CREATE");
        mockRecord.setOperator("admin");
        mockRecord.setChangeFields(Collections.emptyList());
        mockRecord.setCreatedAt(LocalDateTime.now());
    }

    // ==================== 获取历史记录列表 ====================

    @Test
    @DisplayName("获取历史记录列表 - 正常场景")
    void getHistory_success() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(
                Collections.singletonList(mockRecord),
                1L,
                1,
                10
        );
        when(historyService.getHistory("ConfigItem", 1L, 1, 10)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].entityType").value("ConfigItem"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：空列表")
    void getHistory_emptyList() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(
                Collections.emptyList(),
                0L,
                1,
                10
        );
        when(historyService.getHistory(anyString(), anyLong(), anyInt(), anyInt())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：默认分页参数")
    void getHistory_defaultPaging() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 10);
        when(historyService.getHistory("ConfigItem", 1L, 1, 10)).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：不同实体类型")
    void getHistory_differentEntityTypes() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 10);
        when(historyService.getHistory(anyString(), anyLong(), anyInt(), anyInt())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/User/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/history/Order/123"))
                .andExpect(status().isOk());
    }

    // ==================== 获取指定版本详情 ====================

    @Test
    @DisplayName("获取指定版本详情 - 正常场景")
    void getVersion_success() throws Exception {
        // Given
        when(historyService.getVersion("ConfigItem", 1L, 1L)).thenReturn(mockRecord);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.entityType").value("ConfigItem"));
    }

    @Test
    @DisplayName("获取指定版本详情 - 异常场景：版本不存在")
    void getVersion_notFound() throws Exception {
        // Given
        when(historyService.getVersion(anyString(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("历史版本不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 版本对比 ====================

    @Test
    @DisplayName("版本对比 - 正常场景：有差异")
    void compareVersions_withDifferences() throws Exception {
        // Given
        Map<String, DiffCalculator.DiffItem> diffItems = new HashMap<>();
        diffItems.put("theme", DiffCalculator.DiffItem.builder()
                .type("MODIFY").oldValue("light").newValue("dark").build());

        DiffResult diffResult = DiffResult.builder()
                .sourceVersion(1)
                .targetVersion(2)
                .sourceSnapshot("{\"theme\":\"light\"}")
                .targetSnapshot("{\"theme\":\"dark\"}")
                .differences(diffItems)
                .build();

        when(historyService.compareVersions("ConfigItem", 1L, 1L, 2L)).thenReturn(diffResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/diff")
                        .param("from", "1")
                        .param("to", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sourceVersion").value(1))
                .andExpect(jsonPath("$.data.targetVersion").value(2))
                .andExpect(jsonPath("$.data.differences.theme.type").value("MODIFY"));
    }

    @Test
    @DisplayName("版本对比 - 正常场景：无差异")
    void compareVersions_noDifferences() throws Exception {
        // Given
        DiffResult diffResult = DiffResult.builder()
                .sourceVersion(1)
                .targetVersion(2)
                .sourceSnapshot("{\"theme\":\"light\"}")
                .targetSnapshot("{\"theme\":\"light\"}")
                .differences(new HashMap<>())
                .build();

        when(historyService.compareVersions(anyString(), anyLong(), anyLong(), anyLong()))
                .thenReturn(diffResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/diff")
                        .param("from", "1")
                        .param("to", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.differences").isEmpty());
    }

    @Test
    @DisplayName("版本对比 - 异常场景：版本不存在")
    void compareVersions_versionNotFound() throws Exception {
        // Given
        when(historyService.compareVersions(anyString(), anyLong(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("历史版本不存在"));

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/diff")
                        .param("from", "1")
                        .param("to", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("版本对比 - 异常场景：缺少参数")
    void compareVersions_missingParams() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/diff"))
                .andExpect(status().isBadRequest());
    }

    // ==================== 版本回退 ====================

    @Test
    @DisplayName("版本回退 - 正常场景：带操作人和原因")
    void rollback_success() throws Exception {
        // Given
        doNothing().when(historyService).rollback(anyString(), anyLong(), anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/1/rollback/1")
                        .param("operator", "admin")
                        .param("reason", "修复错误配置"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("版本回退 - 正常场景：无操作人和原因")
    void rollback_withoutOperatorAndReason() throws Exception {
        // Given
        doNothing().when(historyService).rollback(anyString(), anyLong(), anyLong(), isNull(), isNull());

        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/1/rollback/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("版本回退 - 正常场景：仅操作人")
    void rollback_withOperatorOnly() throws Exception {
        // Given
        doNothing().when(historyService).rollback(anyString(), anyLong(), anyLong(), anyString(), isNull());

        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/1/rollback/1")
                        .param("operator", "user1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("版本回退 - 异常场景：版本不存在")
    void rollback_versionNotFound() throws Exception {
        // Given
        doThrow(new RuntimeException("目标版本不存在"))
                .when(historyService).rollback(anyString(), anyLong(), anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/1/rollback/999")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("版本回退 - 异常场景：实体不匹配")
    void rollback_entityMismatch() throws Exception {
        // Given
        doThrow(new RuntimeException("目标版本不存在"))
                .when(historyService).rollback(anyString(), anyLong(), anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/2/rollback/1")
                        .param("operator", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 边界场景 ====================

    @Test
    @DisplayName("边界场景：大实体ID")
    void getHistory_largeEntityId() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 10);
        when(historyService.getHistory(anyString(), anyLong(), anyInt(), anyInt())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/9223372036854775807"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("边界场景：特殊字符实体类型")
    void getHistory_specialCharactersInEntityType() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, 10);
        when(historyService.getHistory(anyString(), anyLong(), anyInt(), anyInt())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/Config-Item_v2.0/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("边界场景：分页参数边界值")
    void getHistory_pagingBoundary() throws Exception {
        // Given
        PageResult<HistoryRecord> pageResult = PageResult.of(Collections.emptyList(), 0L, 1, Integer.MAX_VALUE);
        when(historyService.getHistory(anyString(), anyLong(), anyInt(), anyInt())).thenReturn(pageResult);

        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1")
                        .param("page", "1")
                        .param("pageSize", "100"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("边界场景：不同HTTP方法")
    void invalidHttpMethod() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/history/ConfigItem/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("边界场景：无效路径")
    void invalidPath() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/history/"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("边界场景：缺少版本ID")
    void missingVersionId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/history/ConfigItem/1/"))
                .andExpect(status().isNotFound());
    }
}