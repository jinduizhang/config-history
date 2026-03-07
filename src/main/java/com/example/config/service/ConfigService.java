package com.example.config.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.common.PageResult;
import com.example.config.dto.ConfigRequest;
import com.example.config.dto.DiffResponse;
import com.example.config.dto.HistoryResponse;

public interface ConfigService {

    PageResult<com.example.config.dto.ConfigResponse> listConfigs(Integer page, Integer pageSize, String keyword);

    com.example.config.dto.ConfigResponse getConfigById(Long id);

    com.example.config.dto.ConfigResponse createConfig(ConfigRequest request);

    com.example.config.dto.ConfigResponse updateConfig(Long id, ConfigRequest request);

    void deleteConfig(Long id);

    PageResult<HistoryResponse> getHistory(Long configId, Integer page, Integer pageSize);

    HistoryResponse getHistoryVersion(Long configId, Long versionId);

    void rollback(Long configId, Long versionId, String operator);

    DiffResponse compareVersions(Long configId, Long versionId1, Long versionId2);
}
