package com.example.config.history.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 创建分页结果
     *
     * @param records  数据列表
     * @param total    总记录数
     * @param page     当前页码
     * @param pageSize 每页数量
     * @param <T>      数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> records, Long total, Integer page, Integer pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }
}