package com.example.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.config.history.annotation.HistoryField;
import com.example.config.history.annotation.HistoryTrack;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_item")
@HistoryTrack(entityName = "ConfigItem", tableName = "config_item")
public class ConfigItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    @HistoryField(displayName = "配置键")
    private String configKey;

    private String configName;

    private String configValue;

    private String description;

    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
