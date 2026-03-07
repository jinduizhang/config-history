package com.example.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.config.entity.ConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConfigHistoryMapper extends BaseMapper<ConfigHistory> {

    IPage<ConfigHistory> selectHistoryPage(Page<?> page, @Param("configId") Long configId);

    Integer selectMaxVersionNo(@Param("configId") Long configId);
}
