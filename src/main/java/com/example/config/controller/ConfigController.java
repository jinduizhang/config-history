package com.example.config.controller;

import com.example.config.common.PageResult;
import com.example.config.common.Result;
import com.example.config.dto.*;
import com.example.config.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
@Tag(name = "配置管理", description = "配置项的CRUD及历史管理接口")
public class ConfigController {

    private final ConfigService configService;

    @GetMapping
    @Operation(summary = "获取配置列表", description = "分页查询配置列表，支持关键字搜索")
    public Result<PageResult<ConfigResponse>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(configService.listConfigs(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情", description = "根据ID获取配置详情")
    public Result<ConfigResponse> getById(@PathVariable Long id) {
        return Result.success(configService.getConfigById(id));
    }

    @PostMapping
    @Operation(summary = "新增配置", description = "创建新的配置项")
    public Result<ConfigResponse> create(@Valid @RequestBody ConfigRequest request) {
        return Result.success(configService.createConfig(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配置", description = "更新配置项，会自动记录历史")
    public Result<ConfigResponse> update(@PathVariable Long id, @Valid @RequestBody ConfigRequest request) {
        return Result.success(configService.updateConfig(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置", description = "软删除配置项")
    public Result<Void> delete(@PathVariable Long id) {
        configService.deleteConfig(id);
        return Result.success();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "获取历史记录", description = "获取配置的历史变更记录")
    public Result<PageResult<HistoryResponse>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(configService.getHistory(id, page, pageSize));
    }

    @GetMapping("/{id}/history/{versionId}")
    @Operation(summary = "获取历史版本详情", description = "获取指定历史版本的完整快照")
    public Result<HistoryResponse> getHistoryVersion(
            @PathVariable Long id,
            @PathVariable Long versionId) {
        return Result.success(configService.getHistoryVersion(id, versionId));
    }

    @PostMapping("/{id}/rollback/{versionId}")
    @Operation(summary = "回退版本", description = "将配置回退到指定历史版本")
    public Result<Void> rollback(
            @PathVariable Long id,
            @PathVariable Long versionId,
            @RequestParam(required = false) String operator) {
        configService.rollback(id, versionId, operator);
        return Result.success();
    }

    @GetMapping("/{id}/diff")
    @Operation(summary = "版本对比", description = "对比两个历史版本的差异")
    public Result<DiffResponse> diff(
            @PathVariable Long id,
            @RequestParam Long from,
            @RequestParam Long to) {
        return Result.success(configService.compareVersions(id, from, to));
    }
}
