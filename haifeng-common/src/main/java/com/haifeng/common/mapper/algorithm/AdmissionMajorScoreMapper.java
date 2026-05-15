package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Select("SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode}")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode);

    /**
     * 批量查询专业历史数据
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
}
