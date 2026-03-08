package com.example.config.history.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HistoryRecord {
    private Long id;
    private String entityType;
    private Long entityId;
    private Integer versionNo;
    private String snapshot;
    private String changeType;
    private List<String> changeFields;
    private String operator;
    private String operatorIp;
    private String changeReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}