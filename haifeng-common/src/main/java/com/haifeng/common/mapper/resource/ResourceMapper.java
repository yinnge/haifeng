package com.haifeng.common.mapper.resource;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.resource.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.OffsetDateTime;
import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE t_resource SET view_count = view_count + 1 WHERE id = #{id} AND is_deleted = false")
    int incrementViewCount(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE t_resource SET is_deleted = true, updated_at = #{now} " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = false" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids, @Param("now") OffsetDateTime now);

    @Select("SELECT DISTINCT category FROM t_resource WHERE is_deleted = false AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategories();
}
