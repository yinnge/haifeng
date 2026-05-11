package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.CommissionQueryDTO;
import com.haifeng.admin.service.user.CommissionService;
import com.haifeng.admin.vo.user.CommissionListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/commission")
@RequiredArgsConstructor
public class CommissionController {

    private final CommissionService commissionService;

    /**
     * 分页查询佣金列表
     */
    @GetMapping("/list")
    public R<IPage<CommissionListVO>> list(@Valid CommissionQueryDTO dto) {
        return R.ok(commissionService.page(dto));
    }

    /**
     * 删除佣金记录（软删除/禁用）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "禁用佣金记录")
    public R<Void> delete(@PathVariable Long id) {
        commissionService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除佣金记录（物理删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "用户管理", action = "硬删除佣金记录")
    public R<Void> hardDelete(@PathVariable Long id) {
        commissionService.hardDelete(id);
        return R.ok();
    }

    /**
     * 恢复佣金记录
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "用户管理", action = "恢复佣金记录")
    public R<Void> restore(@PathVariable Long id) {
        commissionService.restore(id);
        return R.ok();
    }
}
