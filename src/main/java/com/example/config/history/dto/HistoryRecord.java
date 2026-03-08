package com.example.config.history.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史记录数据传输对象
 */
@Data
public class HistoryRecord {
    
    /**
     * 历史记录ID
     */
    private Long id;
    
    /**
     * 实体类型
     */
    private String entityType;
    
    /**
     * 实体ID
     */
    private Long entityId;
    
    /**
     * 版本号
     */
    private Integer versionNo;
    
    /**
     * 实体快照（JSON格式）
     */
    private String snapshot;
    
    /**
     * 变更类型：CREATE/UPDATE/DELETE
     */
    private String changeType;
    
    /**
     * 变更字段列表
     */
    private List<String> changeFields;
    
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