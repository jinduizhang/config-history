package com.example.config.history.exception;

import com.example.config.history.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(1)
@RestControllerAdvice(basePackages = "com.example.config.history")
public class HistoryExceptionHandler {

    @ExceptionHandler(HistoryException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleHistoryException(HistoryException e) {
        log.warn("History exception: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}