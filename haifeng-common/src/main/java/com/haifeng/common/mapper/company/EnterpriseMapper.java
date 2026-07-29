package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.common.entity.company.Enterprise;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise WHERE enterprise_name = #{enterpriseName} AND is_deleted = FALSE)")
    boolean existsByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Select("SELECT id FROM t_enterprise WHERE enterprise_name = #{enterpriseName} AND is_deleted = FALSE")
    Long findIdByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Insert("<script>INSERT INTO t_enterprise (id, city_name, enterprise_name, enterprise_nature, enterprise_type, logo_url, official_website, region, enterprise_scale, main_business, enterprise_intro, recruitment_status, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='e' separator=','>(#{e.id}, #{e.cityName}, #{e.enterpriseName}, #{e.enterpriseNature}, #{e.enterpriseType}, #{e.logoUrl}, #{e.officialWebsite}, #{e.region}, #{e.enterpriseScale}, #{e.mainBusiness}, #{e.enterpriseIntro}, #{e.recruitmentStatus}, #{e.isDeleted}, #{e.createdAt}, #{e.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<Enterprise> list);

    @Select("<script>" +
            "SELECT * FROM t_enterprise " +
            "<where>" +
            "<if test='params.cityName != null and params.cityName != \"\"'>AND city_name LIKE CONCAT('%', #{params.cityName}, '%')</if>" +
            "<if test='params.enterpriseName != null and params.enterpriseName != \"\"'>AND enterprise_name LIKE CONCAT('%', #{params.enterpriseName}, '%')</if>" +
            "<if test='params.enterpriseType != null and params.enterpriseType != \"\"'>AND enterprise_type LIKE CONCAT('%', #{params.enterpriseType}, '%')</if>" +
            "<if test='params.enterpriseNature != null and params.enterpriseNature != \"\"'>AND enterprise_nature = #{params.enterpriseNature}</if>" +
            "<if test='params.recruitmentStatus != null and params.recruitmentStatus != \"\"'>AND recruitment_status = #{params.recruitmentStatus}</if>" +
            "<if test='params.isDeleted != null'>AND is_deleted = #{params.isDeleted}</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<Enterprise> selectPageCustom(Page<?> page, @Param("params") Map<String, Object> params);

    @Select("SELECT * FROM t_enterprise WHERE id = #{id}")
    Enterprise selectByIdCustom(@Param("id") Long id);

    @Update("UPDATE t_enterprise SET is_deleted = #{isDeleted}, updated_at = NOW() WHERE id = #{id}")
    int updateIsDeletedById(@Param("id") Long id, @Param("isDeleted") Boolean isDeleted);

    @Update("<script>" +
            "UPDATE t_enterprise SET " +
            "city_name = #{e.cityName}, " +
            "enterprise_name = #{e.enterpriseName}, " +
            "enterprise_nature = #{e.enterpriseNature}, " +
            "enterprise_type = #{e.enterpriseType}, " +
            "logo_url = #{e.logoUrl}, " +
            "official_website = #{e.officialWebsite}, " +
            "region = #{e.region}, " +
            "enterprise_scale = #{e.enterpriseScale}, " +
            "main_business = #{e.mainBusiness}, " +
            "enterprise_intro = #{e.enterpriseIntro}, " +
            "recruitment_status = #{e.recruitmentStatus}, " +
            "updated_at = #{e.updatedAt} " +
            "WHERE id = #{e.id}" +
            "</script>")
    void updateEntityById(@Param("e") Enterprise enterprise);

    @Delete("DELETE FROM t_enterprise WHERE id = #{id}")
    void deletePhysicallyById(@Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM t_enterprise WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    void deletePhysicallyBatch(@Param("ids") List<Long> ids);
}
