package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.Major;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MajorMapper extends BaseMapper<Major> {

    @Select("SELECT id FROM t_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT * FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    Major findByMajorName(@Param("majorName") String majorName);

    @Select("SELECT major_code FROM t_major WHERE major_name = #{majorName} AND status = 1 LIMIT 1")
    String selectCodeByName(@Param("majorName") String majorName);
}
