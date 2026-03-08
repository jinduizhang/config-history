package com.example.config.history.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果封装类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;

    /**
     * 成功响应（带数据）
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /**
     * 错误响应（默认状态码500）
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 错误响应（自定义状态码）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 错误响应（带数据）
     *
     * @param code    状态码
     * @param message 错误消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 错误结果
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
}