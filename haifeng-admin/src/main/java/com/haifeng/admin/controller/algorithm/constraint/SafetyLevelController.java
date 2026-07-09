package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelBatchDeleteDTO;
import com.haifeng.admin.dto.algorithm.constraint.SafetyLevelQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.SafetyLevelService;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.SafetyLevelListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/safety-level")
@RequiredArgsConstructor
@RequireAdminModule("algo_safety_level")
public class SafetyLevelController {

    private final SafetyLevelService safetyLevelService;

    @GetMapping("/page")
    public R<IPage<SafetyLevelListVO>> page(@Valid SafetyLevelQueryDTO dto) {
        return R.ok(safetyLevelService.page(dto));
    }

    @GetMapping("/{level}")
    public R<SafetyLevelDetailVO> detail(@PathVariable Short level) {
        return R.ok(safetyLevelService.detail(level));
    }

    @PostMapping
    @OperationLog(module = "安全系数管理", action = "新增安全系数等级")
    public R<Void> add(@Valid @RequestBody SafetyLevelAddDTO dto) {
        safetyLevelService.add(dto);
        return R.ok();
    }

    @PutMapping("/{level}")
    @OperationLog(module = "安全系数管理", action = "修改安全系数等级")
    public R<Void> update(@PathVariable Short level, @Valid @RequestBody SafetyLevelAddDTO dto) {
        safetyLevelService.update(level, dto);
        return R.ok();
    }

    @DeleteMapping("/{level}")
    @OperationLog(module = "安全系数管理", action = "删除安全系数等级")
    public R<Void> delete(@PathVariable Short level) {
        safetyLevelService.delete(level);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "安全系数管理", action = "批量删除安全系数等级")
    public R<Void> batchDelete(@Valid @RequestBody SafetyLevelBatchDeleteDTO dto) {
        safetyLevelService.batchDelete(dto.getLevels());
        return R.ok();
    }
}
