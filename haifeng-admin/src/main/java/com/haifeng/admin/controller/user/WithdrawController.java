package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.service.user.WithdrawService;
import com.haifeng.admin.vo.user.WithdrawListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/withdraw")
@RequiredArgsConstructor
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
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(withdrawService.getWechatPlaintext(id));
    }

    /**
     * 处理提现
     */
    @PutMapping("/{id}/process")
    @OperationLog(module = "用户管理", action = "处理提现")
    public R<Void> process(@PathVariable Long id, @Valid @RequestBody WithdrawProcessDTO dto) {
        withdrawService.process(id, dto);
        return R.ok();
    }

    /**
     * 删除提现记录
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除提现记录")
    public R<Void> delete(@PathVariable Long id) {
        withdrawService.delete(id);
        return R.ok();
    }
}
