package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.Enterprise;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise WHERE enterprise_name = #{enterpriseName} AND is_deleted = FALSE)")
    boolean existsByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Select("SELECT id FROM t_enterprise WHERE enterprise_name = #{enterpriseName} AND is_deleted = FALSE")
    Long findIdByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Insert("<script>INSERT INTO t_enterprise (id, city_name, enterprise_name, enterprise_nature, enterprise_type, logo_url, official_website, region, enterprise_scale, main_business, enterprise_intro, recruitment_status, is_deleted, created_at, updated_at) VALUES <foreach collection='list' item='e' separator=','>(#{e.id}, #{e.cityName}, #{e.enterpriseName}, #{e.enterpriseNature}, #{e.enterpriseType}, #{e.logoUrl}, #{e.officialWebsite}, #{e.region}, #{e.enterpriseScale}, #{e.mainBusiness}, #{e.enterpriseIntro}, #{e.recruitmentStatus}, #{e.isDeleted}, #{e.createdAt}, #{e.updatedAt})</foreach></script>")
    void insertBatch(@Param("list") List<Enterprise> list);
}
