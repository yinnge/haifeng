package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.LaboratoryAddDTO;
import com.haifeng.admin.dto.university.LaboratoryQueryDTO;
import com.haifeng.admin.dto.university.LaboratoryUpdateDTO;
import com.haifeng.admin.service.university.LaboratoryService;
import com.haifeng.admin.vo.university.LaboratoryDetailVO;
import com.haifeng.admin.vo.university.LaboratoryListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 实验室Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/laboratory")
@RequiredArgsConstructor
@RequireAdminModule("university_lab")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    /**
     * 分页查询实验室列表
     */
    @GetMapping("/list")
    public R<IPage<LaboratoryListVO>> list(@Valid LaboratoryQueryDTO dto) {
        return R.ok(laboratoryService.page(dto));
    }

    /**
     * 获取实验室详情
     */
    @GetMapping("/{id}")
    public R<LaboratoryDetailVO> detail(@PathVariable Long id) {
        return R.ok(laboratoryService.detail(id));
    }

    /**
     * 新增实验室
     */
    @PostMapping
    @OperationLog(module = "实验室管理", action = "新增实验室")
    public R<Long> add(@Valid @RequestBody LaboratoryAddDTO dto) {
        return R.ok(laboratoryService.add(dto));
    }

    /**
     * 修改实验室
     */
    @PutMapping("/{id}")
    @OperationLog(module = "实验室管理", action = "修改实验室")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody LaboratoryUpdateDTO dto) {
        laboratoryService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改实验室状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "实验室管理", action = "修改实验室状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        laboratoryService.updateStatus(id, dto.getStatus().intValue());
        return R.ok();
    }

    /**
     * 软删除实验室（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "实验室管理", action = "软删除实验室")
    public R<Void> delete(@PathVariable Long id) {
        laboratoryService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除实验室（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "实验室管理", action = "硬删除实验室")
    public R<Void> hardDelete(@PathVariable Long id) {
        laboratoryService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除实验室
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "实验室管理", action = "批量软删除实验室")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        laboratoryService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除实验室
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "实验室管理", action = "批量硬删除实验室")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        laboratoryService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入实验室数据
     */
    @PostMapping("/import")
    @OperationLog(module = "实验室管理", action = "导入实验室数据")
    public R<Void> importLaboratories(@RequestParam("file") MultipartFile file) {
        laboratoryService.importLaboratories(file);
        return R.ok();
    }
}
