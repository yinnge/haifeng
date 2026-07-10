package com.haifeng.app.controller.competition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.service.competition.CompetitionService;
import com.haifeng.app.vo.competition.CompetitionDetailVO;
import com.haifeng.app.vo.competition.CompetitionListVO;
import com.haifeng.app.vo.competition.CompetitionMajorBriefVO;
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
 * C 端竞赛管理（spec 任务2）
 * 接口1 公开，接口2 需登录，接口3 需 Pro
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/competition")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    /** 任务2接口1：竞赛列表（公开） */
    @GetMapping("/list")
    public R<IPage<CompetitionListVO>> list(@Valid BasePageQueryDTO dto) {
        return R.ok(competitionService.page(dto));
    }

    /** 任务2接口2：竞赛详情（登录） */
    @RequireLogin
    @GetMapping("/{compId}/detail")
    public R<CompetitionDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long compId) {
        return R.ok(competitionService.detail(compId));
    }

    /** 任务2接口3：分页查询某竞赛关联的专业（Pro） */
    @RequirePro
    @GetMapping("/{compId}/majors")
    public R<IPage<CompetitionMajorBriefVO>> majors(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long compId,
            @Valid BasePageQueryDTO dto) {
        return R.ok(competitionService.majors(compId, dto));
    }
}
