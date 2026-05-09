package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdmissionGroupMapper extends BaseMapper<AdmissionGroup> {

    @Select("SELECT id FROM t_admission_group " +
            "WHERE university_id = #{universityId} " +
            "AND year = #{year} " +
            "AND province = #{province} " +
            "AND subject_type = #{subjectType} " +
            "AND batch = #{batch} " +
            "AND group_code = #{groupCode} " +
            "AND is_deleted = FALSE " +
            "LIMIT 1")
    Integer selectIdByBusinessKey(
            @Param("universityId") Long universityId,
            @Param("year") Short year,
            @Param("province") String province,
            @Param("subjectType") String subjectType,
            @Param("batch") String batch,
            @Param("groupCode") String groupCode);

    @Select("SELECT * FROM fn_recalc_all_groups()")
    Integer recalcAllGroups();
}
