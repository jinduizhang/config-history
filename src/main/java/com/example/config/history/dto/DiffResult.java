package com.example.config.history.dto;

import com.example.config.history.service.DiffCalculator;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DiffResult {
    private Integer version1;
    private Integer version2;
    private String snapshot1;
    private String snapshot2;
    private Map<String, DiffCalculator.DiffItem> differences;
}