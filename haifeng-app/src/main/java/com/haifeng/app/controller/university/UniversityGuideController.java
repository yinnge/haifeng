package com.haifeng.app.controller.university;

import com.haifeng.app.service.university.UniversityGuideService;
import com.haifeng.app.vo.university.*;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端院校适应指南 - 6 个分类子路径
 * academic 需要 Pro，其余均需登录
 */
@RestController
@RequestMapping("/api/v1/app/university/guides")
@RequiredArgsConstructor
public class UniversityGuideController {

    private final UniversityGuideService guideService;

    @RequireLogin
    @GetMapping("/{universityId}/overview")
    public R<UniversityGuideOverviewVO> overview(@PathVariable Long universityId) {
        return R.ok(guideService.overview(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/survival")
    public R<UniversityGuideSurvivalVO> survival(@PathVariable Long universityId) {
        return R.ok(guideService.survival(universityId));
    }

    @RequirePro
    @GetMapping("/{universityId}/academic")
    public R<UniversityGuideAcademicVO> academic(@PathVariable Long universityId) {
        return R.ok(guideService.academic(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/social")
    public R<UniversityGuideSocialVO> social(@PathVariable Long universityId) {
        return R.ok(guideService.social(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/safety")
    public R<UniversityGuideSafetyVO> safety(@PathVariable Long universityId) {
        return R.ok(guideService.safety(universityId));
    }

    @RequireLogin
    @GetMapping("/{universityId}/life")
    public R<UniversityGuideLifeVO> life(@PathVariable Long universityId) {
        return R.ok(guideService.life(universityId));
    }
}
