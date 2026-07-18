package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseScoreAddDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreBatchDeleteDTO;
import com.haifeng.admin.dto.special.StrongBaseScoreQueryDTO;
import com.haifeng.admin.service.special.StrongBaseScoreService;
import com.haifeng.admin.vo.special.StrongBaseScoreDetailVO;
import com.haifeng.admin.vo.special.StrongBaseScoreListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 特殊通道 - 强基计划分数/数据管理
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/special/strong-base-score")
@RequiredArgsConstructor
@RequireAdminModule("special_sbs_score")
public class StrongBaseScoreController {

    private final StrongBaseScoreService strongBaseScoreService;

    @GetMapping("/page")
    public R<IPage<StrongBaseScoreListVO>> page(@Valid StrongBaseScoreQueryDTO dto) {
        return R.ok(strongBaseScoreService.page(dto));
    }

    @GetMapping("/{id}")
    public R<StrongBaseScoreDetailVO> detail(@PathVariable Long id) {
        return R.ok(strongBaseScoreService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "强基计划管理", action = "新增强基数据")
    public R<Void> add(@Valid @RequestBody StrongBaseScoreAddDTO dto) {
        strongBaseScoreService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "修改强基数据")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StrongBaseScoreAddDTO dto) {
        strongBaseScoreService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "强基计划管理", action = "切换强基数据状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        strongBaseScoreService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "删除强基数据")
    public R<Void> delete(@PathVariable Long id) {
        strongBaseScoreService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "强基计划管理", action = "批量删除强基数据")
    public R<Void> batchDelete(@Valid @RequestBody StrongBaseScoreBatchDeleteDTO dto) {
        strongBaseScoreService.batchDelete(dto.getIds());
        return R.ok();
    }
}
