package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.MajorConstraintService;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 专业-约束关联管理
 * 将约束字典中的约束项（如视力≥4.8）关联到具体专业，构成专业录取约束
 */
@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/major")
@RequiredArgsConstructor
@RequireAdminModule("algo_constraint_mjr")
public class MajorConstraintController {

    private final MajorConstraintService majorConstraintService;

    /** 分页查询 专业与约束的关联列表 */
    @GetMapping("/page")
    public R<IPage<MajorConstraintListVO>> page(@Valid MajorConstraintQueryDTO dto) {
        return R.ok(majorConstraintService.page(dto));
    }

    /** 查看单条关联详情 */
    @GetMapping("/{id}")
    public R<MajorConstraintDetailVO> detail(@PathVariable Long id) {
        return R.ok(majorConstraintService.detail(id));
    }

    /** 新增 专业与约束的关联 */
    @PostMapping
    @OperationLog(module = "专业约束管理", action = "新增专业约束关联")
    public R<Long> add(@Valid @RequestBody MajorConstraintAddDTO dto) {
        return R.ok(majorConstraintService.add(dto));
    }

    /** 删除单条关联 */
    @DeleteMapping("/{id}")
    @OperationLog(module = "专业约束管理", action = "删除专业约束关联")
    public R<Void> delete(@PathVariable Long id) {
        majorConstraintService.delete(id);
        return R.ok();
    }

    /** 批量删除关联 */
    @DeleteMapping("/batch")
    @OperationLog(module = "专业约束管理", action = "批量删除专业约束关联")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        majorConstraintService.batchDelete(ids);
        return R.ok();
    }

    /** 通过 Excel 批量导入 专业与约束的关联 */
    @PostMapping("/import")
    @OperationLog(module = "专业约束管理", action = "导入专业约束关联")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        majorConstraintService.importData(file);
        return R.ok();
    }
}
