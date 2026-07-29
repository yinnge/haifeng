package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdmissionGroupMapper extends BaseMapper<AdmissionGroup> {

    @Select("SELECT * FROM t_admission_group WHERE id = #{id}")
    AdmissionGroup findByIdIgnoreLogicDelete(@Param("id") Integer id);

    @Update("UPDATE t_admission_group SET is_deleted = #{isDeleted} WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Integer id, @Param("isDeleted") Boolean isDeleted);

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

    @Select("<script>" +
            "SELECT * FROM t_admission_group" +
            "<where>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "<if test='universityName != null and universityName != \"\"'>" +
            "AND (university_name LIKE '%' || #{universityName} || '%' OR EXISTS (" +
            "  SELECT 1 FROM t_university WHERE id = t_admission_group.university_id AND name LIKE '%' || #{universityName} || '%'" +
            "))" +
            "</if>" +
            "<if test='year != null'>AND year = #{year}</if>" +
            "<if test='province != null and province != \"\"'>AND province = #{province}</if>" +
            "<if test='requirementType != null and requirementType != \"\"'>AND requirement_type = #{requirementType}</if>" +
            "<if test='enrollmentCode != null and enrollmentCode != \"\"'>AND enrollment_code LIKE '%' || #{enrollmentCode} || '%'</if>" +
            "<if test='groupCode != null and groupCode != \"\"'>AND group_code LIKE '%' || #{groupCode} || '%'</if>" +
            "<if test='groupName != null and groupName != \"\"'>AND group_name LIKE '%' || #{groupName} || '%'</if>" +
            "</where>" +
            "ORDER BY year DESC, university_id ASC, group_code ASC" +
            "</script>")
    IPage<AdmissionGroup> selectPageIgnoreLogicDelete(Page<AdmissionGroup> page,
                                                       @Param("isDeleted") Boolean isDeleted,
                                                       @Param("universityName") String universityName,
                                                       @Param("year") Short year,
                                                       @Param("province") String province,
                                                       @Param("requirementType") String requirementType,
                                                       @Param("enrollmentCode") String enrollmentCode,
                                                       @Param("groupCode") String groupCode,
                                                       @Param("groupName") String groupName);

    @Select("SELECT * FROM fn_recalc_all_groups()")
    Integer recalcAllGroups();

    /**
     * 批量查询历史数据
     * @param keys university_id + group_code 组合列表
     * @param province 省份
     * @param minYear 最小年份
     * @return 历史数据列表
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE is_deleted = FALSE " +
            "AND province = #{province} " +
            "AND year >= #{minYear} " +
            "AND (university_id, group_code) IN " +
            "<foreach collection='keys' item='key' open='(' separator=',' close=')'>" +
            "(#{key.universityId}, #{key.groupCode})" +
            "</foreach> " +
            "ORDER BY university_id, group_code, year DESC" +
            "</script>")
    List<AdmissionGroup> selectHistoryByKeys(@Param("keys") List<GroupKey> keys, @Param("province") String province, @Param("minYear") Short minYear);

    /**
     * 分页查询专业组（带选科筛选 + 模糊查询）
     * 注意：此 SQL 依赖 PostgreSQL 特有的数组操作符（&& 和 @>）
     */
    @Select("<script>" +
            "SELECT * FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND year = #{year} " +
            "AND is_deleted = FALSE " +
            "<if test='universityName != null and universityName != \"\"'>" +
            "AND university_name LIKE '%' || #{universityName} || '%' " +
            "</if>" +
            "<if test='cityName != null and cityName != \"\"'>" +
            "AND city_name LIKE '%' || #{cityName} || '%' " +
            "</if>" +
            "<if test='groupName != null and groupName != \"\"'>" +
            "AND group_name LIKE '%' || #{groupName} || '%' " +
            "</if>" +
            "<if test='enrollmentCode != null and enrollmentCode != \"\"'>" +
            "AND enrollment_code LIKE '%' || #{enrollmentCode} || '%' " +
            "</if>" +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND (subjects = '{}' OR subjects &amp;&amp; #{userSubjects}::text[])) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND (subjects = '{}' OR #{userSubjects}::text[] @&gt; subjects))" +
            ")" +
            "</if>" +
            "ORDER BY min_rank ASC NULLS LAST " +
            "LIMIT #{size} OFFSET #{offset}" +
            "</script>")
    List<AdmissionGroup> selectPageByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("year") Short year,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects,
            @Param("universityName") String universityName,
            @Param("cityName") String cityName,
            @Param("groupName") String groupName,
            @Param("enrollmentCode") String enrollmentCode,
            @Param("size") int size,
            @Param("offset") int offset);

    /**
     * 统计总数（带选科筛选 + 模糊查询）
     * 注意：此 SQL 依赖 PostgreSQL 特有的数组操作符（&& 和 @>）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_admission_group " +
            "WHERE province = #{province} " +
            "AND batch = #{batch} " +
            "AND year = #{year} " +
            "AND is_deleted = FALSE " +
            "<if test='universityName != null and universityName != \"\"'>" +
            "AND university_name LIKE '%' || #{universityName} || '%' " +
            "</if>" +
            "<if test='cityName != null and cityName != \"\"'>" +
            "AND city_name LIKE '%' || #{cityName} || '%' " +
            "</if>" +
            "<if test='groupName != null and groupName != \"\"'>" +
            "AND group_name LIKE '%' || #{groupName} || '%' " +
            "</if>" +
            "<if test='enrollmentCode != null and enrollmentCode != \"\"'>" +
            "AND enrollment_code LIKE '%' || #{enrollmentCode} || '%' " +
            "</if>" +
            "<if test='subjectFilter and userSubjects != null'>" +
            "AND (" +
            "  requirement_type = '不限' " +
            "  OR subjects = '{}' " +
            "  OR subjects IS NULL " +
            "  OR (requirement_type IN ('2选1', '3选1') AND (subjects = '{}' OR subjects &amp;&amp; #{userSubjects}::text[])) " +
            "  OR (requirement_type IN ('必选1', '必选2', '必选3') AND (subjects = '{}' OR #{userSubjects}::text[] @&gt; subjects))" +
            ")" +
            "</if>" +
            "</script>")
    long countByCondition(
            @Param("province") String province,
            @Param("batch") String batch,
            @Param("year") Short year,
            @Param("subjectFilter") boolean subjectFilter,
            @Param("userSubjects") String userSubjects,
            @Param("universityName") String universityName,
            @Param("cityName") String cityName,
            @Param("groupName") String groupName,
            @Param("enrollmentCode") String enrollmentCode);
}
