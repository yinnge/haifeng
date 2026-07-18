package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.*;
import com.haifeng.admin.service.university.UniversityGuideService;
import com.haifeng.admin.vo.university.UniversityGuideDetailVO;
import com.haifeng.admin.vo.university.UniversityGuideListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 院校适应指南Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/guide")
@RequiredArgsConstructor
@RequireAdminModule("university_guide")
@Validated
public class UniversityGuideController {

    private final UniversityGuideService universityGuideService;

    /**
     * 分页查询院校适应指南列表
     */
    @GetMapping("/list")
    public R<IPage<UniversityGuideListVO>> list(@Valid UniversityGuideQueryDTO dto) {
        return R.ok(universityGuideService.page(dto));
    }

    /**
     * 获取院校适应指南详情
     */
    @GetMapping("/{id}")
    public R<UniversityGuideDetailVO> detail(@PathVariable Long id) {
        return R.ok(universityGuideService.detail(id));
    }

    /**
     * 新增院校适应指南
     */
    @PostMapping
    @OperationLog(module = "院校管理", action = "新增院校适应指南")
    public R<Long> add(@Valid @RequestBody UniversityGuideAddDTO dto) {
        return R.ok(universityGuideService.add(dto));
    }

    /**
     * 修改院校适应指南
     */
    @PutMapping("/{id}")
    @OperationLog(module = "院校管理", action = "修改院校适应指南")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody UniversityGuideUpdateDTO dto) {
        universityGuideService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改院校适应指南状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "院校管理", action = "修改院校适应指南状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        universityGuideService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    /**
     * 软删除院校适应指南（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "院校管理", action = "软删除院校适应指南")
    public R<Void> delete(@PathVariable Long id) {
        universityGuideService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除院校适应指南（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "院校管理", action = "硬删除院校适应指南")
    public R<Void> hardDelete(@PathVariable Long id) {
        universityGuideService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除院校适应指南
     */
    @PostMapping("/batch-delete")
    @OperationLog(module = "院校管理", action = "批量软删除院校适应指南")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        universityGuideService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除院校适应指南
     */
    @PostMapping("/batch-hard-delete")
    @OperationLog(module = "院校管理", action = "批量硬删除院校适应指南")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        universityGuideService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入院校适应指南数据
     */
    @PostMapping("/import")
    @OperationLog(module = "院校管理", action = "导入院校适应指南数据")
    public R<Void> importGuide(@RequestParam("file") MultipartFile file) {
        universityGuideService.importGuide(file);
        return R.ok();
    }
}
