package com.example.config.history.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.common.PageResult;
import com.example.config.history.dto.DiffResult;
import com.example.config.history.dto.HistoryRecord;
import com.example.config.history.entity.GenericHistory;
import com.example.config.history.mapper.GenericHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

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

    /**
     * 获取实体历史记录列表
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param page       页码
     * @param pageSize   每页数量
     * @return 分页历史记录
     */
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

    /**
     * 获取指定历史版本详情
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param versionId  历史版本ID
     * @return 历史版本详情
     * @throws RuntimeException 版本不存在或与实体不匹配时抛出
     */
    public HistoryRecord getVersion(String entityType, Long entityId, Long versionId) {
        GenericHistory history = genericHistoryMapper.selectById(versionId);
        if (history == null || !history.getEntityType().equals(entityType) || !history.getEntityId().equals(entityId)) {
            throw new RuntimeException("历史版本不存在");
        }
        return toRecord(history);
    }

    /**
     * 对比两个历史版本
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param versionId1 版本1 ID
     * @param versionId2 版本2 ID
     * @return 差异对比结果
     * @throws RuntimeException 版本不存在或不匹配时抛出
     */
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

    /**
     * 回退到指定版本
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param versionId  目标版本ID
     * @param operator   操作人
     * @param reason     回退原因
     * @throws RuntimeException 目标版本不存在时抛出
     */
    public void rollback(String entityType, Long entityId, Long versionId, String operator, String reason) {
        GenericHistory targetVersion = genericHistoryMapper.selectById(versionId);
        if (targetVersion == null || !targetVersion.getEntityType().equals(entityType) 
                || !targetVersion.getEntityId().equals(entityId)) {
            throw new RuntimeException("目标版本不存在");
        }
        
        log.info("Rollback {}:{} to version {}, operator: {}", entityType, entityId, targetVersion.getVersionNo(), operator);
    }

    /**
     * 将实体转换为DTO
     *
     * @param history 历史记录实体
     * @return 历史记录DTO
     */
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