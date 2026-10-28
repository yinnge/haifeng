package com.haifeng.app.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.university.UniversityChannelQueryDTO;
import com.haifeng.app.dto.university.UniversityQueryDTO;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.university.ChannelOptionVO;
import com.haifeng.app.vo.university.UniversityChannelListVO;
import com.haifeng.app.vo.university.UniversityDetailVO;
import com.haifeng.app.vo.university.UniversityListVO;
import com.haifeng.app.vo.university.UniversityStatsVO;
import com.haifeng.common.annotation.RequireLogin;
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
 * C 端院校管理 - 列表（公开）+ 详情（登录）
 */
@Validated
@RestController
@RequestMapping("/api/v1/app/university")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityService universityService;

    /** 院校列表（按界面原型重设计）：分页查询，免登录；排序仅 sort_order DESC */
    @GetMapping("/list")
    public R<IPage<UniversityListVO>> list(@Valid UniversityQueryDTO dto) {
        return R.ok(universityService.page(dto));
    }

    /** 院校统计概览（985/211/双一流/本科数量），免登录，不受列表筛选影响 */
    @GetMapping("/stats")
    public R<UniversityStatsVO> stats() {
        return R.ok(universityService.stats());
    }

    /** 招生类型筛选下拉选项（t_special_channel，is_active=true，按 sort_order 升序），免登录 */
    @GetMapping("/special-channel-options")
    public R<List<ChannelOptionVO>> specialChannelOptions() {
        return R.ok(universityService.listSpecialChannelOptions());
    }

    /** 任务 2：院校详情，需登录 */
    @RequireLogin
    @GetMapping("/{universityId}/detail")
    public R<UniversityDetailVO> detail(@PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId) {
        return R.ok(universityService.detail(universityId));
    }

    @GetMapping("/{universityId}/channels")
    public R<IPage<UniversityChannelListVO>> channels(
            @PathVariable @Min(value = 1, message = "ID必须大于0") Long universityId,
            @Valid UniversityChannelQueryDTO dto) {
        return R.ok(universityService.pageChannels(universityId, dto));
    }

    @GetMapping("/channel-options")
    public R<List<ChannelOptionVO>> channelOptions() {
        return R.ok(universityService.listChannelOptions());
    }
}
