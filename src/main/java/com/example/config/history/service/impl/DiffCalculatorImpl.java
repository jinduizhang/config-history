package com.example.config.history.service.impl;

import com.example.config.history.service.DiffCalculator;
import com.example.config.history.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiffCalculatorImpl implements DiffCalculator {

    private final SnapshotService snapshotService;

    @Override
    public Map<String, DiffItem> calculate(String snapshot1, String snapshot2) {
        Map<String, DiffItem> result = new HashMap<>();
        
        try {
            Map<String, Object> map1 = snapshotService.parseToMap(snapshot1);
            Map<String, Object> map2 = snapshotService.parseToMap(snapshot2);

            for (String key : map1.keySet()) {
                if (!map2.containsKey(key)) {
                    result.put(key, DiffItem.builder()
                            .type("DELETE")
                            .oldValue(map1.get(key))
                            .newValue(null)
                            .displayName(key)
                            .build());
                } else if (!equals(map1.get(key), map2.get(key))) {
                    result.put(key, DiffItem.builder()
                            .type("MODIFY")
                            .oldValue(map1.get(key))
                            .newValue(map2.get(key))
                            .displayName(key)
                            .build());
                }
            }

            for (String key : map2.keySet()) {
                if (!map1.containsKey(key)) {
                    result.put(key, DiffItem.builder()
                            .type("ADD")
                            .oldValue(null)
                            .newValue(map2.get(key))
                            .displayName(key)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to calculate diff, using simple comparison", e);
            result.put("value", DiffItem.builder()
                    .type("MODIFY")
                    .oldValue(snapshot1)
                    .newValue(snapshot2)
                    .displayName("value")
                    .build());
        }
        
        return result;
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