package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/order")
@RequiredArgsConstructor
public class MemberOrderController {

    private final MemberOrderService orderService;

    /**
     * 分页查询订单列表
     */
    @GetMapping("/list")
    public R<IPage<OrderListVO>> list(@Valid OrderQueryDTO dto) {
        return R.ok(orderService.page(dto));
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public R<OrderDetailVO> detail(@PathVariable Long id) {
        return R.ok(orderService.detail(id));
    }

    /**
     * 查看订单微信明文
     */
    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看订单微信明文")
    public R<String> getWechat(@PathVariable Long id) {
        return R.ok(orderService.getWechatPlaintext(id));
    }

    /**
     * 删除订单（软删除/禁用）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "禁用订单")
    public R<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除订单（物理删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "用户管理", action = "硬删除订单")
    public R<Void> hardDelete(@PathVariable Long id) {
        orderService.hardDelete(id);
        return R.ok();
    }

    /**
     * 恢复订单
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "用户管理", action = "恢复订单")
    public R<Void> restore(@PathVariable Long id) {
        orderService.restore(id);
        return R.ok();
    }
}
