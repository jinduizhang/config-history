package com.example.config.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("config_history")
public class ConfigHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long configId;

    private Integer versionNo;

    private String configValue;

    private String changeType;

    private String operator;

    private String operatorIp;

    private String changeReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
