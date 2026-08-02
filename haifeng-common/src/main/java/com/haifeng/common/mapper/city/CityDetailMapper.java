package com.haifeng.common.mapper.city;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.city.CityDetail;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CityDetailMapper extends BaseMapper<CityDetail> {

    /**
     * 按城市ID查询详情。
     * 注意：必须走 MP 内置 selectOne（autoResultMap 才能套上 JsonbTypeHandler/StringListTypeHandler），
     * 自定义 @Select 不会应用 typeHandler，JSONB/数组字段会反序列化为 null（回显丢失）。
     */
    default CityDetail findByCityId(Long cityId) {
        return selectOne(new LambdaQueryWrapper<CityDetail>()
                .eq(CityDetail::getCityId, cityId)
                .eq(CityDetail::getIsDeleted, false)
                .last("LIMIT 1"));
    }

    @Delete("<script>DELETE FROM t_city_detail WHERE city_id IN <foreach collection='cityIds' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    void deleteByCityIds(@Param("cityIds") List<Long> cityIds);

    @Insert("<script>" +
            "INSERT INTO t_city_detail (id, city_id, city_name, is_deleted, created_at, updated_at) VALUES " +
            "<foreach collection='details' item='d' separator=','>" +
            "(#{d.id}, #{d.cityId}, #{d.cityName}, #{d.isDeleted}, #{d.createdAt}, #{d.updatedAt})" +
            "</foreach></script>")
    void batchInsert(@Param("details") List<CityDetail> details);

    @Update("UPDATE t_city_detail SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE city_id = #{cityId}")
    int updateIsDeletedByCityId(@Param("cityId") Long cityId, @Param("isDeleted") Boolean isDeleted);

    @Delete("DELETE FROM t_city_detail WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
