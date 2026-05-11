package com.haifeng.common.mapper.company;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.company.Enterprise;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_enterprise WHERE enterprise_name = #{enterpriseName})")
    boolean existsByEnterpriseName(@Param("enterpriseName") String enterpriseName);

    @Select("SELECT id FROM t_enterprise WHERE enterprise_name = #{enterpriseName}")
    Long findIdByEnterpriseName(@Param("enterpriseName") String enterpriseName);
}
