package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.config.StringListTypeHandler;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import com.haifeng.common.entity.algorithm.MajorHistoryItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Update("UPDATE t_admission_major_score SET is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Integer id, @Param("isDeleted") Boolean isDeleted);

    /**
     * 自定义全量更新（绕过 MP 全局逻辑删除过滤器，可更新已禁用记录）。
     * constraints 是 PostgreSQL text[] 数组列，通过 typeHandler 显式指定。
     */
    @Update("UPDATE t_admission_major_score SET " +
            "group_id = #{groupId}, major_id = #{majorId}, " +
            "major_code = #{majorCode}, major_name = #{majorName}, " +
            "education_level = #{educationLevel}, duration = #{duration}, " +
            "tuition = #{tuition}, description = #{description}, " +
            "admission_count = #{admissionCount}, " +
            "min_score = #{minScore}, min_rank = #{minRank}, " +
            "avg_score = #{avgScore}, avg_rank = #{avgRank}, " +
            "max_score = #{maxScore}, max_rank = #{maxRank}, " +
            "constraints = #{constraints,typeHandler=com.haifeng.common.config.StringListTypeHandler}, " +
            "updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateByIdCustom(AdmissionMajorScore entity);

    @Select("<script>" +
            "SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode} " +
            "AND is_deleted = FALSE " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId}" +
            "</if>" +
            "</script>")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode,
            @Param("excludeId") Integer excludeId);

    /**
     * 分页查询（含已禁用）。constraints 是数组列，
     * 自定义 @Select 不套 typeHandler，必须用 @Results 显式声明 StringListTypeHandler。
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_major_score " +
            "<where>" +
            "<if test='params.isDeleted != null'>AND is_deleted = #{params.isDeleted}</if>" +
            "<if test='params.groupId != null'>AND group_id = #{params.groupId}</if>" +
            "<if test='params.majorCode != null and params.majorCode != \"\"'>AND major_code LIKE CONCAT('%', #{params.majorCode}, '%')</if>" +
            "<if test='params.majorName != null and params.majorName != \"\"'>AND major_name LIKE CONCAT('%', #{params.majorName}, '%')</if>" +
            "<if test='params.educationLevel != null and params.educationLevel != \"\"'>AND education_level = #{params.educationLevel}</if>" +
            "</where>" +
            "ORDER BY major_code ASC, id ASC" +
            "</script>")
    @Results({
            @Result(column = "constraints", property = "constraints", typeHandler = StringListTypeHandler.class)
    })
    IPage<AdmissionMajorScore> selectPageCustom(Page<?> page, @Param("params") Map<String, Object> params);

    /**
     * 按ID查询（自定义SQL绕过全局逻辑删除，可查到已禁用记录）。
     * constraints 是数组列，必须用 @Results 显式声明 StringListTypeHandler。
     */
    @Select("SELECT * FROM t_admission_major_score WHERE id = #{id}")
    @Results({
            @Result(column = "constraints", property = "constraints", typeHandler = StringListTypeHandler.class)
    })
    AdmissionMajorScore selectByIdCustom(@Param("id") Integer id);

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
