package com.haifeng.common.mapper.special;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.special.StrongBaseScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StrongBaseScoreMapper extends BaseMapper<StrongBaseScore> {

    int countByUnique(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName);

    int countByUniqueExclude(@Param("universityId") Long universityId, @Param("year") Short year, @Param("province") String province, @Param("subjectType") String subjectType, @Param("majorName") String majorName, @Param("excludeId") Long excludeId);
}
