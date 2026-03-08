package com.example.config.history.service;

public interface HistoryRecorder {

    <T> void recordCreate(T entity, String operator, String reason);

    <T> void recordUpdate(T oldEntity, T newEntity, String operator, String reason);

    <T> void recordDelete(T entity, String operator, String reason);

    <T> void recordRollback(T entity, int targetVersion, String operator);
}