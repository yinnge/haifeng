package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.app.service.university.SubjectEvaluationService;
import com.haifeng.app.vo.university.SubjectEvaluationGradeStatsVO;
import com.haifeng.app.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端学科评估明细列表 / 等级统计（spec §3.5、§3.6）
 * 均需登录
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class SubjectEvaluationController {

    private final SubjectEvaluationService subjectEvaluationService;

    /** spec §3.5：按 universityId 分页查询学科评估明细 */
    @RequireLogin
    @GetMapping("/{universityId}/subject-evaluations")
    public R<IPage<SubjectEvaluationListVO>> list(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId,
            @Valid SubjectEvaluationQueryDTO dto) {
        return R.ok(subjectEvaluationService.page(universityId, dto));
    }

    /** spec §3.6：按 universityId 查询 9 个等级的 count 统计 */
    @RequireLogin
    @GetMapping("/{universityId}/subject-evaluations/grade-stats")
    public R<List<SubjectEvaluationGradeStatsVO>> gradeStats(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId) {
        return R.ok(subjectEvaluationService.gradeStats(universityId));
    }
}
