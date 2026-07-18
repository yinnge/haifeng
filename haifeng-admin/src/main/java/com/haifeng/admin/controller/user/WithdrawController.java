package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.service.user.WithdrawService;
import com.haifeng.admin.vo.user.WithdrawListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 - 提现审核处理（含查看微信明文、软/硬删除、恢复）
 */
@RestController
@RequestMapping("/api/v1/admin/user/withdraw")
@RequiredArgsConstructor
@RequireAdminModule("user_withdraw")
@Validated
public class WithdrawController {

    private final WithdrawService withdrawService;

    /**
     * 分页查询提现列表
     */
    @GetMapping("/list")
    public R<IPage<WithdrawListVO>> list(@Valid WithdrawQueryDTO dto) {
        return R.ok(withdrawService.page(dto));
    }

    /**
     * 查看提现微信明文
     */
    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看提现微信明文")
    public R<String> getWechat(@PathVariable @Min(1) Long id) {
        return R.ok(withdrawService.getWechatPlaintext(id));
    }

    /**
     * 处理提现
     */
    @PutMapping("/{id}/process")
    @OperationLog(module = "用户管理", action = "处理提现")
    public R<Void> process(@PathVariable @Min(1) Long id, @Valid @RequestBody WithdrawProcessDTO dto) {
        withdrawService.process(id, dto);
        return R.ok();
    }

    /**
     * 删除提现记录（软删除/禁用）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "禁用提现记录")
    public R<Void> delete(@PathVariable @Min(1) Long id) {
        withdrawService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除提现记录（物理删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "用户管理", action = "硬删除提现记录")
    public R<Void> hardDelete(@PathVariable @Min(1) Long id) {
        withdrawService.hardDelete(id);
        return R.ok();
    }

    /**
     * 恢复提现记录
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "用户管理", action = "恢复提现记录")
    public R<Void> restore(@PathVariable @Min(1) Long id) {
        withdrawService.restore(id);
        return R.ok();
    }
}
