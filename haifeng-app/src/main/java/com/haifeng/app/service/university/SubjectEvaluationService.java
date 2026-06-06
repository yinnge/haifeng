package com.haifeng.app.service.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;

import java.util.List;

public interface SubjectEvaluationService {

    /**
     * 按 universityId 分页查询学科评估明细（仅 status=1）
     * 排序 evaluation_grade ASC, sort_order ASC
     */
    IPage<SubjectEvaluationListVO> page(Long universityId, SubjectEvaluationQueryDTO dto);

    /**
     * 按等级统计该院校的学科评估数量
     * 返回固定 9 条，按 ['A+','A','A-','B+','B','B-','C+','C','C-'] 顺序，缺失等级 count=0
     */
    List<SubjectEvaluationGradeStatsVO> gradeStats(Long universityId);
}
