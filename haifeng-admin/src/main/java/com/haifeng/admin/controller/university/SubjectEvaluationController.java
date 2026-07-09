package com.haifeng.admin.controller.university;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.home.StatusDTO;
import com.haifeng.admin.dto.university.BatchDeleteDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationAddDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationQueryDTO;
import com.haifeng.admin.dto.university.SubjectEvaluationUpdateDTO;
import com.haifeng.admin.service.university.SubjectEvaluationService;
import com.haifeng.admin.vo.university.SubjectEvaluationDetailVO;
import com.haifeng.admin.vo.university.SubjectEvaluationListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学科评估Controller
 */
@RestController
@RequestMapping("/api/v1/admin/university/subject-evaluation")
@RequiredArgsConstructor
@RequireAdminModule("university_eval")
public class SubjectEvaluationController {

    private final SubjectEvaluationService subjectEvaluationService;

    /**
     * 分页查询学科评估列表
     */
    @GetMapping("/list")
    public R<IPage<SubjectEvaluationListVO>> list(@Valid SubjectEvaluationQueryDTO dto) {
        return R.ok(subjectEvaluationService.page(dto));
    }

    /**
     * 获取学科评估详情
     */
    @GetMapping("/{id}")
    public R<SubjectEvaluationDetailVO> detail(@PathVariable Long id) {
        return R.ok(subjectEvaluationService.detail(id));
    }

    /**
     * 新增学科评估
     */
    @PostMapping
    @OperationLog(module = "学科评估管理", action = "新增学科评估")
    public R<Long> add(@Valid @RequestBody SubjectEvaluationAddDTO dto) {
        return R.ok(subjectEvaluationService.add(dto));
    }

    /**
     * 修改学科评估
     */
    @PutMapping("/{id}")
    @OperationLog(module = "学科评估管理", action = "修改学科评估")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SubjectEvaluationUpdateDTO dto) {
        subjectEvaluationService.update(id, dto);
        return R.ok();
    }

    /**
     * 修改学科评估状态（禁用/启用）
     */
    @PutMapping("/{id}/status")
    @OperationLog(module = "学科评估管理", action = "修改学科评估状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        subjectEvaluationService.updateStatus(id, dto.getStatus().intValue());
        return R.ok();
    }

    /**
     * 软删除学科评估（可恢复）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "学科评估管理", action = "软删除学科评估")
    public R<Void> delete(@PathVariable Long id) {
        subjectEvaluationService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除学科评估（永久删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "学科评估管理", action = "硬删除学科评估")
    public R<Void> hardDelete(@PathVariable Long id) {
        subjectEvaluationService.hardDelete(id);
        return R.ok();
    }

    /**
     * 批量软删除学科评估
     */
    @DeleteMapping("/batch")
    @OperationLog(module = "学科评估管理", action = "批量软删除学科评估")
    public R<Void> batchDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        subjectEvaluationService.batchDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 批量硬删除学科评估
     */
    @DeleteMapping("/batch/hard")
    @OperationLog(module = "学科评估管理", action = "批量硬删除学科评估")
    public R<Void> batchHardDelete(@Valid @RequestBody BatchDeleteDTO dto) {
        subjectEvaluationService.batchHardDelete(dto.getIds());
        return R.ok();
    }

    /**
     * 导入学科评估数据
     */
    @PostMapping("/import")
    @OperationLog(module = "学科评估管理", action = "导入学科评估数据")
    public R<Void> importSubjectEvaluations(@RequestParam("file") MultipartFile file) {
        subjectEvaluationService.importSubjectEvaluations(file);
        return R.ok();
    }
}
