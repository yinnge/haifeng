package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.GrassrootsProjectPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.GrassrootsProjectPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.GrassrootsProjectPositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 基层服务管理 - 基层服务项目岗位管理（如三支一扶、西部计划等）
 */
@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/project")
@RequiredArgsConstructor
@RequireAdminModule("emp_grassroots_3s")
public class GrassrootsProjectPositionController {

    private final GrassrootsProjectPositionService grassrootsProjectPositionService;

    @GetMapping("/list")
    public R<IPage<GrassrootsProjectPositionListVO>> list(@Valid GrassrootsProjectPositionQueryDTO dto) {
        return R.ok(grassrootsProjectPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<GrassrootsProjectPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(grassrootsProjectPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改基层服务项目岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody GrassrootsProjectPositionUpdateDTO dto) {
        grassrootsProjectPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除基层服务项目岗位")
    public R<Void> delete(@PathVariable Long id) {
        grassrootsProjectPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "启用/禁用基层服务项目岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        grassrootsProjectPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除基层服务项目岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        grassrootsProjectPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = grassrootsProjectPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入基层服务项目岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        grassrootsProjectPositionService.importExcel(file);
        return R.ok();
    }
}
