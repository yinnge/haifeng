package com.haifeng.app.controller.employment.contentManagement.examGuide;

import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.common.response.R;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/content/exam-guide")
@RequiredArgsConstructor
public class ExamGuideController {

    private final ExamGuideService examGuideService;

    @GetMapping("/list-by-type")
    public R<List<ExamGuideDetailVO>> listByType(
            @RequestParam String guideCategory,
            @RequestParam(defaultValue = "备考攻略") String guideType) {
        return R.ok(examGuideService.listByCategoryAndType(guideCategory, guideType));
    }

}
