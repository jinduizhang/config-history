package com.example.config.history.service;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

public interface DiffCalculator {

    Map<String, DiffItem> calculate(String snapshot1, String snapshot2);

    @Data
    @Builder
    class DiffItem {
        private String type;
        private Object oldValue;
        private Object newValue;
        private String displayName;
    }
}