package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.University;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UniversityMapper extends BaseMapper<University> {

    @Select("SELECT id FROM t_universities WHERE name = #{name} AND status = 1 LIMIT 1")
    Long selectIdByName(@Param("name") String name);
}
