package com.haifeng.admin.controller.employment.civilService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.civilService.CivilPositionQueryDTO;
import com.haifeng.admin.dto.employment.civilService.CivilPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.StatusDTO;
import com.haifeng.admin.service.employment.civilService.CivilPositionService;
import com.haifeng.admin.vo.employment.civilService.CivilPositionDetailVO;
import com.haifeng.admin.vo.employment.civilService.CivilPositionListVO;
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
@RequestMapping("/api/v1/admin/employment/civil-service/civil-position")
@RequiredArgsConstructor
public class CivilPositionController {

    private final CivilPositionService civilPositionService;

    @GetMapping("/list")
    public R<IPage<CivilPositionListVO>> list(@Valid CivilPositionQueryDTO dto) {
        return R.ok(civilPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<CivilPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(civilPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "体制内招录", action = "修改公务员职位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CivilPositionUpdateDTO dto) {
        civilPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "体制内招录", action = "删除公务员职位")
    public R<Void> delete(@PathVariable Long id) {
        civilPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "体制内招录", action = "启用/禁用公务员职位")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        civilPositionService.updateStatus(id, dto.getStatus());
        return R.ok();
    }

    @DeleteMapping("/batch-delete")
    @OperationLog(module = "体制内招录", action = "批量删除公务员职位")
    public R<Void> batchDelete(@Valid @RequestBody List<Long> ids) {
        civilPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = civilPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.ok(result);
    }

    @PostMapping("/import")
    @OperationLog(module = "体制内招录", action = "导入公务员职位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        civilPositionService.importExcel(file);
        return R.ok();
    }
}
