package com.haifeng.common.mapper.industry;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.industry.IndustryDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IndustryDetailMapper extends BaseMapper<IndustryDetail> {

    @Select("SELECT * FROM t_industry_detail WHERE industry_id = #{industryId} AND is_deleted = false LIMIT 1")
    IndustryDetail findByIndustryId(@Param("industryId") Long industryId);
}
