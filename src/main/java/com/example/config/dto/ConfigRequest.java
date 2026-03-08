package com.example.config.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 配置请求DTO
 * <p>
 * 用于创建和更新配置项的请求参数
 * </p>
 */
@Data
public class ConfigRequest {

    /**
     * 配置键，唯一标识
     */
    @NotBlank(message = "配置键不能为空")
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
}