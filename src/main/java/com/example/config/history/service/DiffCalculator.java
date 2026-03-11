package com.example.config.history.service;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 差异计算器接口
 * <p>
 * 用于计算两个版本之间的差异
 * </p>
 */
public interface DiffCalculator {

    /**
     * 计算两个快照的差异
     *
     * @param sourceSnapshot 源快照（旧版本）
     * @param targetSnapshot 目标快照（新版本）
     * @return 差异项Map，key为字段名，value为差异详情
     */
    Map<String, DiffItem> calculate(String sourceSnapshot, String targetSnapshot);

    /**
     * 差异项
     * <p>
     * 描述单个字段的变更信息
     * </p>
     */
    @Data
    @Builder
    class DiffItem {
        /**
         * 变更类型：ADD/MODIFY/DELETE
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
        
        /**
         * 字段显示名
         */
        private String displayName;
    }
}