package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ProvinceReform;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProvinceReformMapper extends BaseMapper<ProvinceReform> {

    @Select("SELECT id FROM t_province_reform WHERE province = #{province} AND is_deleted = FALSE LIMIT 1")
    Long selectIdByProvince(@Param("province") String province);

    @Select("SELECT id FROM t_province_reform WHERE province = #{province} AND is_deleted = TRUE LIMIT 1")
    Long selectDeletedIdByProvince(@Param("province") String province);

    /**
     * 获取某省最早的改革年份
     * 适用于"改革后永远是新高考"的场景
     */
    @Select("SELECT MIN(reform_year) FROM t_province_reform WHERE province = #{province} AND is_deleted = FALSE")
    Short selectMinReformYearByProvince(@Param("province") String province);

    @Select("SELECT * FROM t_province_reform WHERE id = #{id} AND is_deleted = TRUE")
    ProvinceReform selectByIdIgnoreDeleted(@Param("id") Long id);

    @Update("<script>" +
            "UPDATE t_province_reform SET is_deleted = TRUE " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND is_deleted = FALSE" +
            "</script>")
    int batchSoftDelete(@Param("ids") List<Long> ids);

    @Delete("DELETE FROM t_province_reform WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_province_reform WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchHardDelete(@Param("ids") List<Long> ids);
}
