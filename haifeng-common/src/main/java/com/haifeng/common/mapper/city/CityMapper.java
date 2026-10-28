package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.city.City;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CityMapper extends BaseMapper<City> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_city WHERE city_name = #{cityName} AND is_deleted = false)")
    boolean existsByCityName(@Param("cityName") String cityName);

    @Insert("<script>" +
            "INSERT INTO t_city (id, city_name, province, region, city_intro, college_count, key_college_count, " +
            "resident_population, gdp, cover_image_url, is_deleted, created_at, updated_at) VALUES " +
            "<foreach collection='cities' item='city' separator=','>" +
            "(#{city.id}, #{city.cityName}, #{city.province}, #{city.region}, #{city.cityIntro}, " +
            "#{city.collegeCount}, #{city.keyCollegeCount}, #{city.residentPopulation}, #{city.gdp}, " +
            "#{city.coverImageUrl}, #{city.isDeleted}, #{city.createdAt}, #{city.updatedAt})" +
            "</foreach></script>")
    void batchInsert(@Param("cities") List<City> cities);

    @Update("UPDATE t_city SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    /**
     * 自定义全量更新（绕过 MP 全局逻辑删除过滤器，可更新已禁用记录）。
     */
    @Update("UPDATE t_city SET " +
            "city_name = #{cityName}, province = #{province}, region = #{region}, " +
            "city_intro = #{cityIntro}, college_count = #{collegeCount}, " +
            "key_college_count = #{keyCollegeCount}, resident_population = #{residentPopulation}, " +
            "gdp = #{gdp}, cover_image_url = #{coverImageUrl}, updated_at = NOW() " +
            "WHERE id = #{id}")
    int updateByIdCustom(City city);

    @Select("SELECT * FROM t_city WHERE id = #{id}")
    City findByIdIgnoreLogicDelete(@Param("id") Long id);

    /**
     * 城市数据概览聚合：城市总数 / 高校总数 / 常住人口合计(万人) / GDP合计(亿元)。
     * 别名带双引号，PostgreSQL 保留大小写，返回 Map key 与此一致。
     */
    @Select("SELECT COUNT(*) AS \"cityCount\", COALESCE(SUM(college_count), 0) AS \"collegeTotal\", " +
            "COALESCE(SUM(resident_population), 0) AS \"populationTotal\", COALESCE(SUM(gdp), 0) AS \"gdpTotal\" " +
            "FROM t_city WHERE is_deleted = false")
    Map<String, Object> selectOverviewStats();

    @Delete("DELETE FROM t_city WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("<script>DELETE FROM t_city WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int hardDeleteBatchByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT * FROM t_city" +
            "<where>" +
            "<if test='isDeleted != null'>AND is_deleted = #{isDeleted}</if>" +
            "<if test='cityName != null and cityName != \"\"'>AND city_name LIKE CONCAT('%', #{cityName}, '%')</if>" +
            "<if test='province != null and province != \"\"'>AND province LIKE CONCAT('%', #{province}, '%')</if>" +
            "<if test='region != null and region != \"\"'>AND region LIKE CONCAT('%', #{region}, '%')</if>" +
            "</where>" +
            "ORDER BY province ASC, city_name ASC" +
            "</script>")
    IPage<City> selectPageIgnoreLogicDelete(Page<City> page, @Param("isDeleted") Boolean isDeleted,
            @Param("cityName") String cityName, @Param("province") String province, @Param("region") String region);
}
