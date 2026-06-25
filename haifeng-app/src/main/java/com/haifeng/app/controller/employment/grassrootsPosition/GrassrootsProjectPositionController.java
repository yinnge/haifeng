package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.dto.employment.grassrootsPosition.GrassrootsProjectPositionSearchDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/project")
@RequiredArgsConstructor
public class GrassrootsProjectPositionController {

    private final GrassrootsProjectPositionService grassrootsProjectPositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<GrassrootsProjectPositionListVO>> list(@Valid GrassrootsProjectPositionSearchDTO dto) {
        return R.ok(grassrootsProjectPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<GrassrootsProjectPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(grassrootsProjectPositionService.detail(id));
    }

    @GetMapping("/exam-guide/list")
    public R<IPage<ExamGuideDetailVO>> examGuideList(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("grassroots");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice/list")
    public R<IPage<NoticeDetailVO>> noticeList(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("grassroots");
        return R.ok(noticeService.pageDetail(dto));
    }
}
