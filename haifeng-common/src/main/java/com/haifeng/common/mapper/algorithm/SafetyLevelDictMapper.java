package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SafetyLevelDictMapper extends BaseMapper<SafetyLevelDict> {

    @Select("SELECT * FROM t_safety_level_dict WHERE #{coefficient} >= min_coefficient AND #{coefficient} < max_coefficient LIMIT 1")
    SafetyLevelDict selectByCoefficient(@Param("coefficient") BigDecimal coefficient);

    @Select("SELECT * FROM t_safety_level_dict ORDER BY level ASC")
    java.util.List<SafetyLevelDict> selectAll();
}
