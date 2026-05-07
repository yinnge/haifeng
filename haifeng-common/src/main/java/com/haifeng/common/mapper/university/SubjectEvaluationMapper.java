package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.SubjectEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SubjectEvaluationMapper extends BaseMapper<SubjectEvaluation> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_subject_evaluation WHERE university_id = #{universityId} AND discipline_code = #{disciplineCode} AND evaluation_round = #{evaluationRound} AND status = 1)")
    boolean existsByUniversityAndDiscipline(@Param("universityId") Long universityId,
                                            @Param("disciplineCode") String disciplineCode,
                                            @Param("evaluationRound") String evaluationRound);
}
