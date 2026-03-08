package com.example.config.history.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("generic_history")
public class GenericHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String entityType;

    private Long entityId;

    private Integer versionNo;

    private String snapshot;

    private String changeType;

    private String changeFields;

    private String operator;

    private String operatorIp;

    private String changeReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}