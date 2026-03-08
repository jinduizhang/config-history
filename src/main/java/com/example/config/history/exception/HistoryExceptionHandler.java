package com.example.config.history.exception;

import com.example.config.history.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * History模块异常处理器
 * <p>
 * 优先级设为1，先于全局异常处理器执行
 * </p>
 */
@Slf4j
@Order(1)
@RestControllerAdvice(basePackages = "com.example.config.history")
public class HistoryExceptionHandler {

    /**
     * 处理HistoryException
     *
     * @param e 异常实例
     * @return 错误响应
     */
    @ExceptionHandler(HistoryException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleHistoryException(HistoryException e) {
        log.warn("History exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}