package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.major.PostgradMajorUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface PostgradMajorUniversityMapper extends BaseMapper<PostgradMajorUniversity> {

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major_university WHERE postgrad_major_id = #{postgradMajorId} AND university_id = #{universityId}")
    boolean existsByRelation(@Param("postgradMajorId") Long postgradMajorId, @Param("universityId") Long universityId);

    /**
     * 任务3接口1：大学 → 考研专业（联表，支持 degreeType 精准筛选）
     */
    @Select("<script>" +
        "SELECT pm.id AS id, pm.major_name AS majorName, pm.degree_type AS degreeType " +
        "FROM t_postgrad_major_university pmu " +
        "JOIN t_postgrad_major pm ON pm.id = pmu.postgrad_major_id " +
        "WHERE pmu.university_id = #{universityId} " +
        "  AND pmu.status = 1 AND pm.status = 1 " +
        "  <if test='degreeType != null and degreeType != \"\"'> " +
        "    AND pm.degree_type = #{degreeType} " +
        "  </if> " +
        "ORDER BY pmu.sort_order ASC, pm.id DESC" +
        "</script>")
    IPage<Map<String, Object>> selectPostgradMajorsByUniversity(
            Page<?> page,
            @Param("universityId") Long universityId,
            @Param("degreeType")   String degreeType);

    /**
     * 任务4接口1：考研专业 → 大学（联表，支持 category 精准筛选）
     */
    @Select("<script>" +
        "SELECT u.id AS id, u.name AS name, u.category AS category " +
        "FROM t_postgrad_major_university pmu " +
        "JOIN t_universities u ON u.id = pmu.university_id " +
        "WHERE pmu.postgrad_major_id = #{postgradMajorId} " +
        "  AND pmu.status = 1 AND u.status = 1 " +
        "  <if test='category != null and category != \"\"'> " +
        "    AND u.category = #{category} " +
        "  </if> " +
        "ORDER BY pmu.sort_order ASC, u.id DESC" +
        "</script>")
    IPage<Map<String, Object>> selectUniversitiesByPostgradMajor(
            Page<?> page,
            @Param("postgradMajorId") Long postgradMajorId,
            @Param("category")        String category);
}
