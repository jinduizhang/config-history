package com.example.config.history.controller;

import com.example.config.history.common.PageResult;
import com.example.config.history.common.Result;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通用历史记录控制器
 * <p>
 * 提供任意实体类型的历史记录查询、对比和回退功能
 * </p>
 */
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

    @GetMapping("/{entityType}/{entityId}/by-time")
    @Operation(summary = "按时间范围查询历史记录", description = "支持时间范围筛选和排序")
    public Result<PageResult<HistoryRecord>> getHistoryByTimeRange(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(historyService.getHistoryByTimeRange(
                entityType, entityId, startTime, endTime, sortBy, sortOrder, page, pageSize));
    }

    @GetMapping("/{entityType}/{entityId}/top")
    @Operation(summary = "查询前N条历史记录", description = "按时间排序获取前N条历史记录")
    public Result<List<HistoryRecord>> getTopNByTime(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        return Result.success(historyService.getTopNByTime(entityType, entityId, limit, sortOrder));
    }

    @GetMapping("/{entityType}/{entityId}/{versionId}")
    @Operation(summary = "获取指定版本", description = "获取实体的指定历史版本详情")
    public Result<HistoryRecord> getVersion(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PathVariable Long versionId) {
        return Result.success(historyService.getVersion(entityType, entityId, versionId));
    }

    @GetMapping("/{entityType}/{entityId}/at-time")
    @Operation(summary = "获取指定时间点的版本", description = "获取指定时间点最近的历史版本")
    public Result<HistoryRecord> getVersionAtTime(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime targetTime) {
        return Result.success(historyService.getVersionAtTime(entityType, entityId, targetTime));
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

    @PostMapping("/{entityType}/{entityId}/rollback-to-time")
    @Operation(summary = "按时间回退", description = "将实体回退到指定时间点的历史版本")
    public Result<Void> rollbackToTime(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime targetTime,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String reason) {
        historyService.rollbackToTime(entityType, entityId, targetTime, operator, reason);
        return Result.success();
    }
}