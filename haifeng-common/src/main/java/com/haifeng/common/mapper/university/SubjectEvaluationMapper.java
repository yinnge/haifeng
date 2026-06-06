package com.haifeng.common.mapper.university;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haifeng.common.entity.university.SubjectEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SubjectEvaluationMapper extends BaseMapper<SubjectEvaluation> {

    @Select("SELECT EXISTS(SELECT 1 FROM t_subject_evaluation WHERE university_id = #{universityId} AND discipline_code = #{disciplineCode} AND evaluation_round = #{evaluationRound} AND status = 1)")
    boolean existsByUniversityAndDiscipline(@Param("universityId") Long universityId,
                                            @Param("disciplineCode") String disciplineCode,
                                            @Param("evaluationRound") String evaluationRound);

    /**
     * 按等级统计该院校的学科评估数量（仅 status=1）
     * 返回示例：[{grade=A+, count=37}, {grade=A, count=25}, ...]
     * 注意：返回结果不保证 9 个等级齐全，Service 层负责补齐缺失等级为 0。
     */
    @Select("SELECT evaluation_grade AS grade, COUNT(*) AS count " +
            "FROM t_subject_evaluation " +
            "WHERE university_id = #{universityId} AND status = 1 " +
            "GROUP BY evaluation_grade")
    List<Map<String, Object>> countByGrade(@Param("universityId") Long universityId);
}
