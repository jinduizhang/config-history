package com.example.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffResponse {
    private Integer version1;
    private Integer version2;
    private String value1;
    private String value2;
    private Map<String, DiffItem> differences;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffItem {
        private String type; // ADD, DELETE, MODIFY
        private Object oldValue;
        private Object newValue;
    }
}
