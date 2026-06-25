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

    @Select("SELECT reform_year FROM t_province_reform WHERE province = #{province} LIMIT 1")
    Short selectReformYearByProvince(@Param("province") String province);
}
