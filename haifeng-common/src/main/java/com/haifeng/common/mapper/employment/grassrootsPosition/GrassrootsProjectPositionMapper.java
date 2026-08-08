package com.haifeng.common.mapper.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.grassrootsPosition.GrassrootsProjectPosition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface GrassrootsProjectPositionMapper extends BaseMapper<GrassrootsProjectPosition> {

    /**
     * 查询所有不重复的招募年份（用于前端年份筛选下拉，按年份倒序）
     */
    @Select("SELECT DISTINCT year FROM t_grassroots_project_position WHERE is_deleted = false AND year IS NOT NULL AND year <> '' ORDER BY year DESC")
    List<String> listYears();

    /**
     * 查询所有不重复的毕业年份要求（用于前端毕业年份筛选下拉，按年份倒序）
     */
    @Select("SELECT DISTINCT grad_year_requirement FROM t_grassroots_project_position WHERE is_deleted = false AND grad_year_requirement IS NOT NULL AND grad_year_requirement <> '' ORDER BY grad_year_requirement DESC")
    List<String> listGradYears();

    /**
     * 物理删除（真实 DELETE FROM，绕过 MyBatis-Plus 逻辑删除拦截）
     */
    @Delete("DELETE FROM t_grassroots_project_position WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_grassroots_project_position WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int physicalDeleteBatchIds(@Param("ids") Collection<Long> ids);
}
