package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.member.NotificationQueryDTO;
import com.haifeng.app.service.member.NotificationService;
import com.haifeng.app.vo.member.NotificationDetailVO;
import com.haifeng.app.vo.member.NotificationListVO;
import com.haifeng.app.vo.member.UnreadCountVO;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int RECENT_LIMIT = 5;
    private static final int EXPIRED_DAYS = 7;

    private final MemberNotificationMapper memberNotificationMapper;

    @Override
    public List<NotificationListVO> recent() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        List<MemberNotification> list = memberNotificationMapper.selectList(
                new LambdaQueryWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false)
                        .orderByDesc(MemberNotification::getCreatedAt)
                        .last("LIMIT " + RECENT_LIMIT));

        return list.stream().map(this::toListVO).collect(Collectors.toList());
    }

    @Override
    public IPage<NotificationListVO> page(NotificationQueryDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Page<MemberNotification> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<MemberNotification> wrapper = new LambdaQueryWrapper<MemberNotification>()
                .eq(MemberNotification::getMemberId, memberId)
                .orderByDesc(MemberNotification::getCreatedAt);

        if (dto.getNotificationType() != null) {
            wrapper.eq(MemberNotification::getNotificationType, dto.getNotificationType());
        }
        if (dto.getIsRead() != null) {
            wrapper.eq(MemberNotification::getIsRead, dto.getIsRead());
        }

        IPage<MemberNotification> entityPage = memberNotificationMapper.selectPage(page, wrapper);

        return entityPage.convert(this::toListVO);
    }

    @Override
    @Transactional
    public NotificationDetailVO detail(Long notificationId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberNotification notification = memberNotificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(OffsetDateTime.now());
            notification.setUpdatedAt(OffsetDateTime.now());
            memberNotificationMapper.updateById(notification);
        }

        return toDetailVO(notification);
    }

    @Override
    @Transactional
    public UnreadCountVO getUnreadCount() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Long count = memberNotificationMapper.selectCount(
                new LambdaQueryWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false));

        return UnreadCountVO.builder()
                .unreadCount(count.intValue())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberNotification notification = memberNotificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(403, "无权操作此通知");
        }
        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }

        notification.setIsRead(true);
        notification.setReadAt(OffsetDateTime.now());
        notification.setUpdatedAt(OffsetDateTime.now());
        memberNotificationMapper.updateById(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        int updated = memberNotificationMapper.update(null,
                new LambdaUpdateWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false)
                        .set(MemberNotification::getIsRead, true)
                        .set(MemberNotification::getReadAt, OffsetDateTime.now())
                        .set(MemberNotification::getUpdatedAt, OffsetDateTime.now()));

        log.info("全部标记已读: memberId={}, 更新数量={}", memberId, updated);
    }

    @Override
    @Transactional
    public void markOthersAsRead() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        List<MemberNotification> recentUnread = memberNotificationMapper.selectList(
                new LambdaQueryWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false)
                        .orderByDesc(MemberNotification::getCreatedAt)
                        .last("LIMIT " + RECENT_LIMIT));

        // If unread count <= RECENT_LIMIT, nothing to mark
        Long totalUnread = memberNotificationMapper.selectCount(
                new LambdaQueryWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false));
        if (totalUnread <= RECENT_LIMIT) {
            return;
        }

        List<Long> excludeIds = recentUnread.stream()
                .map(MemberNotification::getId)
                .collect(Collectors.toList());

        int updated = memberNotificationMapper.update(null,
                new LambdaUpdateWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, false)
                        .notIn(MemberNotification::getId, excludeIds)
                        .set(MemberNotification::getIsRead, true)
                        .set(MemberNotification::getReadAt, OffsetDateTime.now())
                        .set(MemberNotification::getUpdatedAt, OffsetDateTime.now()));

        log.info("全部已读（排除最近{}条）: memberId={}, 更新数量={}", RECENT_LIMIT, memberId, updated);
    }

    @Override
    @Transactional
    public void delete(Long notificationId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        MemberNotification notification = memberNotificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!notification.getMemberId().equals(memberId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        notification.setDeleted(true);
        notification.setUpdatedAt(OffsetDateTime.now());
        memberNotificationMapper.updateById(notification);
    }

    @Override
    @Transactional
    public int cleanExpired() {
        Long memberId = SecurityUtil.getCurrentMemberId();

        OffsetDateTime threshold = OffsetDateTime.now().minusDays(EXPIRED_DAYS);

        int deleted = memberNotificationMapper.update(null,
                new LambdaUpdateWrapper<MemberNotification>()
                        .eq(MemberNotification::getMemberId, memberId)
                        .eq(MemberNotification::getIsRead, true)
                        .lt(MemberNotification::getCreatedAt, threshold)
                        .set(MemberNotification::getDeleted, true)
                        .set(MemberNotification::getUpdatedAt, OffsetDateTime.now()));

        log.info("清除过期已读通知: memberId={}, 清除数量={}", memberId, deleted);
        return deleted;
    }

    private NotificationListVO toListVO(MemberNotification notification) {
        return NotificationListVO.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType().getValue())
                .notificationTypeDesc(notification.getNotificationType().getDesc())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private NotificationDetailVO toDetailVO(MemberNotification notification) {
        return NotificationDetailVO.builder()
                .id(notification.getId())
                .notificationType(notification.getNotificationType().getValue())
                .notificationTypeDesc(notification.getNotificationType().getDesc())
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
