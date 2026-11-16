package com.haifeng.common.mapper.home;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.home.Planner;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlannerMapper extends BaseMapper<Planner> {
    // 规划师删除统一走逻辑删除（service 手动置 is_deleted=true），不提供物理删除
}
