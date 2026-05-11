package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.StrongBaseScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StrongBaseScoreMapper extends BaseMapper<StrongBaseScore> {

    @Select("SELECT COUNT(*) FROM t_strong_base_score WHERE university_id = #{universityId} AND year = #{year} AND province = #{province} AND subject_type = #{subjectType} AND major_name = #{majorName}")
    int countByUnique(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName);

    @Select("SELECT COUNT(*) FROM t_strong_base_score WHERE university_id = #{universityId} AND year = #{year} AND province = #{province} AND subject_type = #{subjectType} AND major_name = #{majorName} AND id != #{excludeId}")
    int countByUniqueExclude(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName, @Param("excludeId") Long excludeId);
}
