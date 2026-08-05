package com.haifeng.common.mapper.employment.civilService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.employment.civilService.InstitutionPosition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface InstitutionPositionMapper extends BaseMapper<InstitutionPosition> {

    /**
     * 物理删除（真实 DELETE FROM，绕过 MyBatis-Plus 逻辑删除拦截）
     */
    @Delete("DELETE FROM t_institution_position WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_institution_position WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int physicalDeleteBatchIds(@Param("ids") Collection<Long> ids);

    /**
     * 查询考试类别的去重值列表（仅未删除的记录）
     */
    @Select("SELECT DISTINCT exam_category FROM t_institution_position WHERE exam_category IS NOT NULL AND exam_category != '' AND is_deleted = false ORDER BY exam_category")
    List<String> selectDistinctExamCategories();

    /**
     * 查询职位类型的去重值列表（仅未删除的记录）
     */
    @Select("SELECT DISTINCT position_type FROM t_institution_position WHERE position_type IS NOT NULL AND position_type != '' AND is_deleted = false ORDER BY position_type")
    List<String> selectDistinctPositionTypes();

    /**
     * 查询特殊岗位的去重值列表（仅未删除的记录）
     */
    @Select("SELECT DISTINCT special_position FROM t_institution_position WHERE special_position IS NOT NULL AND special_position != '' AND is_deleted = false ORDER BY special_position")
    List<String> selectDistinctSpecialPositions();
}
