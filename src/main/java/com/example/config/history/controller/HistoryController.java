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

    /**
     * 获取实体历史记录
     *
     * @param entityType 实体类型，如 ConfigItem
     * @param entityId   实体ID
     * @param page       页码，默认1
     * @param pageSize   每页数量，默认10
     * @return 分页历史记录
     */
    @GetMapping("/{entityType}/{entityId}")
    @Operation(summary = "获取实体历史记录", description = "分页查询指定实体的历史变更记录")
    public Result<PageResult<HistoryRecord>> getHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(historyService.getHistory(entityType, entityId, page, pageSize));
    }

    /**
     * 获取指定版本详情
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param versionId  历史版本ID
     * @return 历史版本详情
     */
    @GetMapping("/{entityType}/{entityId}/{versionId}")
    @Operation(summary = "获取指定版本", description = "获取实体的指定历史版本详情")
    public Result<HistoryRecord> getVersion(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PathVariable Long versionId) {
        return Result.success(historyService.getVersion(entityType, entityId, versionId));
    }

    /**
     * 版本对比
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param from       源版本ID
     * @param to         目标版本ID
     * @return 差异对比结果
     */
    @GetMapping("/{entityType}/{entityId}/diff")
    @Operation(summary = "版本对比", description = "对比两个历史版本的差异")
    public Result<DiffResult> compareVersions(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam Long from,
            @RequestParam Long to) {
        return Result.success(historyService.compareVersions(entityType, entityId, from, to));
    }

    /**
     * 版本回退
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param versionId  目标版本ID
     * @param operator   操作人
     * @param reason     回退原因
     * @return 操作结果
     */
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