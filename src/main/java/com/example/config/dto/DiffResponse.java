package com.example.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 版本差异响应DTO
 * <p>
 * 用于返回两个版本之间的差异对比结果
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffResponse {
    
    /**
     * 源版本号
     */
    private Integer sourceVersion;
    
    /**
     * 目标版本号
     */
    private Integer targetVersion;
    
    /**
     * 源版本配置值
     */
    private String sourceValue;
    
    /**
     * 目标版本配置值
     */
    private String targetValue;
    
    /**
     * 差异详情，key为字段名，value为差异项
     */
    private Map<String, DiffItem> differences;

    /**
     * 差异项
     * <p>
     * 描述单个字段的变更信息
     * </p>
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiffItem {
        /**
         * 变更类型：ADD/DELETE/MODIFY
         */
        private String type;
        
        /**
         * 旧值
         */
        private Object oldValue;
        
        /**
         * 新值
         */
        private Object newValue;
    }
}