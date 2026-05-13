package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ConstraintDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ConstraintDictMapper extends BaseMapper<ConstraintDict> {
    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name}")
    int countByName(@Param("name") String name);

    @Select("SELECT COUNT(*) FROM t_constraint_dict WHERE name = #{name} AND code != #{excludeCode}")
    int countByNameExclude(@Param("name") String name, @Param("excludeCode") String excludeCode);

    @Select("SELECT code FROM t_constraint_dict WHERE name = #{name}")
    String selectCodeByName(@Param("name") String name);

    @Select("SELECT * FROM t_constraint_dict WHERE is_active = true ORDER BY sort_order ASC")
    List<ConstraintDict> selectActiveList();
}
