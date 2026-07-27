package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.industry.Industry;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface IndustryMapper extends BaseMapper<Industry> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_industry WHERE industry_name = #{industryName} AND is_deleted = false)")
    boolean existsByIndustryName(@Param("industryName") String industryName);

    @Select("SELECT DISTINCT category FROM t_industry WHERE is_deleted = false AND category IS NOT NULL AND category != '' ORDER BY category")
    List<String> selectDistinctCategories();

    @Select("SELECT * FROM t_industry WHERE id = #{id}")
    Industry findByIdIgnoreLogicDelete(@Param("id") Long id);

    @Update("UPDATE t_industry SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    @Delete("DELETE FROM t_industry WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_industry WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int hardDeleteBatchByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT * FROM t_industry" +
            "<where>" +
            "<if test='industryName != null and industryName != \"\"'>AND industry_name LIKE CONCAT('%', #{industryName}, '%')</if>" +
            "<if test='category != null and category != \"\"'>AND category LIKE CONCAT('%', #{category}, '%')</if>" +
            "<if test='talentTrend != null and talentTrend != \"\"'>AND talent_trend LIKE CONCAT('%', #{talentTrend}, '%')</if>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "</where>" +
            "ORDER BY category ASC, industry_name ASC" +
            "</script>")
    IPage<Industry> selectPageIgnoreLogicDelete(Page<Industry> page,
                                                @Param("industryName") String industryName,
                                                @Param("category") String category,
                                                @Param("talentTrend") String talentTrend,
                                                @Param("isDeleted") Boolean isDeleted);

    @Insert("<script>INSERT INTO t_industry (id, industry_name, category, icon_class, description, annual_growth_rate, market_scale, talent_gap, investment_heat, growth_trend, market_trend, talent_trend, investment_trend, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='i' separator=','>(#{i.id}, #{i.industryName}, #{i.category}, #{i.iconClass}, #{i.description}, #{i.annualGrowthRate}, #{i.marketScale}, #{i.talentGap}, #{i.investmentHeat}, #{i.growthTrend}, #{i.marketTrend}, #{i.talentTrend}, #{i.investmentTrend}, #{i.isDeleted}, #{i.createdAt}, #{i.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<Industry> list);
}
