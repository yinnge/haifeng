package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProvinceReformMapper extends BaseMapper<ProvinceReform> {

    @Select("SELECT id FROM t_province_reform WHERE province = #{province} LIMIT 1")
    Long selectIdByProvince(@Param("province") String province);

    /**
     * 获取某省最早的改革年份
     * 适用于"改革后永远是新高考"的场景
     */
    @Select("SELECT MIN(reform_year) FROM t_province_reform WHERE province = #{province}")
    Short selectMinReformYearByProvince(@Param("province") String province);
}
