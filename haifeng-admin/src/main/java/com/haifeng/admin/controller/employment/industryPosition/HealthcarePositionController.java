package com.haifeng.admin.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.healthcare.*;
import com.haifeng.admin.service.employment.industryPosition.HealthcarePositionService;
import com.haifeng.admin.vo.employment.industryPosition.healthcare.*;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequireLogin
@RestController
@RequestMapping("/api/v1/admin/employment/industry-position/healthcare")
@RequiredArgsConstructor
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
    @OperationLog(module = "行业专项招聘", action = "启用/禁用医疗卫生岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody HealthcarePositionStatusDTO dto) {
        healthcarePositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "行业专项招聘", action = "批量删除医疗卫生岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        healthcarePositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = healthcarePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "行业专项招聘", action = "导入医疗卫生岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        healthcarePositionService.importExcel(file);
        return R.ok();
    }
}
