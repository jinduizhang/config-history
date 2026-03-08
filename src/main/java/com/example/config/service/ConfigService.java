package com.example.config.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.common.PageResult;
import com.example.config.dto.ConfigRequest;
import com.example.config.dto.DiffResponse;
import com.example.config.dto.HistoryResponse;

/**
 * 配置服务接口
 * <p>
 * 提供配置项的业务操作，包括CRUD、历史记录查询、版本对比和回退
 * </p>
 */
public interface ConfigService {

    /**
     * 分页查询配置列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param keyword  搜索关键字，可为null
     * @return 分页配置列表
     */
    PageResult<com.example.config.dto.ConfigResponse> listConfigs(Integer page, Integer pageSize, String keyword);

    /**
     * 根据ID获取配置详情
     *
     * @param id 配置ID
     * @return 配置详情
     * @throws RuntimeException 配置不存在时抛出异常
     */
    com.example.config.dto.ConfigResponse getConfigById(Long id);

    /**
     * 创建配置
     *
     * @param request 配置请求信息
     * @return 创建后的配置
     */
    com.example.config.dto.ConfigResponse createConfig(ConfigRequest request);

    /**
     * 更新配置
     *
     * @param id      配置ID
     * @param request 配置请求信息
     * @return 更新后的配置
     * @throws RuntimeException 配置不存在时抛出异常
     */
    com.example.config.dto.ConfigResponse updateConfig(Long id, ConfigRequest request);

    /**
     * 删除配置（软删除）
     *
     * @param id 配置ID
     * @throws RuntimeException 配置不存在时抛出异常
     */
    void deleteConfig(Long id);

    /**
     * 获取配置历史记录
     *
     * @param configId 配置ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 分页历史记录
     */
    PageResult<HistoryResponse> getHistory(Long configId, Integer page, Integer pageSize);

    /**
     * 获取指定历史版本详情
     *
     * @param configId  配置ID
     * @param versionId 历史版本ID
     * @return 历史版本详情
     * @throws RuntimeException 版本不存在时抛出异常
     */
    HistoryResponse getHistoryVersion(Long configId, Long versionId);

    /**
     * 回退到指定版本
     *
     * @param configId  配置ID
     * @param versionId 目标版本ID
     * @param operator  操作人
     * @throws RuntimeException 配置或版本不存在时抛出异常
     */
    void rollback(Long configId, Long versionId, String operator);

    /**
     * 对比两个历史版本
     *
     * @param configId   配置ID
     * @param versionId1 版本1 ID
     * @param versionId2 版本2 ID
     * @return 差异对比结果
     * @throws RuntimeException 版本不存在时抛出异常
     */
    DiffResponse compareVersions(Long configId, Long versionId1, Long versionId2);
}
