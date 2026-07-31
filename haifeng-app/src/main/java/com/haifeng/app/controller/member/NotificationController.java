package com.haifeng.app.controller.member;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.member.NotificationQueryDTO;
import com.haifeng.app.service.member.NotificationService;
import com.haifeng.app.vo.member.NotificationDetailVO;
import com.haifeng.app.vo.member.NotificationListVO;
import com.haifeng.app.vo.member.UnreadCountVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import com.haifeng.common.vo.user.NotificationTypeVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/app/member/notification")
@RequiredArgsConstructor
@RequireLogin
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recent")
    public R<List<NotificationListVO>> recent() {
        return R.ok(notificationService.recent());
    }

    @GetMapping("/list")
    public R<IPage<NotificationListVO>> list(@Valid NotificationQueryDTO dto) {
        return R.ok(notificationService.page(dto));
    }

    @GetMapping("/{notificationId}")
    public R<NotificationDetailVO> detail(@PathVariable @Min(1) Long notificationId) {
        return R.ok(notificationService.detail(notificationId));
    }

    @GetMapping("/unread-count")
    public R<UnreadCountVO> unreadCount() {
        return R.ok(notificationService.getUnreadCount());
    }

    @GetMapping("/types")
    public R<List<NotificationTypeVO>> types() {
        return R.ok(NotificationTypeVO.all());
    }

    @PutMapping("/{notificationId}/read")
    public R<Void> markAsRead(@PathVariable @Min(1) Long notificationId) {
        notificationService.markAsRead(notificationId);
        return R.ok();
    }

    @PutMapping("/read-all")
    public R<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return R.ok();
    }

    @PutMapping("/read-others")
    public R<Void> markOthersAsRead() {
        notificationService.markOthersAsRead();
        return R.ok();
    }

    @DeleteMapping("/{notificationId}")
    public R<Void> delete(@PathVariable @Min(1) Long notificationId) {
        notificationService.delete(notificationId);
        return R.ok();
    }

    @DeleteMapping("/clean-expired")
    public R<Integer> cleanExpired() {
        return R.ok(notificationService.cleanExpired());
    }
}
