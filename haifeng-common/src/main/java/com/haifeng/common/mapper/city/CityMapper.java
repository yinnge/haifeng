package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.city.City;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CityMapper extends BaseMapper<City> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_city WHERE city_name = #{cityName} AND is_deleted = false)")
    boolean existsByCityName(@Param("cityName") String cityName);
}
