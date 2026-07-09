package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.industry.IndustryDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IndustryDetailMapper extends BaseMapper<IndustryDetail> {

    @Select("SELECT * FROM t_industry_detail WHERE industry_id = #{industryId} AND is_deleted = false LIMIT 1")
    IndustryDetail findByIndustryId(@Param("industryId") Long industryId);

    @Delete("<script>DELETE FROM t_industry_detail WHERE industry_id IN <foreach collection='industryIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByIndustryIds(@Param("industryIds") List<Long> industryIds);

    @Insert("<script>INSERT INTO t_industry_detail (id, industry_id, industry_name, short_description, detailed_description, industry_scale, industry_talent_demand, industry_salary, policy_info, development_support_info, talent_analysis, talent_policy, salary_data, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='d' separator=','>(#{d.id}, #{d.industryId}, #{d.industryName}, #{d.shortDescription}, #{d.detailedDescription}, #{d.industryScale, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.industryTalentDemand, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.industrySalary, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.policyInfo, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.developmentSupportInfo, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.talentAnalysis, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.talentPolicy, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.salaryData, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.isDeleted}, #{d.createdAt}, #{d.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<IndustryDetail> list);
}
