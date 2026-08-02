package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.PostgradMajor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PostgradMajorMapper extends BaseMapper<PostgradMajor> {

    @Select("SELECT id FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT major_name FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    String selectNameByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT id FROM t_postgrad_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    Long selectIdByName(@Param("majorName") String majorName);

    @Select("SELECT DISTINCT discipline_category FROM t_postgrad_major " +
            "WHERE status = 1 AND discipline_category IS NOT NULL AND discipline_category <> '' " +
            "ORDER BY discipline_category")
    List<String> selectDistinctDisciplineCategories();
}
