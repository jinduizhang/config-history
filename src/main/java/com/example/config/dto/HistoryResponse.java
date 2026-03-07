package com.example.config.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryResponse {
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
