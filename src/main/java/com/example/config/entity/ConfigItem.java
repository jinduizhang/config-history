package com.example.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.config.history.annotation.HistoryField;
import com.example.config.history.annotation.HistoryTrack;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置项实体
 * <p>
 * 存储系统配置项信息，支持历史版本追踪
 * </p>
 */
@Data
@TableName("config_item")
@HistoryTrack(entityName = "ConfigItem", tableName = "config_item")
public class ConfigItem {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键，唯一标识
     */
    @HistoryField(displayName = "配置键")
    private String configKey;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置值，支持JSON格式
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 逻辑删除标记：0-正常，1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}