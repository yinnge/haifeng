package com.haifeng.admin.controller.major;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.major.*;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.service.major.MajorService;
import com.haifeng.admin.vo.major.ImportResultVO;
import com.haifeng.admin.vo.major.MajorDetailVO;
import com.haifeng.admin.vo.major.MajorListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 专业管理Controller
 */
@RestController
@RequestMapping("/api/v1/admin/major")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /**
     * 分页查询专业列表
     */
    @GetMapping("/list")
    public R<IPage<MajorListVO>> list(@Valid MajorQueryDTO dto) {
        return R.ok(majorService.list(dto));
    }

    /**
     * 获取专业详情（包含Major + MajorDetail）
     */
    @GetMapping("/{id}")
    public R<MajorDetailVO> detail(@PathVariable Long id) {
        return R.ok(majorService.getById(id));
    }

    /**
     * 新增专业
     */
    @PostMapping
    @OperationLog(module = "专业管理", action = "新增专业")
    public R<Long> add(@Valid @RequestBody MajorAddDTO dto) {
        return R.ok(majorService.add(dto));
    }

    /**
     * 修改专业基础信息
     */
    @PutMapping("/{id}")
    @OperationLog(module = "专业管理", action = "修改专业基础信息")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MajorUpdateDTO dto) {
        majorService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改专业详情信息
     */
    @PutMapping("/{id}/detail")
    @OperationLog(module = "专业管理", action = "修改专业详情信息")
    public R<Void> updateDetail(@PathVariable Long id, @Valid @RequestBody MajorDetailUpdateDTO dto) {
        majorService.updateDetail(id, dto);
        return R.ok();
    }

    /**
     * 修改专业状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "专业管理", action = "修改专业状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        majorService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    /**
     * 软删除专业（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "专业管理", action = "软删除专业")
    public R<Void> softDelete(@PathVariable Long id) {
        majorService.softDelete(id);
        return R.ok();
    }

    /**
     * 硬删除专业（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "专业管理", action = "硬删除专业")
    public R<Void> hardDelete(@PathVariable Long id) {
        majorService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除专业
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "专业管理", action = "批量软删除专业")
    public R<Void> batchSoftDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        majorService.batchSoftDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除专业
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "专业管理", action = "批量硬删除专业")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        majorService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入专业主表数据
     */
    @PostMapping("/import")
    @OperationLog(module = "专业管理", action = "导入专业主表数据")
    public R<ImportResultVO> importMajor(@RequestParam("file") MultipartFile file) {
        return R.ok(majorService.importMajor(file));
    }

    /**
     * 导入专业详情数据
     */
    @PostMapping("/import-detail")
    @OperationLog(module = "专业管理", action = "导入专业详情数据")
    public R<ImportResultVO> importMajorDetail(@RequestParam("file") MultipartFile file) {
        return R.ok(majorService.importMajorDetail(file));
    }

    /**
     * 恢复已禁用的专业
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "专业管理", action = "恢复专业")
    public R<Void> restore(@PathVariable Long id) {
        majorService.restore(id);
        return R.ok();
    }
}
