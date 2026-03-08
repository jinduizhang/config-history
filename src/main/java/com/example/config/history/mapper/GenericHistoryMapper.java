package com.example.config.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.config.history.entity.GenericHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface GenericHistoryMapper extends BaseMapper<GenericHistory> {

    @Select("SELECT COALESCE(MAX(version_no), 0) FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    Integer selectMaxVersionNo(@Param("entityType") String entityType, @Param("entityId") Long entityId);
}