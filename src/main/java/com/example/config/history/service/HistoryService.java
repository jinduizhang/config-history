package com.example.config.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.common.PageResult;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import com.example.config.history.service.DiffCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

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

    public HistoryRecord getVersion(String entityType, Long entityId, Long versionId) {
        GenericHistory history = genericHistoryMapper.selectById(versionId);
        if (history == null || !history.getEntityType().equals(entityType) || !history.getEntityId().equals(entityId)) {
            throw new RuntimeException("历史版本不存在");
        }
        return toRecord(history);
    }

    public DiffResult compareVersions(String entityType, Long entityId, Long versionId1, Long versionId2) {
        GenericHistory v1 = genericHistoryMapper.selectById(versionId1);
        GenericHistory v2 = genericHistoryMapper.selectById(versionId2);
        
        if (v1 == null || v2 == null) {
            throw new RuntimeException("历史版本不存在");
        }
        
        if (!v1.getEntityType().equals(entityType) || !v1.getEntityId().equals(entityId) ||
            !v2.getEntityType().equals(entityType) || !v2.getEntityId().equals(entityId)) {
            throw new RuntimeException("版本与实体不匹配");
        }
        
        return DiffResult.builder()
                .version1(v1.getVersionNo())
                .version2(v2.getVersionNo())
                .snapshot1(v1.getSnapshot())
                .snapshot2(v2.getSnapshot())
                .differences(diffCalculator.calculate(v1.getSnapshot(), v2.getSnapshot()))
                .build();
    }

    public void rollback(String entityType, Long entityId, Long versionId, String operator, String reason) {
        GenericHistory targetVersion = genericHistoryMapper.selectById(versionId);
        if (targetVersion == null || !targetVersion.getEntityType().equals(entityType) 
                || !targetVersion.getEntityId().equals(entityId)) {
            throw new RuntimeException("目标版本不存在");
        }
        
        log.info("Rollback {}:{} to version {}, operator: {}", entityType, entityId, targetVersion.getVersionNo(), operator);
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