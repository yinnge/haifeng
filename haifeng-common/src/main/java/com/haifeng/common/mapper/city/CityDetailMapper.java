package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.city.CityDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CityDetailMapper extends BaseMapper<CityDetail> {

    @Select("SELECT * FROM t_city_detail WHERE city_id = #{cityId} AND is_deleted = false LIMIT 1")
    CityDetail findByCityId(@Param("cityId") Long cityId);

    @Delete("<script>DELETE FROM t_city_detail WHERE city_id IN <foreach collection='cityIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    void deleteByCityIds(@Param("cityIds") List<Long> cityIds);

    @Insert("<script>" +
            "INSERT INTO t_city_detail (id, city_id, city_name, is_deleted, created_at, updated_at) VALUES " +
            "<foreach collection='details' item='d' separator=','>" +
            "(#{d.id}, #{d.cityId}, #{d.cityName}, #{d.isDeleted}, #{d.createdAt}, #{d.updatedAt})" +
            "</foreach></script>")
    void batchInsert(@Param("details") List<CityDetail> details);
}
