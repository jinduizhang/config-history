package com.example.config.history.service.impl;

import com.example.config.history.service.DiffCalculator;
import com.example.config.history.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 差异计算器实现
 * <p>
 * 通过比较两个JSON快照，计算字段级别的差异
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiffCalculatorImpl implements DiffCalculator {

    private final SnapshotService snapshotService;

    @Override
    public Map<String, DiffItem> calculate(String sourceSnapshot, String targetSnapshot) {
        Map<String, DiffItem> result = new HashMap<>();
        
        try {
            Map<String, Object> sourceMap = snapshotService.parseToMap(sourceSnapshot);
            Map<String, Object> targetMap = snapshotService.parseToMap(targetSnapshot);

            for (String key : sourceMap.keySet()) {
                if (!targetMap.containsKey(key)) {
                    result.put(key, DiffItem.builder()
                            .type("DELETE")
                            .oldValue(sourceMap.get(key))
                            .newValue(null)
                            .displayName(key)
                            .build());
                } else if (!equals(sourceMap.get(key), targetMap.get(key))) {
                    result.put(key, DiffItem.builder()
                            .type("MODIFY")
                            .oldValue(sourceMap.get(key))
                            .newValue(targetMap.get(key))
                            .displayName(key)
                            .build());
                }
            }

            for (String key : targetMap.keySet()) {
                if (!sourceMap.containsKey(key)) {
                    result.put(key, DiffItem.builder()
                            .type("ADD")
                            .oldValue(null)
                            .newValue(targetMap.get(key))
                            .displayName(key)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate diff, using simple comparison", e);
            result.put("value", DiffItem.builder()
                    .type("MODIFY")
                    .oldValue(sourceSnapshot)
                    .newValue(targetSnapshot)
                    .displayName("value")
                    .build());
        }
        
        return result;
    }

    private boolean equals(Object first, Object second) {
        if (first == null && second == null) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        return first.equals(second);
    }
}