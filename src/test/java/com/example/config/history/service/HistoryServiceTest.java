package com.example.config.history.service;

import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import com.example.config.history.common.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HistoryService 单元测试
 * <p>
 * 测试通用历史记录服务的所有功能场景和异常场景
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private GenericHistoryMapper genericHistoryMapper;

    @Mock
    private DiffCalculator diffCalculator;

    @InjectMocks
    private HistoryService historyService;

    private GenericHistory mockHistory;
    private final String ENTITY_TYPE = "ConfigItem";
    private final Long ENTITY_ID = 1L;
    private final Long VERSION_ID = 1L;

    @BeforeEach
    void setUp() {
        mockHistory = new GenericHistory();
        mockHistory.setId(VERSION_ID);
        mockHistory.setEntityType(ENTITY_TYPE);
        mockHistory.setEntityId(ENTITY_ID);
        mockHistory.setVersionNo(1);
        mockHistory.setSnapshot("{\"configKey\":\"test\",\"configValue\":\"value\"}");
        mockHistory.setChangeType("CREATE");
        mockHistory.setOperator("admin");
        mockHistory.setCreatedAt(LocalDateTime.now());
    }

    // ==================== 获取历史记录列表 ====================

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：返回分页数据")
    void getHistory_success() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result = historyService.getHistory(ENTITY_TYPE, ENTITY_ID, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
        verify(genericHistoryMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：空列表")
    void getHistory_emptyList() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result = historyService.getHistory(ENTITY_TYPE, ENTITY_ID, 1, 10);

        // Then
        assertNotNull(result);
        assertTrue(result.getRecords().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    @DisplayName("获取历史记录列表 - 正常场景：不同实体类型")
    void getHistory_differentEntityTypes() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result1 = historyService.getHistory("ConfigItem", 1L, 1, 10);
        PageResult<HistoryRecord> result2 = historyService.getHistory("User", 2L, 1, 10);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        verify(genericHistoryMapper, times(2)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    // ==================== 获取指定版本详情 ====================

    @Test
    @DisplayName("获取指定版本详情 - 正常场景：版本存在")
    void getVersion_success() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When
        HistoryRecord result = historyService.getVersion(ENTITY_TYPE, ENTITY_ID, VERSION_ID);

        // Then
        assertNotNull(result);
        assertEquals(VERSION_ID, result.getId());
        assertEquals(ENTITY_TYPE, result.getEntityType());
        assertEquals(ENTITY_ID, result.getEntityId());
        assertEquals(1, result.getVersionNo());
    }

    @Test
    @DisplayName("获取指定版本详情 - 异常场景：版本不存在")
    void getVersion_notFound() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.getVersion(ENTITY_TYPE, ENTITY_ID, VERSION_ID);
        });
        assertEquals("历史版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取指定版本详情 - 异常场景：实体类型不匹配")
    void getVersion_entityTypeMismatch() {
        // Given
        mockHistory.setEntityType("DifferentType");
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.getVersion(ENTITY_TYPE, ENTITY_ID, VERSION_ID);
        });
        assertEquals("历史版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("获取指定版本详情 - 异常场景：实体ID不匹配")
    void getVersion_entityIdMismatch() {
        // Given
        mockHistory.setEntityId(999L);
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.getVersion(ENTITY_TYPE, ENTITY_ID, VERSION_ID);
        });
        assertEquals("历史版本不存在", exception.getMessage());
    }

    // ==================== 版本对比 ====================

    @Test
    @DisplayName("版本对比 - 正常场景：两个版本存在差异")
    void compareVersions_withDifferences() {
        // Given
        GenericHistory v1 = new GenericHistory();
        v1.setId(1L);
        v1.setEntityType(ENTITY_TYPE);
        v1.setEntityId(ENTITY_ID);
        v1.setVersionNo(1);
        v1.setSnapshot("{\"theme\":\"light\"}");

        GenericHistory v2 = new GenericHistory();
        v2.setId(2L);
        v2.setEntityType(ENTITY_TYPE);
        v2.setEntityId(ENTITY_ID);
        v2.setVersionNo(2);
        v2.setSnapshot("{\"theme\":\"dark\"}");

        when(genericHistoryMapper.selectById(1L)).thenReturn(v1);
        when(genericHistoryMapper.selectById(2L)).thenReturn(v2);

        Map<String, DiffCalculator.DiffItem> diffResult = new HashMap<>();
        diffResult.put("theme", DiffCalculator.DiffItem.builder()
                .type("MODIFY").oldValue("light").newValue("dark").build());
        when(diffCalculator.calculate(anyString(), anyString())).thenReturn(diffResult);

        // When
        DiffResult result = historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 2L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getVersion1());
        assertEquals(2, result.getVersion2());
        assertNotNull(result.getDifferences());
        assertTrue(result.getDifferences().containsKey("theme"));
    }

    @Test
    @DisplayName("版本对比 - 正常场景：两个版本无差异")
    void compareVersions_noDifferences() {
        // Given
        GenericHistory v1 = new GenericHistory();
        v1.setId(1L);
        v1.setEntityType(ENTITY_TYPE);
        v1.setEntityId(ENTITY_ID);
        v1.setVersionNo(1);
        v1.setSnapshot("{\"theme\":\"light\"}");

        GenericHistory v2 = new GenericHistory();
        v2.setId(2L);
        v2.setEntityType(ENTITY_TYPE);
        v2.setEntityId(ENTITY_ID);
        v2.setVersionNo(2);
        v2.setSnapshot("{\"theme\":\"light\"}");

        when(genericHistoryMapper.selectById(1L)).thenReturn(v1);
        when(genericHistoryMapper.selectById(2L)).thenReturn(v2);
        when(diffCalculator.calculate(anyString(), anyString())).thenReturn(new HashMap<>());

        // When
        DiffResult result = historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 2L);

        // Then
        assertNotNull(result);
        assertTrue(result.getDifferences().isEmpty());
    }

    @Test
    @DisplayName("版本对比 - 异常场景：版本1不存在")
    void compareVersions_version1NotFound() {
        // Given
        when(genericHistoryMapper.selectById(1L)).thenReturn(null);
        when(genericHistoryMapper.selectById(2L)).thenReturn(mockHistory);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 2L);
        });
        assertEquals("历史版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("版本对比 - 异常场景：版本2不存在")
    void compareVersions_version2NotFound() {
        // Given
        when(genericHistoryMapper.selectById(1L)).thenReturn(mockHistory);
        when(genericHistoryMapper.selectById(2L)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 2L);
        });
        assertEquals("历史版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("版本对比 - 异常场景：版本与实体不匹配")
    void compareVersions_entityMismatch() {
        // Given
        GenericHistory v1 = new GenericHistory();
        v1.setId(1L);
        v1.setEntityType("DifferentType");
        v1.setEntityId(ENTITY_ID);
        v1.setVersionNo(1);
        v1.setSnapshot("{}");

        GenericHistory v2 = new GenericHistory();
        v2.setId(2L);
        v2.setEntityType(ENTITY_TYPE);
        v2.setEntityId(ENTITY_ID);
        v2.setVersionNo(2);
        v2.setSnapshot("{}");

        when(genericHistoryMapper.selectById(1L)).thenReturn(v1);
        when(genericHistoryMapper.selectById(2L)).thenReturn(v2);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 2L);
        });
        assertEquals("版本与实体不匹配", exception.getMessage());
    }

    @Test
    @DisplayName("版本对比 - 正常场景：对比同一版本")
    void compareVersions_sameVersion() {
        // Given
        when(genericHistoryMapper.selectById(1L)).thenReturn(mockHistory);

        Map<String, DiffCalculator.DiffItem> diffResult = new HashMap<>();
        when(diffCalculator.calculate(anyString(), anyString())).thenReturn(diffResult);

        // When
        DiffResult result = historyService.compareVersions(ENTITY_TYPE, ENTITY_ID, 1L, 1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getVersion1());
        assertEquals(1, result.getVersion2());
        assertTrue(result.getDifferences().isEmpty());
    }

    // ==================== 版本回退 ====================

    @Test
    @DisplayName("版本回退 - 正常场景：回退成功")
    void rollback_success() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When
        historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, "admin", "测试回退");

        // Then
        verify(genericHistoryMapper, times(1)).selectById(VERSION_ID);
    }

    @Test
    @DisplayName("版本回退 - 正常场景：带操作人和原因")
    void rollback_withOperatorAndReason() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When
        historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, "user1", "修复错误配置");

        // Then
        verify(genericHistoryMapper, times(1)).selectById(VERSION_ID);
    }

    @Test
    @DisplayName("版本回退 - 正常场景：操作人为空")
    void rollback_nullOperator() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When
        historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, null, null);

        // Then
        verify(genericHistoryMapper, times(1)).selectById(VERSION_ID);
    }

    @Test
    @DisplayName("版本回退 - 异常场景：目标版本不存在")
    void rollback_versionNotFound() {
        // Given
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, "admin", "回退");
        });
        assertEquals("目标版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("版本回退 - 异常场景：实体类型不匹配")
    void rollback_entityTypeMismatch() {
        // Given
        mockHistory.setEntityType("DifferentType");
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, "admin", "回退");
        });
        assertEquals("目标版本不存在", exception.getMessage());
    }

    @Test
    @DisplayName("版本回退 - 异常场景：实体ID不匹配")
    void rollback_entityIdMismatch() {
        // Given
        mockHistory.setEntityId(999L);
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.rollback(ENTITY_TYPE, ENTITY_ID, VERSION_ID, "admin", "回退");
        });
        assertEquals("目标版本不存在", exception.getMessage());
    }

    // ==================== 边界场景 ====================

    @Test
    @DisplayName("边界场景：大版本号")
    void getHistory_largeVersionNo() {
        // Given
        mockHistory.setVersionNo(Integer.MAX_VALUE);
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result = historyService.getHistory(ENTITY_TYPE, ENTITY_ID, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(Integer.MAX_VALUE, result.getRecords().get(0).getVersionNo());
    }

    @Test
    @DisplayName("边界场景：特殊字符实体类型")
    void getHistory_specialCharactersInEntityType() {
        // Given
        String specialType = "Config-Item_v2.0";
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result = historyService.getHistory(specialType, ENTITY_ID, 1, 10);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("边界场景：大JSON快照")
    void getVersion_largeSnapshot() {
        // Given
        StringBuilder largeJson = new StringBuilder("{\"data\":\"");
        for (int i = 0; i < 1000; i++) {
            largeJson.append("test");
        }
        largeJson.append("\"}");
        mockHistory.setSnapshot(largeJson.toString());
        when(genericHistoryMapper.selectById(VERSION_ID)).thenReturn(mockHistory);

        // When
        HistoryRecord result = historyService.getVersion(ENTITY_TYPE, ENTITY_ID, VERSION_ID);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSnapshot());
    }

    // ==================== 按时间范围查询 ====================

    @Test
    @DisplayName("按时间范围查询 - 正常场景：指定时间范围")
    void getHistoryByTimeRange_withTimeRange() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // When
        PageResult<HistoryRecord> result = historyService.getHistoryByTimeRange(
                ENTITY_TYPE, ENTITY_ID, startTime, endTime, "createdAt", "desc", 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(genericHistoryMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("按时间范围查询 - 正常场景：只指定开始时间")
    void getHistoryByTimeRange_onlyStartTime() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        LocalDateTime startTime = LocalDateTime.now().minusDays(1);

        // When
        PageResult<HistoryRecord> result = historyService.getHistoryByTimeRange(
                ENTITY_TYPE, ENTITY_ID, startTime, null, "createdAt", "desc", 1, 10);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("按时间范围查询 - 正常场景：只指定结束时间")
    void getHistoryByTimeRange_onlyEndTime() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        LocalDateTime endTime = LocalDateTime.now();

        // When
        PageResult<HistoryRecord> result = historyService.getHistoryByTimeRange(
                ENTITY_TYPE, ENTITY_ID, null, endTime, "createdAt", "desc", 1, 10);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("按时间范围查询 - 正常场景：按版本号升序排序")
    void getHistoryByTimeRange_sortByVersionAsc() {
        // Given
        Page<GenericHistory> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(mockHistory));
        pageResult.setTotal(1);

        when(genericHistoryMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        // When
        PageResult<HistoryRecord> result = historyService.getHistoryByTimeRange(
                ENTITY_TYPE, ENTITY_ID, null, null, "versionNo", "asc", 1, 10);

        // Then
        assertNotNull(result);
    }

    // ==================== 查询前N条记录 ====================

    @Test
    @DisplayName("查询前N条记录 - 正常场景：获取前5条")
    void getTopNByTime_top5() {
        // Given
        when(genericHistoryMapper.selectTopNByTime(ENTITY_TYPE, ENTITY_ID, 5, "DESC"))
                .thenReturn(Collections.singletonList(mockHistory));

        // When
        java.util.List<HistoryRecord> result = historyService.getTopNByTime(ENTITY_TYPE, ENTITY_ID, 5, "desc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(genericHistoryMapper, times(1)).selectTopNByTime(ENTITY_TYPE, ENTITY_ID, 5, "DESC");
    }

    @Test
    @DisplayName("查询前N条记录 - 正常场景：升序排序")
    void getTopNByTime_ascending() {
        // Given
        when(genericHistoryMapper.selectTopNByTime(ENTITY_TYPE, ENTITY_ID, 10, "ASC"))
                .thenReturn(Collections.singletonList(mockHistory));

        // When
        java.util.List<HistoryRecord> result = historyService.getTopNByTime(ENTITY_TYPE, ENTITY_ID, 10, "asc");

        // Then
        assertNotNull(result);
        verify(genericHistoryMapper, times(1)).selectTopNByTime(ENTITY_TYPE, ENTITY_ID, 10, "ASC");
    }

    @Test
    @DisplayName("查询前N条记录 - 正常场景：空列表")
    void getTopNByTime_emptyList() {
        // Given
        when(genericHistoryMapper.selectTopNByTime(ENTITY_TYPE, ENTITY_ID, 10, "DESC"))
                .thenReturn(Collections.emptyList());

        // When
        java.util.List<HistoryRecord> result = historyService.getTopNByTime(ENTITY_TYPE, ENTITY_ID, 10, "desc");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== 按时间点获取版本 ====================

    @Test
    @DisplayName("按时间点获取版本 - 正常场景：版本存在")
    void getVersionAtTime_success() {
        // Given
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);
        when(genericHistoryMapper.selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime))
                .thenReturn(mockHistory);

        // When
        HistoryRecord result = historyService.getVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime);

        // Then
        assertNotNull(result);
        assertEquals(VERSION_ID, result.getId());
        verify(genericHistoryMapper, times(1)).selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime);
    }

    @Test
    @DisplayName("按时间点获取版本 - 异常场景：指定时间点无记录")
    void getVersionAtTime_noRecord() {
        // Given
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);
        when(genericHistoryMapper.selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.getVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime);
        });
        assertEquals("指定时间点没有历史记录", exception.getMessage());
    }

    // ==================== 按时间回退 ====================

    @Test
    @DisplayName("按时间回退 - 正常场景：回退成功")
    void rollbackToTime_success() {
        // Given
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);
        when(genericHistoryMapper.selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime))
                .thenReturn(mockHistory);

        // When
        historyService.rollbackToTime(ENTITY_TYPE, ENTITY_ID, targetTime, "admin", "测试回退");

        // Then
        verify(genericHistoryMapper, times(1)).selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime);
    }

    @Test
    @DisplayName("按时间回退 - 异常场景：指定时间点无记录")
    void rollbackToTime_noRecord() {
        // Given
        LocalDateTime targetTime = LocalDateTime.now().minusHours(1);
        when(genericHistoryMapper.selectByVersionAtTime(ENTITY_TYPE, ENTITY_ID, targetTime))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historyService.rollbackToTime(ENTITY_TYPE, ENTITY_ID, targetTime, "admin", "回退");
        });
        assertEquals("指定时间点没有历史记录", exception.getMessage());
    }
}