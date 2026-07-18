package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MajorHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Update("UPDATE t_admission_major_score SET is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Integer id, @Param("isDeleted") Boolean isDeleted);

    @Select("SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode} " +
            "AND is_deleted = FALSE " +
            "AND (#{excludeId} IS NULL OR id != #{excludeId})")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode,
            @Param("excludeId") Integer excludeId);

    /**
     * 批量查询专业历史数据（原始 Map 形式）
     * @param universityId 大学ID
     * @param majorCodes 专业代码列表
     * @param minYear 最小年份
     * @return 历史数据列表
     */
    @Select("<script>" +
            "SELECT ams.*, ag.year " +
            "FROM t_admission_major_score ams " +
            "INNER JOIN t_admission_group ag ON ams.group_id = ag.id " +
            "WHERE ag.university_id = #{universityId} " +
            "AND ag.is_deleted = FALSE " +
            "AND ams.is_deleted = FALSE " +
            "AND ag.year >= #{minYear} " +
            "AND ams.major_code IN " +
            "<foreach collection='majorCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach> " +
            "ORDER BY ams.major_code, ag.year DESC" +
            "</script>")
    List<Map<String, Object>> selectHistoryByMajorCodes(
            @Param("universityId") Long universityId,
            @Param("majorCodes") List<String> majorCodes,
            @Param("minYear") Short minYear);

    /**
     * 查询某大学下指定专业组的某些专业的近 N 年历史录取项
     * 排除自身（excludeGroupId），仅返回 minYear 之后的记录
     */
    @Select("<script>" +
            "SELECT ams.major_code AS majorCode, " +
            "       ag.year AS year, " +
            "       ams.min_score AS minScore, " +
            "       ams.min_rank AS minRank, " +
            "       ams.avg_score AS avgScore, " +
            "       ams.avg_rank AS avgRank, " +
            "       ams.max_score AS maxScore, " +
            "       ams.max_rank AS maxRank, " +
            "       ams.admission_count AS admissionCount " +
            "FROM t_admission_major_score ams " +
            "INNER JOIN t_admission_group ag ON ams.group_id = ag.id " +
            "WHERE ag.university_id = #{universityId} " +
            "AND ag.is_deleted = FALSE " +
            "AND ams.is_deleted = FALSE " +
            "AND ag.id != #{excludeGroupId} " +
            "AND ag.year >= #{minYear} " +
            "AND ams.major_code IN " +
            "<foreach collection='majorCodes' item='code' open='(' separator=',' close=')'>" +
            "#{code}" +
            "</foreach> " +
            "ORDER BY ams.major_code, ag.year DESC" +
            "</script>")
    List<MajorHistoryItem> selectMajorHistoryItems(
            @Param("universityId") Long universityId,
            @Param("excludeGroupId") Integer excludeGroupId,
            @Param("majorCodes") List<String> majorCodes,
            @Param("minYear") Short minYear);
}
