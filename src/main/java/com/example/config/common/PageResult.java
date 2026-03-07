package com.example.config.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer page;
    private Integer pageSize;

    public static <T> PageResult<T> of(List<T> records, Long total, Integer page, Integer pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }
}
