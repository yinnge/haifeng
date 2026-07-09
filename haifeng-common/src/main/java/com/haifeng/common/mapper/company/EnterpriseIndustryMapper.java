package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.EnterpriseIndustry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EnterpriseIndustryMapper extends BaseMapper<EnterpriseIndustry> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise_industry WHERE enterprise_id = #{enterpriseId} AND industry_id = #{industryId})")
    boolean existsByEnterpriseIdAndIndustryId(@Param("enterpriseId") Long enterpriseId, @Param("industryId") Long industryId);

    @Delete("<script>DELETE FROM t_enterprise_industry WHERE enterprise_id IN <foreach collection='enterpriseIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int deleteByEnterpriseIds(@Param("enterpriseIds") List<Long> enterpriseIds);

    @Insert("<script>INSERT INTO t_enterprise_industry (id, enterprise_id, enterprise_name, industry_id, industry_name, is_primary, sort_order, created_at) VALUES <foreach collection='list' item='e' separator=','>(#{e.id}, #{e.enterpriseId}, #{e.enterpriseName}, #{e.industryId}, #{e.industryName}, #{e.isPrimary}, #{e.sortOrder}, #{e.createdAt})</foreach></script>")
    void insertBatch(@Param("list") List<EnterpriseIndustry> list);
}
