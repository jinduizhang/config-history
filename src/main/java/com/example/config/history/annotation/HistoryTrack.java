package com.example.config.history.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 历史追踪注解
 * <p>
 * 标注在实体类上，启用该实体的变更历史自动记录功能
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HistoryTrack {
    
    /**
     * 实体名称
     *
     * @return 实体类型标识，如 ConfigItem, Order 等
     */
    String entityName();
    
    /**
     * 表名
     *
     * @return 数据库表名
     */
    String tableName();
    
    /**
     * 主键字段名
     *
     * @return 主键字段名，默认为 id
     */
    String idField() default "id";
}