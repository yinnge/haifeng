package com.haifeng.admin.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.major.PostgradMajorUniversityQueryDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.service.major.PostgradMajorUniversityService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorUniversityListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考研专业-大学关联管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/postgrad-major-university")
@RequiredArgsConstructor
public class PostgradMajorUniversityController {

    private final PostgradMajorUniversityService postgradMajorUniversityService;

    /**
     * 分页查询考研专业-大学关联列表
     */
    @GetMapping("/list")
    public R<IPage<PostgradMajorUniversityListVO>> list(@Valid PostgradMajorUniversityQueryDTO dto) {
        return R.ok(postgradMajorUniversityService.list(dto));
    }

    /**
     * 软删除考研专业-大学关联（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "考研专业-大学关联管理", action = "软删除考研专业-大学关联")
    public R<Void> softDelete(@PathVariable Long id) {
        postgradMajorUniversityService.softDelete(id);
        return R.ok();
    }

    /**
     * 硬删除考研专业-大学关联（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "考研专业-大学关联管理", action = "硬删除考研专业-大学关联")
    public R<Void> hardDelete(@PathVariable Long id) {
        postgradMajorUniversityService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除考研专业-大学关联
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "考研专业-大学关联管理", action = "批量软删除考研专业-大学关联")
    public R<Void> batchSoftDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        postgradMajorUniversityService.batchSoftDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除考研专业-大学关联
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "考研专业-大学关联管理", action = "批量硬删除考研专业-大学关联")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        postgradMajorUniversityService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入考研专业-大学关联数据
     */
    @PostMapping("/import")
    @OperationLog(module = "考研专业-大学关联管理", action = "导入考研专业-大学关联数据")
    public R<ImportResultVO> importPostgradMajorUniversity(@RequestParam("file") MultipartFile file) {
        return R.ok(postgradMajorUniversityService.importPostgradMajorUniversity(file));
    }

    /**
     * 恢复已删除的考研专业-大学关联
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "考研专业-大学关联管理", action = "恢复考研专业-大学关联")
    public R<Void> restore(@PathVariable Long id) {
        postgradMajorUniversityService.restore(id);
        return R.ok();
    }
}
