package com.haifeng.app.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.employment.contentManagement.examGuide.ExamGuideQueryDTO;
import com.haifeng.app.dto.employment.contentManagement.notice.NoticeQueryDTO;
import com.haifeng.app.dto.employment.grassrootsPosition.CommunityPositionSearchDTO;
import com.haifeng.app.service.employment.contentManagement.examGuide.ExamGuideService;
import com.haifeng.app.service.employment.contentManagement.notice.NoticeService;
import com.haifeng.app.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.app.vo.employment.contentManagement.examGuide.ExamGuideDetailVO;
import com.haifeng.app.vo.employment.contentManagement.notice.NoticeDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.app.vo.employment.grassrootsPosition.CommunityPositionListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/app/employment/grassroots/community")
@RequiredArgsConstructor
public class CommunityPositionController {

    private final CommunityPositionService communityPositionService;
    private final ExamGuideService examGuideService;
    private final NoticeService noticeService;

    @GetMapping("/list")
    public R<IPage<CommunityPositionListVO>> list(@Valid CommunityPositionSearchDTO dto) {
        return R.ok(communityPositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<CommunityPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(communityPositionService.detail(id));
    }

    @GetMapping("/exam-guide/list")
    public R<IPage<ExamGuideDetailVO>> examGuideList(@Valid ExamGuideQueryDTO dto) {
        dto.setGuideCategory("community");
        return R.ok(examGuideService.pageDetail(dto));
    }

    @GetMapping("/notice/list")
    public R<IPage<NoticeDetailVO>> noticeList(@Valid NoticeQueryDTO dto) {
        dto.setNoticeCategory("community");
        return R.ok(noticeService.pageDetail(dto));
    }
}
