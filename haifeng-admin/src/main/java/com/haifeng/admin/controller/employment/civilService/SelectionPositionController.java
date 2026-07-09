package com.haifeng.admin.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.civilService.SelectionPositionService;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 体制内招录 - 选调生岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/civil-service/selection-position")
@RequiredArgsConstructor
@RequireAdminModule("emp_civil_selected")
public class SelectionPositionController {

    private final SelectionPositionService selectionPositionService;

    @GetMapping("/list")
    public R<IPage<SelectionPositionListVO>> list(@Valid SelectionPositionQueryDTO dto) {
        return R.ok(selectionPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<SelectionPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(selectionPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "体制内招录", action = "修改选调生岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SelectionPositionUpdateDTO dto) {
        selectionPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "体制内招录", action = "删除选调生岗位")
    public R<Void> delete(@PathVariable Long id) {
        selectionPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "体制内招录", action = "启用/禁用选调生岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        selectionPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "体制内招录", action = "批量删除选调生岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        selectionPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = selectionPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "体制内招录", action = "导入选调生岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        selectionPositionService.importExcel(file);
        return R.ok();
    }
}
