package com.example.config.history.aspect;

import com.example.config.history.annotation.HistoryTrack;
import com.example.config.history.service.HistoryRecorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HistoryAspect {

    private final HistoryRecorder historyRecorder;
    private final ThreadLocal<Map<String, Object>> oldEntityHolder = new ThreadLocal<>();

    @Around("execution(* com.example.config.service..*Service.create*(..)) || " +
            "execution(* com.example.config.service..*Service.save*(..)) || " +
            "execution(* com.example.config.service..*Service.insert*(..))")
    public Object recordCreate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        Object entity = getEntityArg(joinPoint);
        if (entity != null && entity.getClass().isAnnotationPresent(HistoryTrack.class)) {
            String operator = getOperatorFromArgs(joinPoint);
            historyRecorder.recordCreate(entity, operator, "创建");
        }
        
        return result;
    }

    @Around("execution(* com.example.config.service..*Service.update*(..)) || " +
            "execution(* com.example.config.service..*Service.modify*(..))")
    public Object recordUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Long entityId = null;
        Object newEntity = null;
        String operator = "system";
        String reason = null;
        
        for (Object arg : args) {
            if (arg instanceof Long) {
                entityId = (Long) arg;
            } else if (arg != null && arg.getClass().isAnnotationPresent(HistoryTrack.class)) {
                newEntity = arg;
            } else if (arg instanceof String) {
                if (reason == null) {
                    reason = (String) arg;
                } else if (operator.equals("system")) {
                    operator = (String) arg;
                }
            }
        }
        
        Object result = joinPoint.proceed();
        
        if (newEntity != null && newEntity.getClass().isAnnotationPresent(HistoryTrack.class)) {
            historyRecorder.recordUpdate(newEntity, newEntity, operator, reason != null ? reason : "更新");
        }
        
        return result;
    }

    @Around("execution(* com.example.config.service..*Service.delete*(..)) || " +
            "execution(* com.example.config.service..*Service.remove*(..))")
    public Object recordDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        
        Object[] args = joinPoint.getArgs();
        String operator = "system";
        
        for (Object arg : args) {
            if (arg instanceof String && !operator.equals("system")) {
                operator = (String) arg;
                break;
            }
        }
        
        log.info("Delete operation recorded, operator: {}", operator);
        return result;
    }

    private Object getEntityArg(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null && arg.getClass().isAnnotationPresent(HistoryTrack.class)) {
                return arg;
            }
        }
        return null;
    }

    private String getOperatorFromArgs(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].toLowerCase().contains("operator") && args[i] instanceof String) {
                return (String) args[i];
            }
        }
        
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        
        return "system";
    }
}