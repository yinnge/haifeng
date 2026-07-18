package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface BatchScoreLineMapper extends BaseMapper<BatchScoreLine> {

    @Select("SELECT id FROM t_batch_score_line WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND batch = #{batch} AND is_deleted = FALSE LIMIT 1")
    Long selectIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("batch") String batch);

    @Select("SELECT id FROM t_batch_score_line WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND batch = #{batch} AND is_deleted = TRUE LIMIT 1")
    Long selectDeletedIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("batch") String batch);

    @Select("SELECT COUNT(*) FROM t_batch_score_line WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND batch = #{batch} AND is_deleted = FALSE")
    int countByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("batch") String batch);

    @Select({"<script>",
            "SELECT province, year, subject_type, batch FROM t_batch_score_line",
            "WHERE is_deleted = FALSE AND (",
            "<foreach collection='keys' item='k' separator=' OR '>",
            "(province = #{k.province} AND year = #{k.year} AND subject_type = #{k.subjectType} AND batch = #{k.batch})",
            "</foreach>",
            ")</script>"})
    List<BatchScoreLine> selectExistingByKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select({"<script>",
            "SELECT id, province, year, subject_type, batch FROM t_batch_score_line",
            "WHERE is_deleted = TRUE AND (",
            "<foreach collection='keys' item='k' separator=' OR '>",
            "(province = #{k.province} AND year = #{k.year} AND subject_type = #{k.subjectType} AND batch = #{k.batch})",
            "</foreach>",
            ")</script>"})
    List<BatchScoreLine> selectDeletedByKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select("SELECT * FROM t_batch_score_line WHERE id = #{id} AND is_deleted = TRUE")
    BatchScoreLine selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE t_batch_score_line SET is_deleted = TRUE " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = FALSE" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids);

    @Delete("DELETE FROM t_batch_score_line WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_batch_score_line WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchHardDelete(@Param("ids") List<Long> ids);
}
