package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.StrongBaseUnivAddDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivBatchDeleteDTO;
import com.haifeng.admin.dto.special.StrongBaseUnivQueryDTO;
import com.haifeng.admin.service.special.StrongBaseUnivService;
import com.haifeng.admin.vo.special.StrongBaseUnivDetailVO;
import com.haifeng.admin.vo.special.StrongBaseUnivListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 特殊通道 - 强基计划院校配置
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/special/strong-base-univ")
@RequiredArgsConstructor
@RequireAdminModule("special_sbs_config")
public class StrongBaseUnivController {

    private final StrongBaseUnivService strongBaseUnivService;

    @GetMapping("/page")
    public R<IPage<StrongBaseUnivListVO>> page(@Valid StrongBaseUnivQueryDTO dto) {
        return R.ok(strongBaseUnivService.page(dto));
    }

    @GetMapping("/{id}")
    public R<StrongBaseUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(strongBaseUnivService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "强基计划管理", action = "新增强基院校配置")
    public R<Void> add(@Valid @RequestBody StrongBaseUnivAddDTO dto) {
        strongBaseUnivService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "修改强基院校配置")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody StrongBaseUnivAddDTO dto) {
        strongBaseUnivService.update(id, dto);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "强基计划管理", action = "删除强基院校配置")
    public R<Void> delete(@PathVariable Long id) {
        strongBaseUnivService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "强基计划管理", action = "批量删除强基院校配置")
    public R<Void> batchDelete(@Valid @RequestBody StrongBaseUnivBatchDeleteDTO dto) {
        strongBaseUnivService.batchDelete(dto.getIds());
        return R.ok();
    }
}
