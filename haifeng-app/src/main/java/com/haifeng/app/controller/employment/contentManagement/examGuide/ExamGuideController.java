package com.haifeng.app.controller.employment.contentManagement.examGuide;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/content/exam-guide")
@RequiredArgsConstructor
public class ExamGuideController {

    private final ExamGuideService examGuideService;

    @GetMapping("/list")
    public R<IPage<ExamGuideDetailVO>> list(@Valid ExamGuideQueryDTO dto) {
        return R.ok(examGuideService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<ExamGuideDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        return R.ok(examGuideService.detail(id));
    }

    @GetMapping("/list-by-type")
    public R<List<ExamGuideDetailVO>> listByType(
            @RequestParam @NotBlank @Size(max = 50) String guideCategory,
            @RequestParam(defaultValue = "备考攻略") @Size(max = 20) String guideType) {
        return R.ok(examGuideService.listByCategoryAndType(guideCategory, guideType));
    }

    @GetMapping("/{id}/view")
    public R<Void> view(@PathVariable @Min(value = 1, message = "ID必须大于0") Long id) {
        examGuideService.incrementViewCount(id);
        return R.ok();
    }

}
