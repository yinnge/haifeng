package com.haifeng.common.mapper.algorithm;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.algorithm.BatchScoreLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BatchScoreLineMapper extends BaseMapper<BatchScoreLine> {

    @Select("SELECT id FROM t_batch_score_line WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND batch = #{batch} LIMIT 1")
    Long selectIdByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("batch") String batch);

    @Select("SELECT COUNT(*) FROM t_batch_score_line WHERE province = #{province} AND year = #{year} AND subject_type = #{subjectType} AND batch = #{batch}")
    int countByBusinessKey(@Param("province") String province, @Param("year") Short year, @Param("subjectType") String subjectType, @Param("batch") String batch);
}
