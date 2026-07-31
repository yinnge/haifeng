package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.OrderCreateDTO;
import com.haifeng.admin.dto.user.OrderQueryDTO;
import com.haifeng.admin.service.user.MemberOrderService;
import com.haifeng.admin.vo.user.OrderDetailVO;
import com.haifeng.admin.vo.user.OrderListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RateLimit;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 - 会员订单管理（创建/确认/取消/撤销/查询/删除）
 */
@RestController
@RequestMapping("/api/v1/admin/user/order")
@RequiredArgsConstructor
@RequireAdminModule("user_order")
@Validated
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
    public R<OrderDetailVO> detail(@PathVariable @Min(1) Long id) {
        return R.ok(orderService.detail(id));
    }

    /**
     * 查看订单微信明文
     */
    @GetMapping("/{id}/wechat")
    @OperationLog(module = "用户管理", action = "查看订单微信明文")
    @RateLimit(value = 10, time = 60)
    public R<String> getWechat(@PathVariable @Min(1) Long id) {
        return R.ok(orderService.getWechatPlaintext(id));
    }

    /**
     * 创建待支付订单
     */
    @PostMapping("/create")
    @OperationLog(module = "用户管理", action = "创建升级订单")
    public R<Long> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.createOrder(dto));
    }

    /**
     * 确认支付（触发升级）
     */
    @PutMapping("/{id}/confirm")
    @OperationLog(module = "用户管理", action = "确认订单支付")
    public R<Void> confirmOrder(@PathVariable @Min(1) Long id) {
        orderService.confirmOrder(id);
        return R.ok();
    }

    /**
     * 取消待支付订单
     */
    @PutMapping("/{id}/cancel")
    @OperationLog(module = "用户管理", action = "取消订单")
    public R<Void> cancelOrder(@PathVariable @Min(1) Long id) {
        orderService.cancelOrder(id);
        return R.ok();
    }

    /**
     * 撤销已确认订单（触发降级回退）
     */
    @PutMapping("/{id}/revoke")
    @OperationLog(module = "用户管理", action = "撤销订单")
    public R<Void> revokeOrder(@PathVariable @Min(1) Long id,
                               @RequestParam(required = false) String remark) {
        orderService.revokeOrder(id, remark);
        return R.ok();
    }

    /**
     * 硬删除订单（物理删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "用户管理", action = "硬删除订单")
    public R<Void> hardDelete(@PathVariable @Min(1) Long id) {
        orderService.hardDelete(id);
        return R.ok();
    }

}
