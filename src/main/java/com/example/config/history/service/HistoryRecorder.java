package com.example.config.history.service;

/**
 * 历史记录器接口
 * <p>
 * 定义记录实体变更历史的核心方法
 * </p>
 */
public interface HistoryRecorder {

    /**
     * 记录创建操作
     *
     * @param entity   被创建的实体
     * @param operator 操作人
     * @param reason   变更原因
     * @param <T>      实体类型
     */
    <T> void recordCreate(T entity, String operator, String reason);

    /**
     * 记录更新操作
     *
     * @param oldEntity 更新前的实体
     * @param newEntity 更新后的实体
     * @param operator  操作人
     * @param reason    变更原因
     * @param <T>       实体类型
     */
    <T> void recordUpdate(T oldEntity, T newEntity, String operator, String reason);

    /**
     * 记录删除操作
     *
     * @param entity   被删除的实体
     * @param operator 操作人
     * @param reason   变更原因
     * @param <T>      实体类型
     */
    <T> void recordDelete(T entity, String operator, String reason);

    /**
     * 记录回退操作
     *
     * @param entity        当前实体
     * @param targetVersion 目标版本号
     * @param operator      操作人
     * @param <T>           实体类型
     */
    <T> void recordRollback(T entity, int targetVersion, String operator);
}