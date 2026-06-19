package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.industry.Industry;
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
}
