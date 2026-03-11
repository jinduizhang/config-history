package com.example.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.common.PageResult;
import com.example.config.dto.*;
import com.example.config.entity.ConfigHistory;
import com.example.config.entity.ConfigItem;
import com.example.config.mapper.ConfigHistoryMapper;
import com.example.config.mapper.ConfigItemMapper;
import com.example.config.service.ConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigItemMapper configItemMapper;
    private final ConfigHistoryMapper configHistoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<ConfigResponse> listConfigs(Integer page, Integer pageSize, String keyword) {
        Page<ConfigItem> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ConfigItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigItem::getDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ConfigItem::getConfigKey, keyword)
                    .or()
                    .like(ConfigItem::getConfigName, keyword);
        }
        wrapper.orderByDesc(ConfigItem::getUpdatedAt);

        IPage<ConfigItem> result = configItemMapper.selectPage(pageParam, wrapper);
        return PageResult.of(
                result.getRecords().stream().map(this::toResponse).toList(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    @Override
    public ConfigResponse getConfigById(Long id) {
        ConfigItem item = configItemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new RuntimeException("配置不存在");
        }
        return toResponse(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigResponse createConfig(ConfigRequest request) {
        ConfigItem item = new ConfigItem();
        item.setConfigKey(request.getConfigKey());
        item.setConfigName(request.getConfigName());
        item.setConfigValue(request.getConfigValue());
        item.setDescription(request.getDescription());
        item.setDeleted(0);

        configItemMapper.insert(item);

        ConfigHistory history = new ConfigHistory();
        history.setConfigId(item.getId());
        history.setVersionNo(1);
        history.setConfigValue(request.getConfigValue());
        history.setChangeType("INIT");
        history.setOperator(request.getOperator());
        history.setOperatorIp(request.getOperatorIp());
        history.setChangeReason("初始化配置");
        configHistoryMapper.insert(history);

        return toResponse(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigResponse updateConfig(Long id, ConfigRequest request) {
        ConfigItem item = configItemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new RuntimeException("配置不存在");
        }

        // 先更新配置
        item.setConfigValue(request.getConfigValue());
        if (request.getConfigName() != null) {
            item.setConfigName(request.getConfigName());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        configItemMapper.updateById(item);

        // 再记录历史（保存新值）
        Integer maxVersion = configHistoryMapper.selectMaxVersionNo(id);
        ConfigHistory history = new ConfigHistory();
        history.setConfigId(id);
        history.setVersionNo(maxVersion + 1);
        history.setConfigValue(request.getConfigValue()); // 保存新值
        history.setChangeType("UPDATE");
        history.setOperator(request.getOperator());
        history.setOperatorIp(request.getOperatorIp());
        history.setChangeReason(request.getChangeReason());
        configHistoryMapper.insert(history);

        return toResponse(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long id) {
        ConfigItem item = configItemMapper.selectById(id);
        if (item == null || item.getDeleted() == 1) {
            throw new RuntimeException("配置不存在");
        }
        item.setDeleted(1);
        configItemMapper.updateById(item);
    }

    @Override
    public PageResult<HistoryResponse> getHistory(Long configId, Integer page, Integer pageSize) {
        Page<ConfigHistory> pageParam = new Page<>(page, pageSize);
        IPage<ConfigHistory> result = configHistoryMapper.selectHistoryPage(pageParam, configId);
        return PageResult.of(
                result.getRecords().stream().map(this::toHistoryResponse).toList(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }

    @Override
    public HistoryResponse getHistoryVersion(Long configId, Long versionId) {
        ConfigHistory history = configHistoryMapper.selectById(versionId);
        if (history == null || !history.getConfigId().equals(configId)) {
            throw new RuntimeException("历史版本不存在");
        }
        return toHistoryResponse(history);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollback(Long configId, Long versionId, String operator) {
        ConfigHistory targetVersion = configHistoryMapper.selectById(versionId);
        if (targetVersion == null || !targetVersion.getConfigId().equals(configId)) {
            throw new RuntimeException("历史版本不存在");
        }

        ConfigItem current = configItemMapper.selectById(configId);
        if (current == null || current.getDeleted() == 1) {
            throw new RuntimeException("配置不存在");
        }

        Integer maxVersion = configHistoryMapper.selectMaxVersionNo(configId);

        // 先更新配置为目标版本的值
        current.setConfigValue(targetVersion.getConfigValue());
        configItemMapper.updateById(current);

        // 再记录历史（保存回退后的值，即目标版本的值）
        ConfigHistory rollbackHistory = new ConfigHistory();
        rollbackHistory.setConfigId(configId);
        rollbackHistory.setVersionNo(maxVersion + 1);
        rollbackHistory.setConfigValue(targetVersion.getConfigValue()); // 保存目标版本的值
        rollbackHistory.setChangeType("ROLLBACK");
        rollbackHistory.setOperator(operator);
        rollbackHistory.setChangeReason("回退到版本 " + targetVersion.getVersionNo());
        configHistoryMapper.insert(rollbackHistory);
    }

    @Override
    public DiffResponse compareVersions(Long configId, Long versionId1, Long versionId2) {
        ConfigHistory sourceHistory = configHistoryMapper.selectById(versionId1);
        ConfigHistory targetHistory = configHistoryMapper.selectById(versionId2);

        if (sourceHistory == null || targetHistory == null) {
            throw new RuntimeException("历史版本不存在");
        }

        Map<String, DiffResponse.DiffItem> diffMap = computeDiff(sourceHistory.getConfigValue(), targetHistory.getConfigValue());

        return DiffResponse.builder()
                .sourceVersion(sourceHistory.getVersionNo())
                .targetVersion(targetHistory.getVersionNo())
                .sourceValue(sourceHistory.getConfigValue())
                .targetValue(targetHistory.getConfigValue())
                .differences(diffMap)
                .build();
    }

    private Map<String, DiffResponse.DiffItem> computeDiff(String sourceJson, String targetJson) {
        Map<String, DiffResponse.DiffItem> result = new HashMap<>();
        try {
            Map<String, Object> sourceMap = parseJsonOrString(sourceJson);
            Map<String, Object> targetMap = parseJsonOrString(targetJson);

            for (String key : sourceMap.keySet()) {
                if (!targetMap.containsKey(key)) {
                    result.put(key, DiffResponse.DiffItem.builder()
                            .type("DELETE")
                            .oldValue(sourceMap.get(key))
                            .newValue(null)
                            .build());
                } else if (!sourceMap.get(key).equals(targetMap.get(key))) {
                    result.put(key, DiffResponse.DiffItem.builder()
                            .type("MODIFY")
                            .oldValue(sourceMap.get(key))
                            .newValue(targetMap.get(key))
                            .build());
                }
            }

            for (String key : targetMap.keySet()) {
                if (!sourceMap.containsKey(key)) {
                    result.put(key, DiffResponse.DiffItem.builder()
                            .type("ADD")
                            .oldValue(null)
                            .newValue(targetMap.get(key))
                            .build());
                }
            }
        } catch (Exception e) {
            result.put("value", DiffResponse.DiffItem.builder()
                    .type("MODIFY")
                    .oldValue(sourceJson)
                    .newValue(targetJson)
                    .build());
        }
        return result;
    }

    private Map<String, Object> parseJsonOrString(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", value);
            return map;
        }
    }

    private ConfigResponse toResponse(ConfigItem item) {
        ConfigResponse response = new ConfigResponse();
        response.setId(item.getId());
        response.setConfigKey(item.getConfigKey());
        response.setConfigName(item.getConfigName());
        response.setConfigValue(item.getConfigValue());
        response.setDescription(item.getDescription());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    private HistoryResponse toHistoryResponse(ConfigHistory history) {
        HistoryResponse response = new HistoryResponse();
        response.setId(history.getId());
        response.setConfigId(history.getConfigId());
        response.setVersionNo(history.getVersionNo());
        response.setConfigValue(history.getConfigValue());
        response.setChangeType(history.getChangeType());
        response.setOperator(history.getOperator());
        response.setOperatorIp(history.getOperatorIp());
        response.setChangeReason(history.getChangeReason());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }
}
