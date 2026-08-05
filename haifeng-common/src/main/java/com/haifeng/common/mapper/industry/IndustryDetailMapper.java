package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.industry.IndustryDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface IndustryDetailMapper extends BaseMapper<IndustryDetail> {

    /**
     * 按产业ID查询详情。
     * 必须走 MP 内置 selectOne（autoResultMap 才能套上 JsonbTypeHandler），
     * 自定义 @Select 不套 typeHandler，JSONB(Map) 字段会反序列化为 null。
     */
    default IndustryDetail findByIndustryId(Long industryId) {
        return selectOne(new LambdaQueryWrapper<IndustryDetail>()
                .eq(IndustryDetail::getIndustryId, industryId)
                .last("LIMIT 1"));
    }

    /**
     * 按产业ID查询详情（忽略逻辑删除），实现见 IndustryDetailMapper.xml，JSONB 列走 JsonbTypeHandler。
     */
    IndustryDetail findByIndustryIdIgnoreLogicDelete(@Param("industryId") Long industryId);

    /**
     * 更新行业详情（忽略逻辑删除，仅按 id），实现见 IndustryDetailMapper.xml，
     * 只更新非空字段，镜像 MP updateById 的 NOT_NULL 策略。
     */
    int updateByIdIgnoreLogicDelete(IndustryDetail detail);

    @Update("UPDATE t_industry_detail SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE industry_id = #{industryId}")
    int updateIsDeletedByIndustryId(@Param("industryId") Long industryId, @Param("isDeleted") Boolean isDeleted);

    @Delete("<script>DELETE FROM t_industry_detail WHERE industry_id IN <foreach collection='industryIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByIndustryIds(@Param("industryIds") List<Long> industryIds);

    @Insert("<script>INSERT INTO t_industry_detail (id, industry_id, industry_name, short_description, detailed_description, industry_scale, industry_talent_demand, industry_salary, policy_info, development_support_info, talent_analysis, talent_policy, salary_data, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='d' separator=','>(#{d.id}, #{d.industryId}, #{d.industryName}, #{d.shortDescription}, #{d.detailedDescription}, #{d.industryScale, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.industryTalentDemand, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.industrySalary, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.policyInfo, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.developmentSupportInfo, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.talentAnalysis, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.talentPolicy, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.salaryData, typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}, #{d.isDeleted}, #{d.createdAt}, #{d.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<IndustryDetail> list);
}
