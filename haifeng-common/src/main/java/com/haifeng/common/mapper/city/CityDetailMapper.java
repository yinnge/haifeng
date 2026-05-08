package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.city.CityDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CityDetailMapper extends BaseMapper<CityDetail> {

    @Select("SELECT * FROM t_city_detail WHERE city_id = #{cityId} AND is_deleted = false LIMIT 1")
    CityDetail findByCityId(@Param("cityId") Long cityId);
}
