package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseIndustryMapper extends BaseMapper<EnterpriseIndustry> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise_industry WHERE enterprise_id = #{enterpriseId} AND industry_id = #{industryId})")
    boolean existsByEnterpriseIdAndIndustryId(@Param("enterpriseId") Long enterpriseId, @Param("industryId") Long industryId);
}
