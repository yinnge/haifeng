package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.StrongBaseUniversity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StrongBaseUniversityMapper extends BaseMapper<StrongBaseUniversity> {

    @Select("SELECT COUNT(*) FROM t_strong_base_university WHERE university_id = #{universityId}")
    int countByUniversityId(@Param("universityId") Long universityId);

    @Select("SELECT COUNT(*) FROM t_strong_base_university WHERE university_id = #{universityId} AND id != #{excludeId}")
    int countByUniversityIdExclude(@Param("universityId") Long universityId, @Param("excludeId") Long excludeId);
}
