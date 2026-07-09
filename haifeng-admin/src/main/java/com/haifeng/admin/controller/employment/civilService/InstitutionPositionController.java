package com.haifeng.admin.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.InstitutionPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.civilService.InstitutionPositionService;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.InstitutionPositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 体制内招录 - 事业编职位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/civil-service/institution-position")
@RequiredArgsConstructor
@RequireAdminModule("emp_civil_institution")
public class InstitutionPositionController {

    private final InstitutionPositionService institutionPositionService;

    @GetMapping("/list")
    public R<IPage<InstitutionPositionListVO>> list(@Valid InstitutionPositionQueryDTO dto) {
        return R.ok(institutionPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<InstitutionPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(institutionPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "体制内招录", action = "修改事业编职位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody InstitutionPositionUpdateDTO dto) {
        institutionPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "体制内招录", action = "删除事业编职位")
    public R<Void> delete(@PathVariable Long id) {
        institutionPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "体制内招录", action = "启用/禁用事业编职位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        institutionPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "体制内招录", action = "批量删除事业编职位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        institutionPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = institutionPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "体制内招录", action = "导入事业编职位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        institutionPositionService.importExcel(file);
        return R.ok();
    }
}
