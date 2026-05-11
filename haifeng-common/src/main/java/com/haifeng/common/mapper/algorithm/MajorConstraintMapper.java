package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.MajorConstraint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorConstraintMapper extends BaseMapper<MajorConstraint> {
    @Select("SELECT COUNT(*) FROM t_major_constraint WHERE major_code = #{majorCode} AND constraint_code = #{constraintCode}")
    int countByBusinessKey(@Param("majorCode") String majorCode, @Param("constraintCode") String constraintCode);
}
