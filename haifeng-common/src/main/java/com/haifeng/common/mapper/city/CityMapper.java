package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.city.City;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CityMapper extends BaseMapper<City> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_city WHERE city_name = #{cityName} AND is_deleted = false)")
    boolean existsByCityName(@Param("cityName") String cityName);

    @Insert("<script>" +
            "INSERT INTO t_city (id, city_name, province, region, city_intro, college_count, key_college_count, " +
            "resident_population, gdp, is_deleted, created_at, updated_at) VALUES " +
            "<foreach collection='cities' item='city' separator=','>" +
            "(#{city.id}, #{city.cityName}, #{city.province}, #{city.region}, #{city.cityIntro}, " +
            "#{city.collegeCount}, #{city.keyCollegeCount}, #{city.residentPopulation}, #{city.gdp}, " +
            "#{city.isDeleted}, #{city.createdAt}, #{city.updatedAt})" +
            "</foreach></script>")
    void batchInsert(@Param("cities") List<City> cities);
}
