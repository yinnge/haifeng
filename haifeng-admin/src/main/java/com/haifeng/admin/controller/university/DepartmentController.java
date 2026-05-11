package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.DepartmentAddDTO;
import com.haifeng.admin.dto.university.DepartmentQueryDTO;
import com.haifeng.admin.dto.university.DepartmentUpdateDTO;
import com.haifeng.admin.service.university.DepartmentService;
import com.haifeng.admin.vo.university.DepartmentDetailVO;
import com.haifeng.admin.vo.university.DepartmentListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 院系管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 分页查询院系列表
     */
    @GetMapping("/list")
    public R<IPage<DepartmentListVO>> list(@Valid DepartmentQueryDTO dto) {
        return R.ok(departmentService.page(dto));
    }

    /**
     * 获取院系详情
     */
    @GetMapping("/{id}")
    public R<DepartmentDetailVO> detail(@PathVariable Long id) {
        return R.ok(departmentService.detail(id));
    }

    /**
     * 新增院系
     */
    @PostMapping
    @OperationLog(module = "院系管理", action = "新增院系")
    public R<Long> add(@Valid @RequestBody DepartmentAddDTO dto) {
        return R.ok(departmentService.add(dto));
    }

    /**
     * 修改院系
     */
    @PutMapping("/{id}")
    @OperationLog(module = "院系管理", action = "修改院系")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DepartmentUpdateDTO dto) {
        departmentService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改院系状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "院系管理", action = "修改院系状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        departmentService.updateStatus(id, dto.getStatus().intValue());
        return R.ok();
    }

    /**
     * 软删除院系（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "院系管理", action = "软删除院系")
    public R<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除院系（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "院系管理", action = "硬删除院系")
    public R<Void> hardDelete(@PathVariable Long id) {
        departmentService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除院系
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "院系管理", action = "批量软删除院系")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        departmentService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除院系
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "院系管理", action = "批量硬删除院系")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        departmentService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入院系数据
     */
    @PostMapping("/import")
    @OperationLog(module = "院系管理", action = "导入院系数据")
    public R<Void> importDepartments(@RequestParam("file") MultipartFile file) {
        departmentService.importDepartments(file);
        return R.ok();
    }

    /**
     * 导入院系报告数据
     */
    @PostMapping("/import-report")
    @OperationLog(module = "院系管理", action = "导入院系报告数据")
    public R<Void> importDepartmentReports(@RequestParam("file") MultipartFile file) {
        departmentService.importDepartmentReports(file);
        return R.ok();
    }
}
