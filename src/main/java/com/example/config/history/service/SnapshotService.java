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

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final ObjectMapper objectMapper;

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

    public <T> T restore(String snapshot, Class<T> entityClass) {
        try {
            return objectMapper.readValue(snapshot, entityClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to restore entity from snapshot", e);
            throw new RuntimeException("Failed to restore entity from snapshot", e);
        }
    }

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