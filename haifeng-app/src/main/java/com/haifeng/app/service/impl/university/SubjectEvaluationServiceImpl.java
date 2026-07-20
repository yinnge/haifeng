package com.haifeng.app.service.impl.university;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.service.university.SubjectEvaluationService;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.entity.university.SubjectEvaluation;
import com.haifeng.common.entity.university.University;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.university.SubjectEvaluationMapper;
import com.haifeng.common.mapper.university.UniversityMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectEvaluationServiceImpl implements SubjectEvaluationService {

    private static final short STATUS_PUBLISHED = 1;

    /** 固定 9 个等级的输出顺序（spec §3.6） */
    private static final List<String> GRADE_ORDER = List.of(
            "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-");

    private final SubjectEvaluationMapper subjectEvaluationMapper;
    private final UniversityMapper universityMapper;

    @Override
    public IPage<SubjectEvaluationListVO> page(Long universityId, SubjectEvaluationQueryDTO dto) {
        University univ = universityMapper.selectById(universityId);
        if (univ == null || univ.getStatus() == null || univ.getStatus() != STATUS_PUBLISHED) {
            log.debug("院校不存在或已下架, universityId={}", universityId);
            throw new BusinessException(ResultCode.NOT_FOUND, "院校不存在");
        }

        Page<SubjectEvaluation> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<SubjectEvaluation> wrapper = new LambdaQueryWrapper<SubjectEvaluation>()
                .eq(SubjectEvaluation::getUniversityId, universityId)
                .eq(SubjectEvaluation::getStatus, STATUS_PUBLISHED)
                // evaluation_grade 是字符串列，按 ASCII 排会得到 A, A+, A-, B, B+...（顺序错误）。
                // 使用 CASE 映射到分数学意义上的等级顺序 A+ → C-，再按 sort_order 兜底。
                .last("ORDER BY CASE evaluation_grade " +
                        "WHEN 'A+' THEN 1 WHEN 'A' THEN 2 WHEN 'A-' THEN 3 " +
                        "WHEN 'B+' THEN 4 WHEN 'B' THEN 5 WHEN 'B-' THEN 6 " +
                        "WHEN 'C+' THEN 7 WHEN 'C' THEN 8 WHEN 'C-' THEN 9 " +
                        "ELSE 99 END ASC, sort_order ASC");

        IPage<SubjectEvaluation> entityPage = subjectEvaluationMapper.selectPage(page, wrapper);
        return entityPage.convert(this::toListVO);
    }

    @Override
    public List<SubjectEvaluationGradeStatsVO> gradeStats(Long universityId) {
        List<Map<String, Object>> rows = subjectEvaluationMapper.countByGrade(universityId);

        // 把 mapper 结果归一化成 grade -> count
        Map<String, Integer> existing = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object g = row.get("grade");
            Object c = row.get("count");
            if (g != null && c != null) {
                existing.put(String.valueOf(g), ((Number) c).intValue());
            }
        }

        // 按固定 9 个等级顺序输出，缺失补 0
        return GRADE_ORDER.stream()
                .map(grade -> SubjectEvaluationGradeStatsVO.builder()
                        .grade(grade)
                        .count(existing.getOrDefault(grade, 0))
                        .build())
                .collect(Collectors.toList());
    }

    private SubjectEvaluationListVO toListVO(SubjectEvaluation e) {
        return SubjectEvaluationListVO.builder()
                .disciplineCode(e.getDisciplineCode())
                .disciplineName(e.getDisciplineName())
                .evaluationRound(e.getEvaluationRound())
                .evaluationGrade(e.getEvaluationGrade())
                .build();
    }
}
