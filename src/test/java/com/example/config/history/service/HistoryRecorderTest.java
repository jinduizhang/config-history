package com.example.config.history.service;

import com.example.config.history.annotation.HistoryTrack;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import com.example.config.history.service.impl.HistoryRecorderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * HistoryRecorder 单元测试
 */
@ExtendWith(MockitoExtension.class)
class HistoryRecorderTest {

    @Mock
    private GenericHistoryMapper genericHistoryMapper;

    @Mock
    private SnapshotService snapshotService;

    @InjectMocks
    private HistoryRecorderImpl historyRecorder;

    private TestEntity testEntity;

    @HistoryTrack(entityName = "TestEntity", tableName = "test_entity", idField = "id")
    static class TestEntity {
        private Long id;
        private String name;
        private String value;

        public TestEntity(Long id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @BeforeEach
    void setUp() {
        testEntity = new TestEntity(1L, "test", "value");
    }

    @Test
    @DisplayName("记录创建操作 - 正常场景")
    void recordCreate_success() {
        when(genericHistoryMapper.selectMaxVersionNo("TestEntity", 1L)).thenReturn(0);
        when(snapshotService.snapshot(any())).thenReturn("{\"id\":1,\"name\":\"test\"}");
        doReturn(1).when(genericHistoryMapper).insert(any(GenericHistory.class));

        historyRecorder.recordCreate(testEntity, "admin", "创建测试");

        verify(genericHistoryMapper, times(1)).selectMaxVersionNo("TestEntity", 1L);
        verify(genericHistoryMapper, times(1)).insert(any(GenericHistory.class));
    }

    @Test
    @DisplayName("记录更新操作 - 正常场景")
    void recordUpdate_success() {
        TestEntity oldEntity = new TestEntity(1L, "old", "oldValue");
        TestEntity newEntity = new TestEntity(1L, "new", "newValue");

        when(genericHistoryMapper.selectMaxVersionNo("TestEntity", 1L)).thenReturn(1);
        when(snapshotService.snapshot(any())).thenReturn("{\"id\":1,\"name\":\"new\"}");
        doReturn(1).when(genericHistoryMapper).insert(any(GenericHistory.class));

        historyRecorder.recordUpdate(oldEntity, newEntity, "admin", "更新测试");

        verify(genericHistoryMapper, times(1)).insert(any(GenericHistory.class));
    }

    @Test
    @DisplayName("记录删除操作 - 正常场景")
    void recordDelete_success() {
        when(genericHistoryMapper.selectMaxVersionNo("TestEntity", 1L)).thenReturn(1);
        when(snapshotService.snapshot(any())).thenReturn("{\"id\":1,\"name\":\"test\"}");
        doReturn(1).when(genericHistoryMapper).insert(any(GenericHistory.class));

        historyRecorder.recordDelete(testEntity, "admin", "删除测试");

        verify(genericHistoryMapper, times(1)).insert(any(GenericHistory.class));
    }

    @Test
    @DisplayName("记录回退操作 - 正常场景")
    void recordRollback_success() {
        when(genericHistoryMapper.selectMaxVersionNo("TestEntity", 1L)).thenReturn(2);
        when(snapshotService.snapshot(any())).thenReturn("{\"id\":1,\"name\":\"test\"}");
        doReturn(1).when(genericHistoryMapper).insert(any(GenericHistory.class));

        historyRecorder.recordRollback(testEntity, 1, "admin");

        verify(genericHistoryMapper, times(1)).insert(any(GenericHistory.class));
    }

    @Test
    @DisplayName("版本号递增 - 验证版本号正确递增")
    void recordCreate_versionIncrement() {
        when(genericHistoryMapper.selectMaxVersionNo("TestEntity", 1L)).thenReturn(5);
        when(snapshotService.snapshot(any())).thenReturn("{}");
        doAnswer(invocation -> {
            GenericHistory history = invocation.getArgument(0);
            assertEquals(6, history.getVersionNo());
            return 1;
        }).when(genericHistoryMapper).insert(any(GenericHistory.class));

        historyRecorder.recordCreate(testEntity, "admin", "测试");

        verify(genericHistoryMapper, times(1)).insert(any(GenericHistory.class));
    }

    @Test
    @DisplayName("无注解实体 - 不记录历史")
    void recordCreate_noAnnotation() {
        Object entityWithoutAnnotation = new Object();

        historyRecorder.recordCreate(entityWithoutAnnotation, "admin", "测试");

        verify(genericHistoryMapper, never()).selectMaxVersionNo(anyString(), anyLong());
    }
}