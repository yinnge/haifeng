package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.NotificationBroadcastDTO;
import com.haifeng.admin.dto.user.NotificationQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.vo.user.NotificationListVO;
import com.haifeng.common.dto.user.BroadcastNotificationResult;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.haifeng.common.vo.user.NotificationRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.RowBounds;
import org.springframework.scheduling.annotation.Async;
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
    private final SqlSessionFactory sqlSessionFactory;

    @Override
    public IPage<NotificationListVO> page(NotificationQueryDTO dto) {
        // 群发通知：按 title+content+created_at 分组，返回一条 + 接收人数
        if ("system_notice".equals(dto.getNotificationType())) {
            return broadcastPage(dto);
        }
        // 其他类型：保持原有逻辑（单条展示）
        return individualPage(dto);
    }

    /**
     * 群发通知分组分页查询
     */
    private IPage<NotificationListVO> broadcastPage(NotificationQueryDTO dto) {
        Page<NotificationListVO> resultPage = new Page<>(dto.getPage(), dto.getSize());
        int offset = (dto.getPage() - 1) * dto.getSize();

        List<BroadcastNotificationResult> broadcastResults = memberNotificationMapper.selectBroadcastPage(
                new RowBounds(offset, dto.getSize()));
        long total = memberNotificationMapper.countBroadcastGroups();

        // 转换为 NotificationListVO
        List<NotificationListVO> records = broadcastResults.stream().map(result -> {
            NotificationListVO vo = new NotificationListVO();
            vo.setId(result.getId());
            vo.setBroadcastId(result.getBroadcastId());
            vo.setNotificationType(result.getNotificationType());
            NotificationType typeEnum = NotificationType.fromValue(result.getNotificationType());
            vo.setNotificationTypeDesc(typeEnum != null ? typeEnum.getDesc() : result.getNotificationType());
            vo.setTitle(result.getTitle());
            vo.setContent(result.getContent());
            vo.setCreatedAt(result.getCreatedAt());
            vo.setRecipientCount(result.getRecipientCount());
            Integer disabledCount = result.getDisabledCount() == null ? 0 : result.getDisabledCount();
            vo.setDisabledCount(disabledCount);
            // 完全禁用：已禁用人数 == 总人数；部分禁用：disabledCount > 0 但 < 总人数
            vo.setDisabled(disabledCount > 0 && disabledCount.equals(result.getRecipientCount()));
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(records);
        resultPage.setTotal(total);
        return resultPage;
    }

    /**
     * 单条通知分页查询
     * showDisabled=true 时使用自定义 SQL 绕过 @TableLogic 自动过滤
     */
    private IPage<NotificationListVO> individualPage(NotificationQueryDTO dto) {
        // showDisabled=true：使用自定义查询，绕过 @TableLogic
        if (Boolean.TRUE.equals(dto.getShowDisabled())) {
            return individualPageWithDisabled(dto);
        }
        // 默认：使用 BaseMapper 查询（@TableLogic 自动过滤已删除记录）
        return individualPageNormal(dto);
    }

    /**
     * 查询含已禁用记录的分页
     * 使用 NotificationRecordVO 绕过 @TableLogic 对 deleted 字段的映射干扰
     */
    private IPage<NotificationListVO> individualPageWithDisabled(NotificationQueryDTO dto) {
        Page<NotificationListVO> resultPage = new Page<>(dto.getPage(), dto.getSize());
        int offset = (dto.getPage() - 1) * dto.getSize();

        List<NotificationRecordVO> records = memberNotificationMapper.selectAllWithDisabled(
                dto.getNotificationType(),
                dto.getMemberId(),
                dto.getIsRead(),
                new RowBounds(offset, dto.getSize()));

        long total = memberNotificationMapper.countAllWithDisabled(
                dto.getNotificationType(),
                dto.getMemberId(),
                dto.getIsRead());

        Set<Long> memberIds = records.stream()
                .map(NotificationRecordVO::getMemberId)
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
        List<NotificationListVO> voList = records.stream().map(record -> {
            NotificationListVO vo = new NotificationListVO();
            vo.setId(record.getId());
            vo.setMemberId(record.getMemberId());
            vo.setMemberName(finalMemberNameMap.get(record.getMemberId()));
            vo.setTitle(record.getTitle());
            vo.setContent(record.getContent());
            vo.setIsRead(record.getIsRead());
            vo.setCreatedAt(record.getCreatedAt());
            vo.setReadAt(record.getReadAt());
            vo.setDisabled(record.getDisabled());
            vo.setNotificationType(record.getNotificationType());
            NotificationType typeEnum = NotificationType.fromValue(record.getNotificationType());
            vo.setNotificationTypeDesc(typeEnum != null ? typeEnum.getDesc() : record.getNotificationType());
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        resultPage.setTotal(total);
        return resultPage;
    }

    /**
     * 仅查询未禁用记录的分页（原有逻辑）
     */
    private IPage<NotificationListVO> individualPageNormal(NotificationQueryDTO dto) {
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
            vo.setId(notification.getId());
            vo.setMemberId(notification.getMemberId());
            vo.setMemberName(finalMemberNameMap.get(notification.getMemberId()));
            vo.setTitle(notification.getTitle());
            vo.setContent(notification.getContent());
            vo.setIsRead(notification.getIsRead());
            vo.setCreatedAt(notification.getCreatedAt());
            vo.setReadAt(notification.getReadAt());
            vo.setDisabled(false);
            if (notification.getNotificationType() != null) {
                vo.setNotificationType(notification.getNotificationType().getValue());
                vo.setNotificationTypeDesc(notification.getNotificationType().getDesc());
            }
            return vo;
        });
    }

    @Override
    @Async("broadcastExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void broadcast(NotificationBroadcastDTO dto) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);
        wrapper.eq(Member::getStatus, "active");
        wrapper.select(Member::getId);

        List<Member> activeMembers = memberMapper.selectList(wrapper);

        if (activeMembers.isEmpty()) {
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        Long broadcastId = SnowflakeIdGenerator.nextId();
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            MemberNotificationMapper batchMapper = sqlSession.getMapper(MemberNotificationMapper.class);
            for (Member member : activeMembers) {
                batchMapper.insert(MemberNotification.builder()
                        .id(SnowflakeIdGenerator.nextId())
                        .memberId(member.getId())
                        .notificationType(NotificationType.SYSTEM_NOTICE)
                        .broadcastId(broadcastId)
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .isRead(false)
                        .deleted(false)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());
            }
            sqlSession.commit();
        }

        log.info("群发系统公告成功: title={}, count={}", dto.getTitle(), activeMembers.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        MemberNotification notification = memberNotificationMapper.selectByIdIgnoreDeleted(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }
        if (Boolean.TRUE.equals(notification.getDeleted())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通知已被禁用");
        }

        // 使用自定义 SQL 绕过 @TableLogic 对 updateById 的限制
        memberNotificationMapper.logicalDeleteById(id, OffsetDateTime.now());

        log.info("删除通知成功: notificationId={}", id);
    }

    @Override
    public void sendNotification(Long memberId, NotificationType type, String title, String content, Long relatedId) {
        OffsetDateTime now = OffsetDateTime.now();
        MemberNotification notification = MemberNotification.builder()
                .id(SnowflakeIdGenerator.nextId())
                .memberId(memberId)
                .notificationType(type)
                .title(title)
                .content(content)
                .relatedId(relatedId)
                .isRead(false)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        memberNotificationMapper.insert(notification);

        log.info("发送通知成功: memberId={}, type={}", memberId, type.getValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        MemberNotification notification = memberNotificationMapper.selectByIdIgnoreDeleted(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "通知不存在");
        }

        memberNotificationMapper.hardDeleteById(id);
        log.info("硬删除通知成功: notificationId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        // 直接执行恢复 SQL（自带 WHERE is_deleted = true 条件，未禁用时 affected=0）
        int affected = memberNotificationMapper.restoreById(id, OffsetDateTime.now());
        if (affected == 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该通知不存在或未被禁用");
        }
        log.info("恢复通知成功: notificationId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeBroadcast(Long broadcastId) {
        if (memberNotificationMapper.countByBroadcastId(broadcastId) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群发公告不存在");
        }

        int updated = memberNotificationMapper.revokeBroadcast(broadcastId, OffsetDateTime.now());
        log.info("整批撤回群发公告成功: broadcastId={}, 影响条数={}", broadcastId, updated);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreBroadcast(Long broadcastId) {
        if (memberNotificationMapper.countByBroadcastId(broadcastId) == 0) {
            throw new BusinessException(ResultCode.NOT_FOUND, "群发公告不存在");
        }

        int updated = memberNotificationMapper.restoreBroadcast(broadcastId, OffsetDateTime.now());
        log.info("整批恢复群发公告成功: broadcastId={}, 影响条数={}", broadcastId, updated);
    }
}
