package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdmissionGroupMapper extends BaseMapper<AdmissionGroup> {

    @Select("SELECT id FROM t_admission_group " +
            "WHERE university_id = #{universityId} " +
            "AND year = #{year} " +
            "AND province = #{province} " +
            "AND batch = #{batch} " +
            "AND group_code = #{groupCode} " +
            "AND is_deleted = FALSE " +
            "LIMIT 1")
    Integer selectIdByBusinessKey(
            @Param("universityId") Long universityId,
            @Param("year") Short year,
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("groupCode") String groupCode);

    @Select("SELECT * FROM fn_recalc_all_groups()")
    Integer recalcAllGroups();

    /**
     * 批量查询历史数据
     * @param keys university_id + group_code 组合列表
     * @param minYear 最小年份
     * @return 历史数据列表
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE is_deleted = FALSE " +
            "AND year >= #{minYear} " +
            "AND (university_id, group_code) IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>" +
            "(#{key.universityId}, #{key.groupCode})" +
            "</foreach> " +
            "ORDER BY university_id, group_code, year DESC" +
            "</script>")
    List<AdmissionGroup> selectHistoryByKeys(@Param("keys") List<GroupKey> keys, @Param("minYear") Short minYear);

    /**
     * 分页查询专业组（带选科筛选）
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND is_deleted = FALSE " +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND subjects &amp;&amp; #{userSubjects}::text[]) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND #{userSubjects}::text[] @&gt; subjects)" +
            ")" +
            "</if>" +
            "ORDER BY min_rank ASC NULLS LAST " +
            "LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<AdmissionGroup> selectPageByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects,
            @Param("size") int size,
            @Param("offset") int offset);

    /**
     * 统计总数（带选科筛选）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND is_deleted = FALSE " +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND subjects &amp;&amp; #{userSubjects}::text[]) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND #{userSubjects}::text[] @&gt; subjects)" +
            ")" +
            "</if>" +
            "</script>")
    long countByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects);
}
