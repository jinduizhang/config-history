package com.example.config.history.exception;

import lombok.Getter;

@Getter
public class HistoryException extends RuntimeException {
    
    private final Integer code;
    
    public HistoryException(String message) {
        super(message);
        this.code = 404;
    }
    
    public HistoryException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public static HistoryException of(String message) {
        return new HistoryException(message);
    }
    
    public static HistoryException of(Integer code, String message) {
        return new HistoryException(code, message);
    }
}