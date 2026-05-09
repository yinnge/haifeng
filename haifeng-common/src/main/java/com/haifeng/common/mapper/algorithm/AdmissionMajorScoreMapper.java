package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.AdmissionMajorScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdmissionMajorScoreMapper extends BaseMapper<AdmissionMajorScore> {

    @Select("SELECT COUNT(*) FROM t_admission_major_score " +
            "WHERE group_id = #{groupId} AND major_code = #{majorCode}")
    int countByGroupIdAndMajorCode(
            @Param("groupId") Integer groupId,
            @Param("majorCode") String majorCode);
}
