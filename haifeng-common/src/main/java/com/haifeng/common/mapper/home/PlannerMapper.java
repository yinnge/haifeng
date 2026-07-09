package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Planner;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlannerMapper extends BaseMapper<Planner> {

    @Delete("DELETE FROM t_planners WHERE id = #{id}")
    int hardDeleteById(@Param("id") Long id);
}
