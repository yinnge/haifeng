package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PositionStatusUpdateDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
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
 * 基层服务管理 - 公益性岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/welfare")
@RequiredArgsConstructor
@RequireAdminModule("emp_grassroots_welfare")
@Validated
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionQueryDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改公益性岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PublicWelfarePositionUpdateDTO dto) {
        publicWelfarePositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除公益性岗位")
    public R<Void> delete(@PathVariable Long id) {
        publicWelfarePositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "更新公益性岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        publicWelfarePositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除公益性岗位")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty @Size(max = 100) List<Long> ids) {
        publicWelfarePositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = publicWelfarePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.fail(400, result);
    }

    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入公益性岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        publicWelfarePositionService.importExcel(file);
        return R.ok();
    }
}
