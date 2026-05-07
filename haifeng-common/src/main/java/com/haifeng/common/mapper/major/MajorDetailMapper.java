package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.MajorDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorDetailMapper extends BaseMapper<MajorDetail> {

    @Select("SELECT * FROM t_major_detail WHERE major_id = #{majorId} AND status = 1 LIMIT 1")
    MajorDetail selectByMajorId(@Param("majorId") Long majorId);

    @Select("SELECT COUNT(*) > 0 FROM t_major_detail WHERE major_id = #{majorId}")
    boolean existsByMajorId(@Param("majorId") Long majorId);
}
