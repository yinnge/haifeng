package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.dto.employment.grassrootsPosition.PublicWelfarePositionSearchDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/welfare")
@RequiredArgsConstructor
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionSearchDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }

    @GetMapping("/exam-guide/list")
    public R<IPage<ExamGuideDetailVO>> examGuideList(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("public_welfare");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice/list")
    public R<IPage<NoticeDetailVO>> noticeList(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("public_welfare");
        return R.ok(noticeService.pageDetail(dto));
    }
}
