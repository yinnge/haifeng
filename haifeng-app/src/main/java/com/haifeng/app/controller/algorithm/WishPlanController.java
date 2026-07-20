package com.haifeng.app.controller.algorithm;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.wish.WishGroupExportAllDTO;
import com.haifeng.app.dto.algorithm.wish.WishGroupSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorExportDTO;
import com.haifeng.app.dto.algorithm.wish.WishMajorSortDTO;
import com.haifeng.app.dto.algorithm.wish.WishPlanAddMajorsDTO;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportFileVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanExportProgressVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanGroupVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanLimitVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanListVO;
import com.haifeng.app.vo.algorithm.wish.WishPlanMajorVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequirePro;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    public R<Void> deletePlan(@PathVariable @Min(1) Integer planId) {
        wishPlanService.deletePlan(planId);
        return R.ok();
    }

    @GetMapping("/{planId}/groups")
    public R<IPage<WishPlanGroupVO>> pageGroups(
            @PathVariable @Min(1) Integer planId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {
        return R.ok(wishPlanService.pageGroups(planId, page, size));
    }

    @GetMapping("/{planId}/groups/{groupSnapshotId}/majors")
    public R<IPage<WishPlanMajorVO>> pageMajors(
            @PathVariable @Min(1) Integer planId,
            @PathVariable @Min(1) Integer groupSnapshotId,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {
        return R.ok(wishPlanService.pageMajors(planId, groupSnapshotId, page, size));
    }

    @PutMapping("/{planId}/groups/sort")
    public R<Void> updateGroupSortOrder(
            @PathVariable @Min(1) Integer planId,
            @Valid @RequestBody WishGroupSortDTO dto) {
        wishPlanService.updateGroupSortOrder(planId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/groups/{groupSnapshotId}/majors/sort")
    public R<Void> updateMajorSortOrder(
            @PathVariable @Min(1) Integer planId,
            @PathVariable @Min(1) Integer groupSnapshotId,
            @Valid @RequestBody WishMajorSortDTO dto) {
        wishPlanService.updateMajorSortOrder(planId, groupSnapshotId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/majors/{majorId}/export")
    public R<Void> updateMajorExportStatus(
            @PathVariable @Min(1) Integer planId,
            @PathVariable @Min(1) Integer majorId,
            @Valid @RequestBody WishMajorExportDTO dto) {
        wishPlanService.updateMajorExportStatus(planId, majorId, dto);
        return R.ok();
    }

    @PutMapping("/{planId}/groups/{groupSnapshotId}/export-all")
    public R<Void> batchUpdateMajorExportStatus(
            @PathVariable @Min(1) Integer planId,
            @PathVariable @Min(1) Integer groupSnapshotId,
            @Valid @RequestBody WishGroupExportAllDTO dto) {
        wishPlanService.batchUpdateMajorExportStatus(planId, groupSnapshotId, dto);
        return R.ok();
    }

    @GetMapping("/{planId}/export/progress")
    @RequirePro
    public R<WishPlanExportProgressVO> getExportProgress(@PathVariable @Min(1) Integer planId) {
        return R.ok(wishPlanService.getExportProgress(planId));
    }

    /**
     * 生成导出文件（POST，非幂等）
     */
    @PostMapping("/{planId}/export/generate")
    @RequirePro
    public R<WishPlanExportFileVO> generateExportFile(@PathVariable @Min(1) Integer planId) {
        return R.ok(wishPlanService.generateExportFile(planId));
    }

    /**
     * 下载已生成的导出文件（GET，只读）
     */
    @GetMapping("/{planId}/export/download")
    @RequirePro
    public ResponseEntity<byte[]> downloadExportFile(
            @PathVariable @Min(1) Integer planId,
            @RequestParam @NotBlank String file) {
        byte[] content = wishPlanService.readExportFile(planId, file);
        String encodedFileName = URLEncoder.encode(file, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping("/{planId}/export/save")
    public R<Void> saveExportStatusToDatabase(@PathVariable @Min(1) Integer planId) {
        wishPlanService.saveExportStatusToDatabase(planId);
        return R.ok();
    }
}
