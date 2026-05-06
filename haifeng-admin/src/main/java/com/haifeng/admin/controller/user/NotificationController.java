package com.haifeng.admin.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/user/notification")
@RequiredArgsConstructor
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
    public R<Integer> broadcast(@Valid @RequestBody NotificationBroadcastDTO dto) {
        return R.ok(notificationService.broadcast(dto));
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    @OperationLog(module = "用户管理", action = "删除通知")
    public R<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return R.ok();
    }
}
