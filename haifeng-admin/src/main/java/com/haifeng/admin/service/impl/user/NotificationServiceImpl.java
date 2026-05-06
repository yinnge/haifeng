package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final MemberNotificationMapper memberNotificationMapper;
    private final MemberMapper memberMapper;

    @Override
    public IPage<NotificationListVO> page(NotificationQueryDTO dto) {
        Page<MemberNotification> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<MemberNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberNotification::getDeleted, false);

        if (StringUtils.hasText(dto.getNotificationType())) {
            wrapper.eq(MemberNotification::getNotificationType, dto.getNotificationType());
        }
        if (dto.getMemberId() != null) {
            wrapper.eq(MemberNotification::getMemberId, dto.getMemberId());
        }
        if (dto.getIsRead() != null) {
            wrapper.eq(MemberNotification::getIsRead, dto.getIsRead());
        }

        wrapper.orderByDesc(MemberNotification::getCreatedAt);

        IPage<MemberNotification> notificationPage = memberNotificationMapper.selectPage(page, wrapper);

        Set<Long> memberIds = notificationPage.getRecords().stream()
                .map(MemberNotification::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, String> memberNameMap = new HashMap<>();
        if (!memberIds.isEmpty()) {
            LambdaQueryWrapper<Member> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.in(Member::getId, memberIds);
            memberWrapper.select(Member::getId, Member::getUsername);
            List<Member> members = memberMapper.selectList(memberWrapper);
            memberNameMap = members.stream()
                    .collect(Collectors.toMap(Member::getId, Member::getUsername, (a, b) -> a));
        }

        Map<Long, String> finalMemberNameMap = memberNameMap;
        return notificationPage.convert(notification -> {
            NotificationListVO vo = new NotificationListVO();
            BeanUtils.copyProperties(notification, vo);
            vo.setMemberName(finalMemberNameMap.get(notification.getMemberId()));
            if (notification.getNotificationType() != null) {
                vo.setNotificationType(notification.getNotificationType().getValue());
            }
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int broadcast(NotificationBroadcastDTO dto) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);
        wrapper.eq(Member::getStatus, "active");
        wrapper.select(Member::getId);

        List<Member> activeMembers = memberMapper.selectList(wrapper);

        if (activeMembers.isEmpty()) {
            return 0;
        }

        int count = 0;
        OffsetDateTime now = OffsetDateTime.now();
        for (Member member : activeMembers) {
            MemberNotification notification = MemberNotification.builder()
                    .id(SnowflakeIdGenerator.nextId())
                    .memberId(member.getId())
                    .notificationType(NotificationType.SYSTEM_NOTICE)
                    .title(dto.getTitle())
                    .content(dto.getContent())
                    .isRead(false)
                    .deleted(false)
                    .createdAt(now)
                    .build();
            memberNotificationMapper.insert(notification);
            count++;
        }

        log.info("群发系统公告成功: title={}, count={}", dto.getTitle(), count);
        return count;
    }

    @Override
    public void delete(Long id) {
        MemberNotification notification = memberNotificationMapper.selectById(id);
        if (notification == null || notification.getDeleted()) {
            throw new BusinessException(404, "通知不存在");
        }

        notification.setDeleted(true);
        memberNotificationMapper.updateById(notification);

        log.info("删除通知成功: notificationId={}", id);
    }

    @Override
    public void sendNotification(Long memberId, NotificationType type, String title, String content, Long relatedId) {
        MemberNotification notification = MemberNotification.builder()
                .id(SnowflakeIdGenerator.nextId())
                .memberId(memberId)
                .notificationType(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .isRead(false)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .build();
        memberNotificationMapper.insert(notification);

        log.info("发送通知成功: memberId={}, type={}", memberId, type.getValue());
    }
}
