package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PublicWelfarePositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.PublicWelfarePositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.PublicWelfarePositionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/welfare")
@RequiredArgsConstructor
public class PublicWelfarePositionController {

    private final PublicWelfarePositionService publicWelfarePositionService;

    @GetMapping("/list")
    public R<IPage<PublicWelfarePositionListVO>> list(@Valid PublicWelfarePositionQueryDTO dto) {
        return R.ok(publicWelfarePositionService.page(dto));
    }

    @RequireLogin
    @GetMapping("/{id}/detail")
    public R<PublicWelfarePositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(publicWelfarePositionService.detail(id));
    }

    @RequireLogin
    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改公益性岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PublicWelfarePositionUpdateDTO dto) {
        publicWelfarePositionService.update(id, dto);
        return R.ok();
    }

    @RequireLogin
    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除公益性岗位")
    public R<Void> delete(@PathVariable Long id) {
        publicWelfarePositionService.delete(id);
        return R.ok();
    }

    @RequireLogin
    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "启用/禁用公益性岗位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        publicWelfarePositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @RequireLogin
    @DeleteMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除公益性岗位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        publicWelfarePositionService.batchDelete(ids);
        return R.ok();
    }

    @RequireLogin
    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = publicWelfarePositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @RequireLogin
    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入公益性岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        publicWelfarePositionService.importExcel(file);
        return R.ok();
    }
}
