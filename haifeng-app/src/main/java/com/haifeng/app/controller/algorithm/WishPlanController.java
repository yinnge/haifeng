package com.haifeng.app.controller.algorithm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishGroupExportAllDTO;
import com.haifeng.app.dto.algorithm.wish.WishGroupSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorExportDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/wish-plan")
@RequiredArgsConstructor
@RequireLogin
public class WishPlanController {

    private final WishPlanService wishPlanService;

    @GetMapping("/default-limits")
    public R<WishPlanLimitVO> getDefaultLimits() {
        return R.ok(wishPlanService.getDefaultLimits());
    }

    @PostMapping("/add-majors")
    public R<WishPlanListVO> addMajors(@Valid @RequestBody WishPlanAddMajorsDTO dto) {
        return R.ok(wishPlanService.addMajors(dto));
    }

    @GetMapping("/my-plans")
    public R<List<WishPlanListVO>> myPlans() {
        return R.ok(wishPlanService.myPlans());
    }

    @DeleteMapping("/{planId}")
    public R<Void> deletePlan(@PathVariable Integer planId) {
        wishPlanService.deletePlan(planId);
        return R.ok();
    }

    @GetMapping("/{planId}/groups")
    public R<IPage<WishPlanGroupVO>> pageGroups(
            @PathVariable Integer planId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(wishPlanService.pageGroups(planId, page, size));
    }

    @GetMapping("/{planId}/groups/{groupSnapshotId}/majors")
    public R<IPage<WishPlanMajorVO>> pageMajors(
            @PathVariable Integer planId,
            @PathVariable Integer groupSnapshotId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok(wishPlanService.pageMajors(planId, groupSnapshotId, page, size));
    }

    @PutMapping("/{planId}/groups/sort")
    @OperationLog(action = "修改专业组排序")
    public R<Void> updateGroupSortOrder(
            @PathVariable Integer planId,
            @Valid @RequestBody WishGroupSortDTO dto) {
        wishPlanService.updateGroupSortOrder(planId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/groups/{groupSnapshotId}/majors/sort")
    @OperationLog(action = "修改专业排序")
    public R<Void> updateMajorSortOrder(
            @PathVariable Integer planId,
            @PathVariable Integer groupSnapshotId,
            @Valid @RequestBody WishMajorSortDTO dto) {
        wishPlanService.updateMajorSortOrder(planId, groupSnapshotId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/majors/{majorId}/export")
    @OperationLog(action = "修改专业导出状态")
    public R<Void> updateMajorExportStatus(
            @PathVariable Integer planId,
            @PathVariable Integer majorId,
            @Valid @RequestBody WishMajorExportDTO dto) {
        wishPlanService.updateMajorExportStatus(planId, majorId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/groups/{groupSnapshotId}/export-all")
    @OperationLog(action = "批量修改专业组下专业导出状态")
    public R<Void> batchUpdateMajorExportStatus(
            @PathVariable Integer planId,
            @PathVariable Integer groupSnapshotId,
            @Valid @RequestBody WishGroupExportAllDTO dto) {
        wishPlanService.batchUpdateMajorExportStatus(planId, groupSnapshotId, dto);
        return R.ok();
    }
}
