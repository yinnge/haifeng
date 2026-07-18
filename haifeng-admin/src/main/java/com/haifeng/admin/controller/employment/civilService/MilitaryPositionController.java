package com.haifeng.admin.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.MilitaryPositionUpdateDTO;
import com.haifeng.admin.dto.employment.civilService.PositionStatusUpdateDTO;
import com.haifeng.admin.service.employment.civilService.MilitaryPositionService;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.MilitaryPositionListVO;
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
 * 体制内招录 - 部队文职岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/civil-service/military-position")
@RequiredArgsConstructor
@RequireAdminModule("emp_civil_military")
@Validated
public class MilitaryPositionController {

    private final MilitaryPositionService militaryPositionService;

    @GetMapping("/list")
    public R<IPage<MilitaryPositionListVO>> list(@Valid MilitaryPositionQueryDTO dto) {
        return R.ok(militaryPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<MilitaryPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(militaryPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "体制内招录", action = "修改部队文职岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody MilitaryPositionUpdateDTO dto) {
        militaryPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "体制内招录", action = "删除部队文职岗位")
    public R<Void> delete(@PathVariable Long id) {
        militaryPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "体制内招录", action = "更新部队文职岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        militaryPositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "体制内招录", action = "批量删除部队文职岗位")
    public R<Void> batchDelete(
            @Valid @RequestBody
            @NotEmpty(message = "ids不能为空")
            @Size(max = 100, message = "单次最多删除100条")
            List<Long> ids
    ) {
        militaryPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<Void> preValidate(@RequestParam("file") MultipartFile file) {
        String result = militaryPositionService.preValidate(file);
        if (result != null) {
            return R.fail(400, result);
        }
        return R.ok();
    }

    @PostMapping("/import")
    @OperationLog(module = "体制内招录", action = "导入部队文职岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        militaryPositionService.importExcel(file);
        return R.ok();
    }
}
