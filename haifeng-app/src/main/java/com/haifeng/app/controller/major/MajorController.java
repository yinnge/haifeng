package com.haifeng.app.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.major.MajorListQueryDTO;
import com.haifeng.app.dto.major.MajorRankingQueryDTO;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.vo.major.CompetitionBriefVO;
import com.haifeng.app.vo.major.MajorCategoryStatVO;
import com.haifeng.app.vo.major.MajorDetailVO;
import com.haifeng.app.vo.major.MajorListVO;
import com.haifeng.app.vo.major.PostgradMajorDirectionBriefVO;
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

import java.util.List;

/**
 * C 端专业管理（spec 任务1）
 * 接口1/3 公开，接口2 需登录，接口4 需 Pro
 * 新增：任务1接口1（关联查询）需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /** 任务1接口1：专业列表（公开） */
    @GetMapping("/list")
    public R<IPage<MajorListVO>> list(@Valid MajorListQueryDTO dto) {
        return R.ok(majorService.page(dto));
    }

    /** 任务1接口2：专业详情（登录） */
    @RequireLogin
    @GetMapping("/{majorId}/detail")
    public R<MajorDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long majorId) {
        return R.ok(majorService.detail(majorId));
    }

    /** 任务1接口3：按 major_category 分组统计（公开） */
    @GetMapping("/category-stats")
    public R<List<MajorCategoryStatVO>> categoryStats() {
        return R.ok(majorService.categoryStats());
    }

    /** 任务1接口4：薪资/就业排行（Pro 及以上） */
    @RequirePro
    @GetMapping("/ranking")
    public R<IPage<MajorListVO>> ranking(@Valid MajorRankingQueryDTO dto) {
        return R.ok(majorService.ranking(dto));
    }

    /** 任务1接口1（关联查询）：本科专业 → 考研方向列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/postgrad-directions")
    public R<IPage<PostgradMajorDirectionBriefVO>> postgradDirections(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long majorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(majorService.postgradDirections(majorId, dto));
    }

    /** 任务3接口1：专业 → 关联竞赛列表（Pro 及以上） */
    @RequirePro
    @GetMapping("/{majorId}/competitions")
    public R<IPage<CompetitionBriefVO>> competitions(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long majorId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(majorService.competitions(majorId, dto));
    }
}
