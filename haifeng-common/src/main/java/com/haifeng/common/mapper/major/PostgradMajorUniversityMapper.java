package com.haifeng.common.mapper.major;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.major.PostgradMajorUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostgradMajorUniversityMapper extends BaseMapper<PostgradMajorUniversity> {

    @Select("SELECT COUNT(*) > 0 FROM t_postgrad_major_university WHERE postgrad_major_id = #{postgradMajorId} AND university_id = #{universityId}")
    boolean existsByRelation(@Param("postgradMajorId") Long postgradMajorId, @Param("universityId") Long universityId);
}
