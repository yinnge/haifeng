package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireAdminModule;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 - 通知消息查询与系统公告群发
 */
@RestController
@RequestMapping("/api/v1/admin/user/notification")
@RequiredArgsConstructor
@RequireAdminModule("user_notification")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 分页查询通知列表
     */
    @GetMapping("/list")
    public R<IPage<NotificationListVO>> list(@Valid NotificationQueryDTO dto) {
        return R.ok(notificationService.page(dto));
    }

    /**
     * 群发系统公告
     */
    @PostMapping("/broadcast")
    @OperationLog(module = "用户管理", action = "群发系统公告")
    public R<String> broadcast(@Valid @RequestBody NotificationBroadcastDTO dto) {
        notificationService.broadcast(dto);
        return R.ok("群发任务已提交");
    }

    /**
     * 删除通知（软删除/禁用）
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "禁用通知")
    public R<Void> delete(@PathVariable @Min(1) Long id) {
        notificationService.delete(id);
        return R.ok();
    }

    /**
     * 硬删除通知（物理删除）
     */
    @DeleteMapping("/{id}/hard")
    @OperationLog(module = "用户管理", action = "硬删除通知")
    public R<Void> hardDelete(@PathVariable @Min(1) Long id) {
        notificationService.hardDelete(id);
        return R.ok();
    }

    /**
     * 恢复通知
     */
    @PutMapping("/{id}/restore")
    @OperationLog(module = "用户管理", action = "恢复通知")
    public R<Void> restore(@PathVariable @Min(1) Long id) {
        notificationService.restore(id);
        return R.ok();
    }
}
