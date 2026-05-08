package com.haifeng.admin.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.major.PostgradMajorAddDTO;
import com.haifeng.admin.dto.major.PostgradMajorQueryDTO;
import com.haifeng.admin.dto.major.PostgradMajorUpdateDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.service.major.PostgradMajorService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.PostgradMajorDetailVO;
import com.haifeng.admin.vo.major.PostgradMajorListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 考研专业管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/postgrad-major")
@RequiredArgsConstructor
public class PostgradMajorController {

    private final PostgradMajorService postgradMajorService;

    /**
     * 分页查询考研专业列表
     */
    @GetMapping("/list")
    public R<IPage<PostgradMajorListVO>> list(@Valid PostgradMajorQueryDTO dto) {
        return R.ok(postgradMajorService.list(dto));
    }

    /**
     * 获取考研专业详情
     */
    @GetMapping("/{id}")
    public R<PostgradMajorDetailVO> detail(@PathVariable Long id) {
        return R.ok(postgradMajorService.getById(id));
    }

    /**
     * 新增考研专业
     */
    @PostMapping
    @OperationLog(module = "考研专业管理", action = "新增考研专业")
    public R<Long> add(@Valid @RequestBody PostgradMajorAddDTO dto) {
        return R.ok(postgradMajorService.add(dto));
    }

    /**
     * 修改考研专业
     */
    @PutMapping("/{id}")
    @OperationLog(module = "考研专业管理", action = "修改考研专业")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PostgradMajorUpdateDTO dto) {
        postgradMajorService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改考研专业状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "考研专业管理", action = "修改考研专业状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        postgradMajorService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    /**
     * 软删除考研专业（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "考研专业管理", action = "软删除考研专业")
    public R<Void> softDelete(@PathVariable Long id) {
        postgradMajorService.softDelete(id);
        return R.ok();
    }

    /**
     * 硬删除考研专业（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "考研专业管理", action = "硬删除考研专业")
    public R<Void> hardDelete(@PathVariable Long id) {
        postgradMajorService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除考研专业
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "考研专业管理", action = "批量软删除考研专业")
    public R<Void> batchSoftDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        postgradMajorService.batchSoftDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除考研专业
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "考研专业管理", action = "批量硬删除考研专业")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        postgradMajorService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入考研专业数据
     */
    @PostMapping("/import")
    @OperationLog(module = "考研专业管理", action = "导入考研专业数据")
    public R<ImportResultVO> importPostgradMajor(@RequestParam("file") MultipartFile file) {
        return R.ok(postgradMajorService.importPostgradMajor(file));
    }

    /**
     * 恢复已删除的考研专业
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "考研专业管理", action = "恢复考研专业")
    public R<Void> restore(@PathVariable Long id) {
        postgradMajorService.restore(id);
        return R.ok();
    }
}
