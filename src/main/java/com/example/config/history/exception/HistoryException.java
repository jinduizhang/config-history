package com.example.config.history.exception;

import lombok.Getter;

/**
 * History模块业务异常
 */
@Getter
public class HistoryException extends RuntimeException {
    
    private final Integer code;
    
    /**
     * 构造异常（默认状态码404）
     *
     * @param message 错误消息
     */
    public HistoryException(String message) {
        super(message);
        this.code = 404;
    }
    
    /**
     * 构造异常（自定义状态码）
     *
     * @param code    状态码
     * @param message 错误消息
     */
    public HistoryException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 创建异常实例
     *
     * @param message 错误消息
     * @return 异常实例
     */
    public static HistoryException of(String message) {
        return new HistoryException(message);
    }
    
    /**
     * 创建异常实例
     *
     * @param code    状态码
     * @param message 错误消息
     * @return 异常实例
     */
    public static HistoryException of(Integer code, String message) {
        return new HistoryException(code, message);
    }
}