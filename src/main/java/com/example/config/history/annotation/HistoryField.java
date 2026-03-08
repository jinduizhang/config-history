package com.example.config.history.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 历史记录字段注解
 * <p>
 * 标注在实体字段上，用于控制历史记录的行为
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryField {
    
    /**
     * 字段显示名称
     *
     * @return 显示名称，用于前端展示
     */
    String displayName() default "";
    
    /**
     * 是否忽略该字段
     * <p>
     * 设为true时，该字段变更不会被记录
     * </p>
     *
     * @return true-忽略，false-记录（默认）
     */
    boolean ignore() default false;
}