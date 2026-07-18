package com.haifeng.admin.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.SelectionPositionUpdateDTO;
import com.haifeng.admin.dto.employment.civilService.PositionStatusUpdateDTO;
import com.haifeng.admin.service.employment.civilService.SelectionPositionService;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.SelectionPositionListVO;
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
 * 体制内招录 - 选调生岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/civil-service/selection-position")
@RequiredArgsConstructor
@RequireAdminModule("emp_civil_selected")
@Validated
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
    @OperationLog(module = "体制内招录", action = "更新选调生岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        selectionPositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "体制内招录", action = "批量删除选调生岗位")
    public R<Void> batchDelete(
            @Valid @RequestBody
            @NotEmpty(message = "ids不能为空")
            @Size(max = 100, message = "单次最多删除100条")
            List<Long> ids
    ) {
        selectionPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<Void> preValidate(@RequestParam("file") MultipartFile file) {
        String result = selectionPositionService.preValidate(file);
        if (result != null) {
            return R.fail(400, result);
        }
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "体制内招录", action = "导入选调生岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        selectionPositionService.importExcel(file);
        return R.ok();
    }
}
