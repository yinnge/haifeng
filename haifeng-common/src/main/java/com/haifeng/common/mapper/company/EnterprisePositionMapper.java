package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterprisePosition;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnterprisePositionMapper extends BaseMapper<EnterprisePosition> {

    @Delete("DELETE FROM t_enterprise_position WHERE enterprise_id = #{enterpriseId}")
    int deleteByEnterpriseId(@Param("enterpriseId") Long enterpriseId);

    @Delete("<script>DELETE FROM t_enterprise_position WHERE enterprise_id IN <foreach collection='enterpriseIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByEnterpriseIds(@Param("enterpriseIds") List<Long> enterpriseIds);

    @Insert("<script>INSERT INTO t_enterprise_position (id, enterprise_id, position_name, recruitment_type, position_requirement, position_tags, province, city, work_location, education_requirement, major_requirement, work_experience, salary_min, salary_max, apply_link, deadline, position_status, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='p' separator=','>(#{p.id}, #{p.enterpriseId}, #{p.positionName}, #{p.recruitmentType}, #{p.positionRequirement}, #{p.positionTags, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{p.province}, #{p.city}, #{p.workLocation}, #{p.educationRequirement}, #{p.majorRequirement}, #{p.workExperience}, #{p.salaryMin}, #{p.salaryMax}, #{p.applyLink}, #{p.deadline}, #{p.positionStatus}, #{p.isDeleted}, #{p.createdAt}, #{p.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<EnterprisePosition> list);
}
