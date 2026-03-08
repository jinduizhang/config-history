package com.example.config.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.config.history.entity.GenericHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface GenericHistoryMapper extends BaseMapper<GenericHistory> {

    @Select("SELECT COALESCE(MAX(version_no), 0) FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    Integer selectMaxVersionNo(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Select("SELECT * FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId} " +
            "AND created_at <= #{targetTime} ORDER BY created_at DESC LIMIT 1")
    GenericHistory selectByVersionAtTime(@Param("entityType") String entityType, 
                                         @Param("entityId") Long entityId, 
                                         @Param("targetTime") LocalDateTime targetTime);

    @Select("SELECT * FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId} " +
            "ORDER BY created_at ${orderDirection} LIMIT #{limit}")
    List<GenericHistory> selectTopNByTime(@Param("entityType") String entityType, 
                                          @Param("entityId") Long entityId, 
                                          @Param("limit") int limit,
                                          @Param("orderDirection") String orderDirection);
}