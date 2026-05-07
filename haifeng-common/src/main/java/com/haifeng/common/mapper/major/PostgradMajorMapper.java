package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.PostgradMajor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostgradMajorMapper extends BaseMapper<PostgradMajor> {

    @Select("SELECT id FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    Long selectIdByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT major_name FROM t_postgrad_major WHERE major_code = #{majorCode} AND status = 1 LIMIT 1")
    String selectNameByMajorCode(@Param("majorCode") String majorCode);

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major WHERE major_code = #{majorCode}")
    boolean existsByMajorCode(@Param("majorCode") String majorCode);
}
