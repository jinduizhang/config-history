package com.example.config.history.service;

import com.example.config.history.annotation.HistoryField;
import com.example.config.history.annotation.HistoryTrack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 快照服务
 * <p>
 * 负责生成和恢复实体快照
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final ObjectMapper objectMapper;

    /**
     * 生成实体快照
     * <p>
     * 根据实体上的@HistoryTrack和@HistoryField注解生成JSON快照
     * </p>
     *
     * @param entity 实体对象
     * @param <T>    实体类型
     * @return JSON格式的快照
     * @throws RuntimeException 快照生成失败时抛出
     */
    public <T> String snapshot(T entity) {
        try {
            Map<String, Object> data = new HashMap<>();
            Class<?> clazz = entity.getClass();
            
            HistoryTrack historyTrack = clazz.getAnnotation(HistoryTrack.class);
            if (historyTrack == null) {
                return objectMapper.writeValueAsString(entity);
            }
            
            for (Field field : clazz.getDeclaredFields()) {
                HistoryField historyField = field.getAnnotation(HistoryField.class);
                if (historyField != null && historyField.ignore()) {
                    continue;
                }
                
                field.setAccessible(true);
                Object value = field.get(entity);
                String fieldName = historyField != null && !historyField.displayName().isEmpty() 
                    ? historyField.displayName() : field.getName();
                data.put(fieldName, value);
            }
            
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to create snapshot", e);
            throw new RuntimeException("Failed to create snapshot", e);
        }
    }

    /**
     * 从快照恢复实体
     *
     * @param snapshot    JSON快照
     * @param entityClass 实体类
     * @param <T>         实体类型
     * @return 恢复的实体对象
     * @throws RuntimeException 恢复失败时抛出
     */
    public <T> T restore(String snapshot, Class<T> entityClass) {
        try {
            return objectMapper.readValue(snapshot, entityClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to restore entity from snapshot", e);
            throw new RuntimeException("Failed to restore entity from snapshot", e);
        }
    }

    /**
     * 将快照解析为Map
     * <p>
     * 如果快照不是有效的JSON，则将其作为value字段包装
     * </p>
     *
     * @param snapshot JSON快照
     * @return 解析后的Map
     */
    public Map<String, Object> parseToMap(String snapshot) {
        try {
            return objectMapper.readValue(snapshot, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", snapshot);
            return map;
        }
    }
}