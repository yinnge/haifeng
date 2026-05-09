package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.ScoreRank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ScoreRankMapper extends BaseMapper<ScoreRank> {

    @Select("SELECT id FROM t_score_rank WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND score = #{score} LIMIT 1")
    Long selectIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("score") Short score);

    @Select("SELECT COUNT(*) FROM t_score_rank WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND score = #{score}")
    int countByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("score") Short score);
}
