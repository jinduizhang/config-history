package com.example.config.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigRequest {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    private String configName;

    private String configValue;

    private String description;

    private String operator;

    private String operatorIp;

    private String changeReason;
}
