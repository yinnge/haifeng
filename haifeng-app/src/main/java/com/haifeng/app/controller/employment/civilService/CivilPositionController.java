package com.haifeng.app.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.civilService.CivilPositionSearchDTO;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.service.employment.civilService.CivilPositionService;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.app.vo.employment.civilService.CivilPositionListVO;
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
@RequestMapping("/api/v1/app/employment/civil-service/position")
@RequiredArgsConstructor
public class CivilPositionController {

    private final CivilPositionService civilPositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<CivilPositionListVO>> list(@Valid CivilPositionSearchDTO dto) {
        return R.ok(civilPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CivilPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(civilPositionService.detail(id));
    }

    @GetMapping("/exam-guide")
    public R<IPage<ExamGuideDetailVO>> examGuide(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("civil");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice")
    public R<IPage<NoticeDetailVO>> notice(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("civil");
        return R.ok(noticeService.pageDetail(dto));
    }
}
