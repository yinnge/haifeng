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
     * 删除佣金记录
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除佣金记录")
    public R<Void> delete(@PathVariable Long id) {
        commissionService.delete(id);
        return R.ok();
    }
}
