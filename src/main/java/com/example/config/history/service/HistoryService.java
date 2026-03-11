package com.example.config.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.history.common.PageResult;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import com.example.config.history.exception.HistoryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 通用历史记录服务
 * <p>
 * 提供历史记录的查询、对比和回退功能
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final GenericHistoryMapper genericHistoryMapper;
    private final DiffCalculator diffCalculator;

    public PageResult<HistoryRecord> getHistory(String entityType, Long entityId, Integer page, Integer pageSize) {
        Page<GenericHistory> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<GenericHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenericHistory::getEntityType, entityType)
                .eq(GenericHistory::getEntityId, entityId)
                .orderByDesc(GenericHistory::getVersionNo);
        
        IPage<GenericHistory> result = genericHistoryMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(
                result.getRecords().stream().map(this::toRecord).toList(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    public PageResult<HistoryRecord> getHistoryByTimeRange(String entityType, Long entityId, 
                                                           LocalDateTime startTime, LocalDateTime endTime,
                                                           String sortBy, String sortOrder,
                                                           Integer page, Integer pageSize) {
        Page<GenericHistory> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<GenericHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GenericHistory::getEntityType, entityType)
                .eq(GenericHistory::getEntityId, entityId);
        
        if (startTime != null) {
            wrapper.ge(GenericHistory::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(GenericHistory::getCreatedAt, endTime);
        }
        
        boolean desc = "desc".equalsIgnoreCase(sortOrder);
        if ("createdAt".equalsIgnoreCase(sortBy) || "time".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, !desc, GenericHistory::getCreatedAt);
        } else if ("versionNo".equalsIgnoreCase(sortBy) || "version".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, !desc, GenericHistory::getVersionNo);
        } else {
            wrapper.orderByDesc(GenericHistory::getCreatedAt);
        }
        
        IPage<GenericHistory> result = genericHistoryMapper.selectPage(pageParam, wrapper);
        
        return PageResult.of(
                result.getRecords().stream().map(this::toRecord).toList(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    public List<HistoryRecord> getTopNByTime(String entityType, Long entityId, int limit, String sortOrder) {
        String orderDirection = "asc".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";
        List<GenericHistory> histories = genericHistoryMapper.selectTopNByTime(entityType, entityId, limit, orderDirection);
        return histories.stream().map(this::toRecord).toList();
    }

    public HistoryRecord getVersion(String entityType, Long entityId, Long versionId) {
        GenericHistory history = genericHistoryMapper.selectById(versionId);
        if (history == null || !history.getEntityType().equals(entityType) || !history.getEntityId().equals(entityId)) {
            throw new HistoryException("历史版本不存在");
        }
        return toRecord(history);
    }

    public HistoryRecord getVersionAtTime(String entityType, Long entityId, LocalDateTime targetTime) {
        GenericHistory history = genericHistoryMapper.selectByVersionAtTime(entityType, entityId, targetTime);
        if (history == null) {
            throw new HistoryException("指定时间点没有历史记录");
        }
        return toRecord(history);
    }

    public DiffResult compareVersions(String entityType, Long entityId, Long versionId1, Long versionId2) {
        GenericHistory sourceHistory = genericHistoryMapper.selectById(versionId1);
        GenericHistory targetHistory = genericHistoryMapper.selectById(versionId2);
        
        if (sourceHistory == null || targetHistory == null) {
            throw new HistoryException("历史版本不存在");
        }
        
        if (!sourceHistory.getEntityType().equals(entityType) || !sourceHistory.getEntityId().equals(entityId) ||
            !targetHistory.getEntityType().equals(entityType) || !targetHistory.getEntityId().equals(entityId)) {
            throw new HistoryException("版本与实体不匹配");
        }
        
        return DiffResult.builder()
                .sourceVersion(sourceHistory.getVersionNo())
                .targetVersion(targetHistory.getVersionNo())
                .sourceSnapshot(sourceHistory.getSnapshot())
                .targetSnapshot(targetHistory.getSnapshot())
                .differences(diffCalculator.calculate(sourceHistory.getSnapshot(), targetHistory.getSnapshot()))
                .build();
    }

    public void rollback(String entityType, Long entityId, Long versionId, String operator, String reason) {
        GenericHistory targetVersion = genericHistoryMapper.selectById(versionId);
        if (targetVersion == null || !targetVersion.getEntityType().equals(entityType) 
                || !targetVersion.getEntityId().equals(entityId)) {
            throw new HistoryException("目标版本不存在");
        }
        
        log.info("Rollback {}:{} to version {}, operator: {}", entityType, entityId, targetVersion.getVersionNo(), operator);
    }

    public void rollbackToTime(String entityType, Long entityId, LocalDateTime targetTime, String operator, String reason) {
        GenericHistory targetVersion = genericHistoryMapper.selectByVersionAtTime(entityType, entityId, targetTime);
        if (targetVersion == null) {
            throw new HistoryException("指定时间点没有历史记录");
        }
        
        log.info("Rollback {}:{} to time {} (version {}), operator: {}", 
                entityType, entityId, targetTime, targetVersion.getVersionNo(), operator);
    }

    private HistoryRecord toRecord(GenericHistory history) {
        HistoryRecord record = new HistoryRecord();
        record.setId(history.getId());
        record.setEntityType(history.getEntityType());
        record.setEntityId(history.getEntityId());
        record.setVersionNo(history.getVersionNo());
        record.setSnapshot(history.getSnapshot());
        record.setChangeType(history.getChangeType());
        record.setChangeFields(history.getChangeFields() != null 
                ? Arrays.asList(history.getChangeFields().split(",")) 
                : Collections.emptyList());
        record.setOperator(history.getOperator());
        record.setOperatorIp(history.getOperatorIp());
        record.setChangeReason(history.getChangeReason());
        record.setCreatedAt(history.getCreatedAt());
        return record;
    }
}