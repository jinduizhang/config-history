package com.example.config.history.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用历史记录实体
 * <p>
 * 存储任意实体的变更历史，支持快照和版本管理
 * </p>
 */
@Data
@TableName("generic_history")
public class GenericHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 实体类型，如 ConfigItem
     */
    private String entityType;

    /**
     * 实体ID
     */
    private Long entityId;

    /**
     * 版本号，从1开始递增
     */
    private Integer versionNo;

    /**
     * 实体快照，JSON格式
     */
    private String snapshot;

    /**
     * 变更类型：CREATE/UPDATE/DELETE/ROLLBACK
     */
    private String changeType;

    /**
     * 变更字段列表，逗号分隔
     */
    private String changeFields;

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