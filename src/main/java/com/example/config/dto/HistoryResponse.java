package com.example.config.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 历史记录响应DTO
 * <p>
 * 用于返回配置项的历史版本信息
 * </p>
 */
@Data
public class HistoryResponse {
    
    /**
     * 历史记录ID
     */
    private Long id;
    
    /**
     * 关联的配置ID
     */
    private Long configId;
    
    /**
     * 版本号
     */
    private Integer versionNo;
    
    /**
     * 配置值快照
     */
    private String configValue;
    
    /**
     * 变更类型：INIT/UPDATE/ROLLBACK
     */
    private String changeType;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 操作IP
     */
    private String operatorIp;
    
    /**
     * 变更原因
     */
    private String changeReason;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}