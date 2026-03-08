package com.example.config.history.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.config.history.entity.GenericHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通用历史记录数据访问接口
 */
@Mapper
public interface GenericHistoryMapper extends BaseMapper<GenericHistory> {

    /**
     * 查询指定实体的最大版本号
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @return 最大版本号，无记录时返回0
     */
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId}")
    Integer selectMaxVersionNo(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    /**
     * 查询指定时间点最近的版本
     *
     * @param entityType 实体类型
     * @param entityId   实体ID
     * @param targetTime 目标时间点
     * @return 该时间点最近的历史记录，无匹配时返回null
     */
    @Select("SELECT * FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId} " +
            "AND created_at <= #{targetTime} ORDER BY created_at DESC LIMIT 1")
    GenericHistory selectByVersionAtTime(@Param("entityType") String entityType, 
                                         @Param("entityId") Long entityId, 
                                         @Param("targetTime") LocalDateTime targetTime);

    /**
     * 按时间排序查询前N条历史记录
     *
     * @param entityType    实体类型
     * @param entityId      实体ID
     * @param limit         返回条数
     * @param orderDirection 排序方向 (ASC/DESC)
     * @return 历史记录列表
     */
    @Select("SELECT * FROM generic_history WHERE entity_type = #{entityType} AND entity_id = #{entityId} " +
            "ORDER BY created_at ${orderDirection} LIMIT #{limit}")
    List<GenericHistory> selectTopNByTime(@Param("entityType") String entityType, 
                                          @Param("entityId") Long entityId, 
                                          @Param("limit") int limit,
                                          @Param("orderDirection") String orderDirection);
}