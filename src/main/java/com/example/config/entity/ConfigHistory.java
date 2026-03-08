package com.example.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置历史实体
 * <p>
 * 存储配置项的变更历史记录
 * </p>
 */
@Data
@TableName("config_history")
public class ConfigHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的配置项ID
     */
    private Long configId;

    /**
     * 版本号，从1开始递增
     */
    private Integer versionNo;

    /**
     * 配置值快照
     */
    private String configValue;

    /**
     * 变更类型：INIT-初始化，UPDATE-更新，ROLLBACK-回退
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