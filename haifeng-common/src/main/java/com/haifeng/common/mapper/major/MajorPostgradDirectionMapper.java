package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.MajorPostgradDirection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorPostgradDirectionMapper extends BaseMapper<MajorPostgradDirection> {

    @Select("SELECT COUNT(*) > 0 FROM t_major_postgrad_direction WHERE major_id = #{majorId} AND postgrad_major_id = #{postgradMajorId}")
    boolean existsByRelation(@Param("majorId") Long majorId, @Param("postgradMajorId") Long postgradMajorId);
}
