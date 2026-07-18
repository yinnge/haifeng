package com.haifeng.admin.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.PositionStatusUpdateDTO;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.*;
import com.haifeng.admin.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.*;
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
 * 行业专项招聘 - 医疗卫生岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/industry-position/healthcare")
@RequiredArgsConstructor
@RequireAdminModule("emp_industry_medical")
@Validated
public class HealthcarePositionController {

    private final HealthcarePositionService healthcarePositionService;

    @GetMapping("/list")
    public R<IPage<HealthcarePositionListVO>> list(@Valid HealthcarePositionQueryDTO dto) {
        return R.ok(healthcarePositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<HealthcarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(healthcarePositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "行业专项招聘", action = "修改医疗卫生岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody HealthcarePositionUpdateDTO dto) {
        healthcarePositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "行业专项招聘", action = "删除医疗卫生岗位")
    public R<Void> delete(@PathVariable Long id) {
        healthcarePositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "行业专项招聘", action = "更新医疗卫生岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        healthcarePositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "行业专项招聘", action = "批量删除医疗卫生岗位")
    public R<Void> batchDelete(
            @Valid @RequestBody
            @NotEmpty(message = "ids不能为空")
            @Size(max = 100, message = "单次最多删除100条")
            List<Long> ids
    ) {
        healthcarePositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = healthcarePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.fail(400, result);
    }

    @PostMapping("/import")
    @OperationLog(module = "行业专项招聘", action = "导入医疗卫生岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        healthcarePositionService.importExcel(file);
        return R.ok();
    }
}
