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
     * 版本号1
     */
    private Integer version1;
    
    /**
     * 版本号2
     */
    private Integer version2;
    
    /**
     * 版本1的配置值
     */
    private String value1;
    
    /**
     * 版本2的配置值
     */
    private String value2;
    
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