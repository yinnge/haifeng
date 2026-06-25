package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.InstitutionPositionSearchDTO;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.civilService.InstitutionPositionService;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/institution")
@RequiredArgsConstructor
public class InstitutionPositionController {

    private final InstitutionPositionService institutionPositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<InstitutionPositionListVO>> list(@Valid InstitutionPositionSearchDTO dto) {
        return R.ok(institutionPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<InstitutionPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionPositionService.detail(id));
    }

    @GetMapping("/exam-guide")
    public R<IPage<ExamGuideDetailVO>> examGuide(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("institution");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice")
    public R<IPage<NoticeDetailVO>> notice(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("institution");
        return R.ok(noticeService.pageDetail(dto));
    }
}
