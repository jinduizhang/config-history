package com.example.config.history.controller;

import com.example.config.common.PageResult;
import com.example.config.common.Result;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/history")
@RequiredArgsConstructor
@Tag(name = "通用历史记录", description = "通用实体历史记录管理接口")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/{entityType}/{entityId}")
    @Operation(summary = "获取实体历史记录", description = "分页查询指定实体的历史变更记录")
    public Result<PageResult<HistoryRecord>> getHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(historyService.getHistory(entityType, entityId, page, pageSize));
    }

    @GetMapping("/{entityType}/{entityId}/{versionId}")
    @Operation(summary = "获取指定版本", description = "获取实体的指定历史版本详情")
    public Result<HistoryRecord> getVersion(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PathVariable Long versionId) {
        return Result.success(historyService.getVersion(entityType, entityId, versionId));
    }

    @GetMapping("/{entityType}/{entityId}/diff")
    @Operation(summary = "版本对比", description = "对比两个历史版本的差异")
    public Result<DiffResult> compareVersions(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long from,
            @RequestParam Long to) {
        return Result.success(historyService.compareVersions(entityType, entityId, from, to));
    }

    @PostMapping("/{entityType}/{entityId}/rollback/{versionId}")
    @Operation(summary = "版本回退", description = "将实体回退到指定历史版本")
    public Result<Void> rollback(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PathVariable Long versionId,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String reason) {
        historyService.rollback(entityType, entityId, versionId, operator, reason);
        return Result.success();
    }
}