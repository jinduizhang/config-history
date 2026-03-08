package com.example.config.history.service;

import com.example.config.history.annotation.HistoryTrack;
import com.example.config.history.aspect.HistoryAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * HistoryAspect 单元测试
 */
@ExtendWith(MockitoExtension.class)
class HistoryAspectTest {

    @Mock
    private HistoryRecorder historyRecorder;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private HistoryAspect historyAspect;

    @HistoryTrack(entityName = "TestEntity", tableName = "test_entity")
    static class TestEntity {
        private Long id;
        private String name;

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
    }

    @Test
    @DisplayName("记录创建操作 - 带注解实体")
    void recordCreate_withAnnotatedEntity() throws Throwable {
        TestEntity entity = new TestEntity(1L, "test");
        Object[] args = new Object[]{entity};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(entity);
        lenient().when(joinPoint.getSignature()).thenReturn(methodSignature);
        lenient().when(methodSignature.getParameterNames()).thenReturn(new String[]{});

        historyAspect.recordCreate(joinPoint);

        verify(joinPoint, times(1)).proceed();
        verify(historyRecorder, times(1)).recordCreate(any(TestEntity.class), anyString(), anyString());
    }

    @Test
    @DisplayName("记录创建操作 - 无注解实体")
    void recordCreate_withoutAnnotation() throws Throwable {
        Object entity = new Object();
        Object[] args = new Object[]{entity};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(entity);

        historyAspect.recordCreate(joinPoint);

        verify(joinPoint, times(1)).proceed();
        verify(historyRecorder, never()).recordCreate(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("记录更新操作 - 带注解实体")
    void recordUpdate_withAnnotatedEntity() throws Throwable {
        TestEntity entity = new TestEntity(1L, "test");
        Object[] args = new Object[]{entity};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(entity);

        historyAspect.recordUpdate(joinPoint);

        verify(joinPoint, times(1)).proceed();
        verify(historyRecorder, times(1)).recordUpdate(any(TestEntity.class), any(TestEntity.class), anyString(), anyString());
    }

    @Test
    @DisplayName("记录删除操作 - 正常场景")
    void recordDelete_success() throws Throwable {
        Object[] args = new Object[]{1L, "admin"};
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn(null);

        historyAspect.recordDelete(joinPoint);

        verify(joinPoint, times(1)).proceed();
    }
}