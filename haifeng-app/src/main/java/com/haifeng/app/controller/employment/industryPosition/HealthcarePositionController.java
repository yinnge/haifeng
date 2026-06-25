package com.haifeng.app.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.dto.employment.industryPosition.HealthcarePositionSearchDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionDetailVO;
import com.haifeng.app.vo.employment.industryPosition.HealthcarePositionListVO;
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
@RequestMapping("/api/v1/app/employment/healthcare")
@RequiredArgsConstructor
public class HealthcarePositionController {

    private final HealthcarePositionService healthcarePositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<HealthcarePositionListVO>> list(@Valid HealthcarePositionSearchDTO dto) {
        return R.ok(healthcarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<HealthcarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(healthcarePositionService.detail(id));
    }

    @GetMapping("/exam-guide/list")
    public R<IPage<ExamGuideDetailVO>> examGuideList(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("healthcare");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice/list")
    public R<IPage<NoticeDetailVO>> noticeList(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("healthcare");
        return R.ok(noticeService.pageDetail(dto));
    }
}
