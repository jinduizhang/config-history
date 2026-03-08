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
     * 版本1版本号
     */
    private Integer version1;
    
    /**
     * 版本2版本号
     */
    private Integer version2;
    
    /**
     * 版本1快照数据
     */
    private String snapshot1;
    
    /**
     * 版本2快照数据
     */
    private String snapshot2;
    
    /**
     * 差异详情
     * <p>
     * key: 字段名, value: 差异项
     * </p>
     */
    private Map<String, DiffCalculator.DiffItem> differences;
}