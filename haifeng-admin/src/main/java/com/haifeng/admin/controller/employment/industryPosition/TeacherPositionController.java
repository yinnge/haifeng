package com.haifeng.admin.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.PositionStatusUpdateDTO;
import com.haifeng.admin.dto.employment.industryPosition.teacher.*;
import com.haifeng.admin.service.employment.industryPosition.TeacherPositionService;
import com.haifeng.admin.vo.employment.industryPosition.teacher.*;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 行业专项招聘 - 教师招聘岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/industry-position/teacher")
@RequiredArgsConstructor
@RequireAdminModule("emp_industry_teacher")
@Validated
public class TeacherPositionController {

    private final TeacherPositionService teacherPositionService;

    @GetMapping("/list")
    public R<IPage<TeacherPositionListVO>> list(@Valid TeacherPositionQueryDTO dto) {
        return R.ok(teacherPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<TeacherPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(teacherPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "行业专项招聘", action = "修改教师招聘岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody TeacherPositionUpdateDTO dto) {
        teacherPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "行业专项招聘", action = "删除教师招聘岗位")
    public R<Void> delete(@PathVariable Long id) {
        teacherPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "行业专项招聘", action = "更新教师招聘岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        teacherPositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "行业专项招聘", action = "批量删除教师招聘岗位")
    public R<Void> batchDelete(
            @Valid @RequestBody
            @NotEmpty(message = "ids不能为空")
            @Size(max = 100, message = "单次最多删除100条")
            List<Long> ids
    ) {
        teacherPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = teacherPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.fail(400, result);
    }

    @PostMapping("/import")
    @OperationLog(module = "行业专项招聘", action = "导入教师招聘岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        teacherPositionService.importExcel(file);
        return R.ok();
    }
}
