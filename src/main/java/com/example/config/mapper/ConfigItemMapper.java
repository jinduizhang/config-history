package com.example.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.config.entity.ConfigItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigItemMapper extends BaseMapper<ConfigItem> {
}
