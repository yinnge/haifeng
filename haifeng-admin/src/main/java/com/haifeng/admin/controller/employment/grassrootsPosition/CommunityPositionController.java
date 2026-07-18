package com.haifeng.admin.controller.employment.grassrootsPosition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionQueryDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.CommunityPositionUpdateDTO;
import com.haifeng.admin.dto.employment.grassrootsPosition.PositionStatusUpdateDTO;
import com.haifeng.admin.service.employment.grassrootsPosition.CommunityPositionService;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionDetailVO;
import com.haifeng.admin.vo.employment.grassrootsPosition.CommunityPositionListVO;
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
 * 基层服务管理 - 社区工作者岗位管理
 */
@RestController
@RequestMapping("/api/v1/admin/employment/grassroots-position/community")
@RequiredArgsConstructor
@RequireAdminModule("emp_grassroots_comm")
@Validated
public class CommunityPositionController {

    private final CommunityPositionService communityPositionService;

    @GetMapping("/list")
    public R<IPage<CommunityPositionListVO>> list(@Valid CommunityPositionQueryDTO dto) {
        return R.ok(communityPositionService.page(dto));
    }

    @GetMapping("/{id}/detail")
    public R<CommunityPositionDetailVO> detail(@PathVariable Long id) {
        return R.ok(communityPositionService.detail(id));
    }

    @PutMapping("/{id}/update")
    @OperationLog(module = "基层服务管理", action = "修改社区工作者岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody CommunityPositionUpdateDTO dto) {
        communityPositionService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}/delete")
    @OperationLog(module = "基层服务管理", action = "删除社区工作者岗位")
    public R<Void> delete(@PathVariable Long id) {
        communityPositionService.delete(id);
        return R.ok();
    }

    @PatchMapping("/{id}/status")
    @OperationLog(module = "基层服务管理", action = "更新社区工作者岗位状态")
    public R<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody PositionStatusUpdateDTO dto) {
        communityPositionService.updateStatus(id, dto.getPositionStatus());
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "基层服务管理", action = "批量删除社区工作者岗位")
    public R<Void> batchDelete(@Valid @RequestBody @NotEmpty @Size(max = 100) List<Long> ids) {
        communityPositionService.batchDelete(ids);
        return R.ok();
    }

    @PostMapping("/pre-validate")
    public R<String> preValidate(@RequestParam("file") MultipartFile file) {
        String result = communityPositionService.preValidate(file);
        return result == null ? R.ok("校验通过") : R.fail(400, result);
    }

    @PostMapping("/import")
    @OperationLog(module = "基层服务管理", action = "导入社区工作者岗位")
    public R<Void> importExcel(@RequestParam("file") MultipartFile file) {
        communityPositionService.importExcel(file);
        return R.ok();
    }
}
