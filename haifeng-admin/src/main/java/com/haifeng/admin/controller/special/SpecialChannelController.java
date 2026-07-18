package com.haifeng.admin.controller.special;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.special.SpecialChannelAddDTO;
import com.haifeng.admin.dto.special.SpecialChannelBatchDeleteDTO;
import com.haifeng.admin.dto.special.SpecialChannelQueryDTO;
import com.haifeng.admin.service.special.SpecialChannelService;
import com.haifeng.admin.vo.special.SpecialChannelDetailVO;
import com.haifeng.admin.vo.special.SpecialChannelListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 特殊通道 - 招生通道管理（如强基计划、综合评价等）
 */
@Validated
@RestController
@RequestMapping("/api/v1/admin/special/channel")
@RequiredArgsConstructor
@RequireAdminModule("special_admission")
public class SpecialChannelController {

    private final SpecialChannelService specialChannelService;

    @GetMapping("/page")
    public R<IPage<SpecialChannelListVO>> page(@Valid SpecialChannelQueryDTO dto) {
        return R.ok(specialChannelService.page(dto));
    }

    @GetMapping("/{id}")
    public R<SpecialChannelDetailVO> detail(@PathVariable Long id) {
        return R.ok(specialChannelService.detail(id));
    }

    @PostMapping
    @OperationLog(module = "特殊通道管理", action = "新增招生通道")
    public R<Void> add(@Valid @RequestBody SpecialChannelAddDTO dto) {
        specialChannelService.add(dto);
        return R.ok();
    }

    @PutMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "修改招生通道")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody SpecialChannelAddDTO dto) {
        specialChannelService.update(id, dto);
        return R.ok();
    }

    @PutMapping("/{id}/toggle")
    @OperationLog(module = "特殊通道管理", action = "切换通道状态")
    public R<Void> toggleActive(@PathVariable Long id) {
        specialChannelService.toggleActive(id);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @OperationLog(module = "特殊通道管理", action = "删除招生通道")
    public R<Void> delete(@PathVariable Long id) {
        specialChannelService.delete(id);
        return R.ok();
    }

    @PostMapping("/batch-delete")
    @OperationLog(module = "特殊通道管理", action = "批量删除招生通道")
    public R<Void> batchDelete(@Valid @RequestBody SpecialChannelBatchDeleteDTO dto) {
        specialChannelService.batchDelete(dto.getIds());
        return R.ok();
    }
}
