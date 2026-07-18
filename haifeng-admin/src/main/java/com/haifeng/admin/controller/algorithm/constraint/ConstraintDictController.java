package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictBatchDeleteDTO;
import com.haifeng.admin.dto.algorithm.constraint.ConstraintDictQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.ConstraintDictService;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.ConstraintDictListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 约束字典管理
 * 定义高考录取中的各类约束项（视力/身高/色觉/单科成绩等），
 * 是约束体系的基础数据，被 MajorConstraintController 引用关联到专业
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/dict")
@RequiredArgsConstructor
@RequireAdminModule("algo_constraint_dict")
public class ConstraintDictController {

    private final ConstraintDictService constraintDictService;

    /** 分页查询 约束字典列表 */
    @GetMapping("/page")
    public R<IPage<ConstraintDictListVO>> page(@Valid ConstraintDictQueryDTO dto) {
        return R.ok(constraintDictService.page(dto));
    }

    /** 查看单个约束字典（按 code） */
    @GetMapping("/{code}")
    public R<ConstraintDictDetailVO> detail(@PathVariable String code) {
        return R.ok(constraintDictService.detail(code));
    }

    /** 新增约束字典 */
    @PostMapping
    @OperationLog(module = "约束字典管理", action = "新增约束字典")
    public R<Void> add(@Valid @RequestBody ConstraintDictAddDTO dto) {
        constraintDictService.add(dto);
        return R.ok();
    }

    /** 修改约束字典 */
    @PutMapping("/{code}")
    @OperationLog(module = "约束字典管理", action = "修改约束字典")
    public R<Void> update(@PathVariable String code, @Valid @RequestBody ConstraintDictAddDTO dto) {
        constraintDictService.update(code, dto);
        return R.ok();
    }

    /** 启用/禁用 约束字典 */
    @PutMapping("/{code}/toggle")
    @OperationLog(module = "约束字典管理", action = "切换约束字典状态")
    public R<Void> toggleActive(@PathVariable String code) {
        constraintDictService.toggleActive(code);
        return R.ok();
    }

    /** 删除约束字典（按 code） */
    @DeleteMapping("/{code}")
    @OperationLog(module = "约束字典管理", action = "删除约束字典")
    public R<Void> delete(@PathVariable String code) {
        constraintDictService.delete(code);
        return R.ok();
    }

    /** 批量删除约束字典（按 code 列表） */
    @PostMapping("/batch-delete")
    @OperationLog(module = "约束字典管理", action = "批量删除约束字典")
    public R<Void> batchDelete(@Valid @RequestBody ConstraintDictBatchDeleteDTO dto) {
        constraintDictService.batchDelete(dto.getCodes());
        return R.ok();
    }
}
