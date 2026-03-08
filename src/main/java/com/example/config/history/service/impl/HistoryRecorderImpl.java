package com.example.config.history.service.impl;

import com.example.config.history.annotation.HistoryTrack;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import com.example.config.history.service.HistoryRecorder;
import com.example.config.history.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryRecorderImpl implements HistoryRecorder {

    private final GenericHistoryMapper genericHistoryMapper;
    private final SnapshotService snapshotService;

    @Override
    public <T> void recordCreate(T entity, String operator, String reason) {
        record(entity, "CREATE", operator, reason, null);
    }

    @Override
    public <T> void recordUpdate(T oldEntity, T newEntity, String operator, String reason) {
        List<String> changedFields = detectChangedFields(oldEntity, newEntity);
        record(newEntity, "UPDATE", operator, reason, changedFields);
    }

    @Override
    public <T> void recordDelete(T entity, String operator, String reason) {
        record(entity, "DELETE", operator, reason, null);
    }

    @Override
    public <T> void recordRollback(T entity, int targetVersion, String operator) {
        record(entity, "ROLLBACK", operator, "回退到版本 " + targetVersion, null);
    }

    private <T> void record(T entity, String changeType, String operator, String reason, List<String> changedFields) {
        HistoryTrack historyTrack = entity.getClass().getAnnotation(HistoryTrack.class);
        if (historyTrack == null) {
            log.debug("Entity {} is not tracked by history", entity.getClass().getSimpleName());
            return;
        }

        try {
            Long entityId = getEntityId(entity, historyTrack);
            String entityType = historyTrack.entityName();
            Integer maxVersion = genericHistoryMapper.selectMaxVersionNo(entityType, entityId);
            
            GenericHistory history = new GenericHistory();
            history.setEntityType(entityType);
            history.setEntityId(entityId);
            history.setVersionNo(maxVersion + 1);
            history.setSnapshot(snapshotService.snapshot(entity));
            history.setChangeType(changeType);
            history.setChangeFields(changedFields != null ? String.join(",", changedFields) : null);
            history.setOperator(operator);
            history.setChangeReason(reason);
            history.setCreatedAt(LocalDateTime.now());
            
            genericHistoryMapper.insert(history);
            log.info("Recorded history for {}:{}, version={}", entityType, entityId, history.getVersionNo());
        } catch (Exception e) {
            log.error("Failed to record history", e);
        }
    }

    private <T> Long getEntityId(T entity, HistoryTrack historyTrack) throws Exception {
        String idField = historyTrack.idField();
        Field field = entity.getClass().getDeclaredField(idField);
        field.setAccessible(true);
        return (Long) field.get(entity);
    }

    private <T> List<String> detectChangedFields(T oldEntity, T newEntity) {
        List<String> changedFields = new ArrayList<>();
        try {
            for (Field field : oldEntity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);
                if (!equals(oldValue, newValue)) {
                    changedFields.add(field.getName());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to detect changed fields", e);
        }
        return changedFields;
    }

    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }
}