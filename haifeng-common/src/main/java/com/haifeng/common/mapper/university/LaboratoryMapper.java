package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.Laboratory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LaboratoryMapper extends BaseMapper<Laboratory> {

    @Select("SELECT EXISTS(SELECT 1 FROM laboratories WHERE name = #{name} AND status = 1)")
    boolean existsByName(@Param("name") String name);

    @Select("SELECT EXISTS(SELECT 1 FROM laboratories WHERE university_id = #{universityId} AND name = #{name} AND status = 1)")
    boolean existsByUniversityIdAndName(@Param("universityId") Long universityId, @Param("name") String name);
}
