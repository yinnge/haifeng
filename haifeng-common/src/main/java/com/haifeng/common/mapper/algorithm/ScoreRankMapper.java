package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ScoreRank;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreRankMapper extends BaseMapper<ScoreRank> {

    @Select("SELECT id FROM t_score_rank WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND score = #{score} AND is_deleted = FALSE LIMIT 1")
    Long selectIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("score") Short score);

    @Select("SELECT id FROM t_score_rank WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND score = #{score} AND is_deleted = TRUE LIMIT 1")
    Long selectDeletedIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("score") Short score);

    @Select("SELECT COUNT(*) FROM t_score_rank WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND score = #{score} AND is_deleted = FALSE")
    int countByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("score") Short score);

    @Select("<script>" +
            "SELECT CONCAT(province, '_', CAST(year AS VARCHAR), '_', subject_type, '_', CAST(score AS VARCHAR)) AS business_key " +
            "FROM t_score_rank " +
            "WHERE is_deleted = FALSE AND " +
            "<foreach collection='keys' item='k' open='(' separator=') OR (' close=')'>" +
            "province = #{k.province} AND year = #{k.year} AND subject_type = #{k.subjectType} AND score = #{k.score}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select("<script>" +
            "SELECT CONCAT(province, '_', CAST(year AS VARCHAR), '_', subject_type, '_', CAST(score AS VARCHAR)) AS business_key " +
            "FROM t_score_rank " +
            "WHERE is_deleted = TRUE AND " +
            "<foreach collection='keys' item='k' open='(' separator=') OR (' close=')'>" +
            "province = #{k.province} AND year = #{k.year} AND subject_type = #{k.subjectType} AND score = #{k.score}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingDeletedKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select({"<script>",
            "SELECT id, province, year, subject_type, score FROM t_score_rank",
            "WHERE is_deleted = TRUE AND (",
            "<foreach collection='keys' item='k' separator=' OR '>",
            "(province = #{k.province} AND year = #{k.year} AND subject_type = #{k.subjectType} AND score = #{k.score})",
            "</foreach>",
            ")</script>"})
    List<ScoreRank> selectDeletedByKeys(@Param("keys") List<Map<String, Object>> keys);

    @Select("SELECT CASE WHEN cumulative_count > 0 THEN " +
            "CAST(same_score_count AS DECIMAL) / cumulative_count " +
            "ELSE NULL END " +
            "FROM t_score_rank " +
            "WHERE province = #{province} AND year = #{year} " +
            "AND subject_type = #{subjectType} AND score = #{score} " +
            "AND is_deleted = FALSE " +
            "LIMIT 1")
    BigDecimal selectDensity(@Param("province") String province,
                             @Param("year") Short year,
                             @Param("subjectType") String subjectType,
                             @Param("score") Integer score);

    @Select("SELECT * FROM t_score_rank WHERE id = #{id} AND is_deleted = TRUE")
    ScoreRank selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE t_score_rank SET is_deleted = TRUE " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = FALSE" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids);

    @Delete("DELETE FROM t_score_rank WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_score_rank WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchHardDelete(@Param("ids") List<Long> ids);

    @Insert("<script>" +
            "INSERT INTO t_score_rank (id, province, year, subject_type, score, rank, same_score_count, cumulative_count, is_deleted) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id}, #{item.province}, #{item.year}, #{item.subjectType}, #{item.score}, #{item.rank}, #{item.sameScoreCount}, #{item.cumulativeCount}, FALSE)" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<ScoreRank> list);
}
