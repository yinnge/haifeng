package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SafetyLevelDictMapper extends BaseMapper<SafetyLevelDict> {

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code}")
    int countByCode(@Param("code") String code);

    @Select("SELECT COUNT(*) FROM t_safety_level_dict WHERE code = #{code} AND level != #{excludeLevel}")
    int countByCodeExclude(@Param("code") String code, @Param("excludeLevel") Short excludeLevel);
}
