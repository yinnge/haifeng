package com.haifeng.admin.controller.algorithm.constraint;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintAddDTO;
import com.haifeng.admin.dto.algorithm.constraint.MajorConstraintQueryDTO;
import com.haifeng.admin.service.algorithm.constraint.MajorConstraintService;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintDetailVO;
import com.haifeng.admin.vo.algorithm.constraint.MajorConstraintListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/algorithm/constraint/major")
@RequiredArgsConstructor
public class MajorConstraintController {

    private final MajorConstraintService majorConstraintService;

    @GetMapping("/page")
    public R<IPage<MajorConstraintListVO>> page(@Valid MajorConstraintQueryDTO dto) {
        return R.ok(majorConstraintService.page(dto));
    }

    @GetMapping("/{id}")
    public R<MajorConstraintDetailVO> detail(@PathVariable Long id) {
        return R.ok(majorConstraintService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "专业约束管理", action = "新增专业约束关联")
    public R<Long> add(@Valid @RequestBody MajorConstraintAddDTO dto) {
        return R.ok(majorConstraintService.add(dto));
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "专业约束管理", action = "删除专业约束关联")
    public R<Void> delete(@PathVariable Long id) {
        majorConstraintService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "专业约束管理", action = "批量删除专业约束关联")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        majorConstraintService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "专业约束管理", action = "导入专业约束关联")
    public R<Void> importData(@RequestParam("file") MultipartFile file) {
        majorConstraintService.importData(file);
        return R.ok();
    }
}
