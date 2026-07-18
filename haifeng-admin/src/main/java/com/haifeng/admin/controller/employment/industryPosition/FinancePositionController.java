package com.haifeng.admin.controller.employment.industryPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.industryPosition.PositionStatusUpdateDTO;
import com.haifeng.admin.dto.employment.industryPosition.finance.*;
import com.haifeng.admin.service.employment.industryPosition.FinancePositionService;
import com.haifeng.admin.vo.employment.industryPosition.finance.*;
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
 * 行业专项招聘 - 银行/金融岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/industry-position/finance")
@RequiredArgsConstructor
@RequireAdminModule("emp_industry_bank")
@Validated
public class FinancePositionController {

    private final FinancePositionService financePositionService;

    @GetMapping("/list")
    public R<IPage<FinancePositionListVO>> list(@Valid FinancePositionQueryDTO dto) {
        return R.ok(financePositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<FinancePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(financePositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "行业专项招聘", action = "修改银行/金融招聘岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody FinancePositionUpdateDTO dto) {
        financePositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "行业专项招聘", action = "删除银行/金融招聘岗位")
    public R<Void> delete(@PathVariable Long id) {
        financePositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "行业专项招聘", action = "更新银行/金融招聘岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        financePositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "行业专项招聘", action = "批量删除银行/金融招聘岗位")
    public R<Void> batchDelete(
            @Valid @RequestBody
            @NotEmpty(message = "ids不能为空")
            @Size(max = 100, message = "单次最多删除100条")
            List<Long> ids
    ) {
        financePositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = financePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.fail(400, result);
    }

    @PostMapping("/import")
    @OperationLog(module = "行业专项招聘", action = "导入银行/金融招聘岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        financePositionService.importExcel(file);
        return R.ok();
    }
}
