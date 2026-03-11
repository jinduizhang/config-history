package com.example.config.history.dto;

import com.example.config.history.service.DiffCalculator;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 版本差异对比结果
 */
@Data
@Builder
public class DiffResult {
    
    /**
     * 源版本号
     */
    private Integer sourceVersion;
    
    /**
     * 目标版本号
     */
    private Integer targetVersion;
    
    /**
     * 源版本快照数据
     */
    private String sourceSnapshot;
    
    /**
     * 目标版本快照数据
     */
    private String targetSnapshot;
    
    /**
     * 差异详情
     * <p>
     * key: 字段名, value: 差异项
     * </p>
     */
    private Map<String, DiffCalculator.DiffItem> differences;
}