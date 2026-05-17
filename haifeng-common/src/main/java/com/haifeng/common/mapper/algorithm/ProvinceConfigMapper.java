package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ProvinceConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProvinceConfigMapper extends BaseMapper<ProvinceConfig> {

    @Select("SELECT * FROM t_province_config WHERE province = #{province}")
    ProvinceConfig selectByProvince(@Param("province") String province);
}
