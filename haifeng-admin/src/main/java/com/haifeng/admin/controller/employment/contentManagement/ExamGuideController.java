package com.haifeng.admin.controller.employment.contentManagement;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideQueryDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideStatusDTO;
import com.haifeng.admin.dto.employment.contentManagement.guide.ExamGuideUpdateDTO;
import com.haifeng.admin.service.employment.contentManagement.ExamGuideService;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideDetailVO;
import com.haifeng.admin.vo.employment.contentManagement.guide.ExamGuideListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招聘内容管理 - 备考指南管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/content-management/exam-guide")
@RequiredArgsConstructor
@RequireAdminModule("emp_content_guide")
@Validated
public class ExamGuideController {

    private final ExamGuideService examGuideService;
    @GetMapping("/list")
    public R<IPage<ExamGuideListVO>> list(@Valid ExamGuideQueryDTO dto) {
        return R.ok(examGuideService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<ExamGuideDetailVO> detail(@PathVariable Long id) {
        return R.ok(examGuideService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "招聘内容管理", action = "修改备考指南")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ExamGuideUpdateDTO dto) {
        examGuideService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "招聘内容管理", action = "删除备考指南")
    public R<Void> delete(@PathVariable Long id) {
        examGuideService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "招聘内容管理", action = "启用/禁用备考指南")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody ExamGuideStatusDTO dto) {
        examGuideService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "招聘内容管理", action = "批量删除备考指南")
    public R<Void> batchDelete(@RequestBody @NotEmpty @Size(max = 100) List<Long> ids) {
        examGuideService.batchDelete(ids);
        return R.ok();
    }
}
