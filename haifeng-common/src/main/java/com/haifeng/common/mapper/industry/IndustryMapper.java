package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.industry.Industry;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IndustryMapper extends BaseMapper<Industry> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_industry WHERE industry_name = #{industryName} AND is_deleted = false)")
    boolean existsByIndustryName(@Param("industryName") String industryName);

    @Select("SELECT DISTINCT category FROM t_industry WHERE is_deleted = false AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategories();

    @Insert("<script>INSERT INTO t_industry (id, industry_name, category, icon_class, description, annual_growth_rate, market_scale, talent_gap, investment_heat, growth_trend, market_trend, talent_trend, investment_trend, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='i' separator=','>(#{i.id}, #{i.industryName}, #{i.category}, #{i.iconClass}, #{i.description}, #{i.annualGrowthRate}, #{i.marketScale}, #{i.talentGap}, #{i.investmentHeat}, #{i.growthTrend}, #{i.marketTrend}, #{i.talentTrend}, #{i.investmentTrend}, #{i.isDeleted}, #{i.createdAt}, #{i.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<Industry> list);
}
