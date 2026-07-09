package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelUnivAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivBatchDeleteDTO;
import com.haifeng.admin.dto.special.SpecialChannelUnivQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelUnivService;
import com.haifeng.admin.vo.special.SpecialChannelUnivDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelUnivListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 特殊通道 - 招生通道与院校的关联管理
 */
@RestController
@RequestMapping("/api/v1/admin/special/channel-univ")
@RequiredArgsConstructor
@RequireAdminModule("special_adm_univ")
public class SpecialChannelUnivController {

    private final SpecialChannelUnivService specialChannelUnivService;

    @GetMapping("/page")
    public R<IPage<SpecialChannelUnivListVO>> page(@Valid SpecialChannelUnivQueryDTO dto) {
        return R.ok(specialChannelUnivService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SpecialChannelUnivDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelUnivService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "特殊通道管理", action = "新增通道-大学关联")
    public R<Void> add(@Valid @RequestBody SpecialChannelUnivAddDTO dto) {
        specialChannelUnivService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "修改通道-大学关联")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SpecialChannelUnivAddDTO dto) {
        specialChannelUnivService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "特殊通道管理", action = "切换关联状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        specialChannelUnivService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "删除通道-大学关联")
    public R<Void> delete(@PathVariable Long id) {
        specialChannelUnivService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/batch")
    @OperationLog(module = "特殊通道管理", action = "批量删除通道-大学关联")
    public R<Void> batchDelete(@Valid @RequestBody SpecialChannelUnivBatchDeleteDTO dto) {
        specialChannelUnivService.batchDelete(dto.getIds());
        return R.ok();
    }
}
