package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.PostgradMajorListQueryDTO;
import com.haifeng.app.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.app.service.major.PostgradMajorService;
import com.haifeng.app.vo.major.PostgradMajorDetailVO;
import com.haifeng.app.vo.major.PostgradMajorListVO;
import com.haifeng.app.vo.major.UniversityBriefForPostgradVO;
import com.haifeng.app.vo.major.UndergraduateMajorDirectionBriefVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.dto.common.BasePageQueryDTO;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * C 端考研专业管理（spec 任务2 + 任务4）
 * 任务2接口1/2 需登录，任务4接口1 需 Pro
 * 新增：任务1接口2（关联查询）需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/postgrad-major")
@RequiredArgsConstructor
public class PostgradMajorController {

    private final PostgradMajorService postgradMajorService;

    /** 任务2接口1：考研专业列表（登录） */
    @RequireLogin
    @GetMapping("/list")
    public R<IPage<PostgradMajorListVO>> list(@Valid PostgradMajorListQueryDTO dto) {
        return R.ok(postgradMajorService.page(dto));
    }

    /** 任务2接口2：考研专业详情（登录） */
    @RequireLogin
    @GetMapping("/{majorId}/detail")
    public R<PostgradMajorDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long majorId) {
        return R.ok(postgradMajorService.detail(majorId));
    }

    /** 任务4接口1：考研专业 → 大学列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/universities")
    public R<IPage<UniversityBriefForPostgradVO>> universities(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long majorId,
            @Valid PostgradMajorUniversityQueryDTO dto) {
        return R.ok(postgradMajorService.universities(majorId, dto));
    }

    /** 任务1接口2（关联查询）：考研方向 → 本科专业列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{postgradMajorId}/undergraduate-majors")
    public R<IPage<UndergraduateMajorDirectionBriefVO>> undergraduateMajors(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long postgradMajorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(postgradMajorService.undergraduateMajors(postgradMajorId, dto));
    }
}
