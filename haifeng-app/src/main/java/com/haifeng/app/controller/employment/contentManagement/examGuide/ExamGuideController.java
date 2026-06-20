package com.haifeng.app.controller.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/content/exam-guide")
@RequiredArgsConstructor
public class ExamGuideController {

    private final ExamGuideService examGuideService;

    @GetMapping("/list")
    public R<IPage<ExamGuideListVO>> list(@Valid ExamGuideQueryDTO dto) {
        return R.ok(examGuideService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}")
    public R<ExamGuideDetailVO> detail(@PathVariable Long id) {
        return R.ok(examGuideService.detail(id));
    }
}
