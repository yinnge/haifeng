package com.haifeng.app.service.impl.consultation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.app.dto.consultation.ConsultationAddDTO;
import com.haifeng.app.dto.consultation.ConsultationQueryDTO;
import com.haifeng.app.service.consultation.ConsultationService;
import com.haifeng.app.vo.consultation.ConsultationDetailVO;
import com.haifeng.app.vo.consultation.ConsultationListVO;
import com.haifeng.app.vo.consultation.ConsultationUnhandledCountVO;
import com.haifeng.common.entity.consultation.Consultation;
import com.haifeng.common.entity.home.Institution;
import com.haifeng.common.entity.home.Planner;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.MemberNotification;
import com.haifeng.common.enums.ConsultationBizType;
import com.haifeng.common.enums.ConsultationStatus;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.consultation.ConsultationMapper;
import com.haifeng.common.mapper.home.InstitutionMapper;
import com.haifeng.common.mapper.home.PlannerMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberNotificationMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.IpUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

/**
 * 咨询留言：C 端提交 + 教师端收件箱。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private static final short STATUS_PUBLISHED = 1;

    private final ConsultationMapper consultationMapper;
    private final PlannerMapper plannerMapper;
    private final InstitutionMapper institutionMapper;
    private final MemberMapper memberMapper;
    private final MemberNotificationMapper memberNotificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submit(ConsultationBizType bizType, Long bizId, ConsultationAddDTO dto) {
        BizTarget target = resolveTarget(bizType, bizId);
        ensureTeacher(target.teacherMemberId());

        OffsetDateTime now = OffsetDateTime.now();
        Consultation consultation = Consultation.builder()
                .id(SnowflakeIdGenerator.nextId())
                .bizType(bizType)
                .bizId(bizId)
                .bizName(target.bizName())
                .teacherMemberId(target.teacherMemberId())
                .senderMemberId(SecurityUtil.getCurrentMemberId())
                .message(dto.getMessage().trim())
                .wechat(StringUtils.hasText(dto.getWechat()) ? dto.getWechat().trim() : null)
                .phone(dto.getPhone().trim())
                .clientIp(IpUtil.getClientIp())
                .status(ConsultationStatus.PENDING)
                .version(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        consultationMapper.insert(consultation);

        memberNotificationMapper.insert(MemberNotification.builder()
                .id(SnowflakeIdGenerator.nextId())
                .memberId(target.teacherMemberId())
                .notificationType(NotificationType.CONSULTATION_RECEIVED)
                .title("收到新的咨询留言")
                .content(buildNotificationContent(consultation))
                .relatedId(consultation.getId())
                .isRead(false)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build());

        log.info("收到咨询留言: consultationId={}, bizType={}, bizId={}, teacherMemberId={}",
                consultation.getId(), bizType.getValue(), bizId, target.teacherMemberId());
        return consultation.getId();
    }

    @Override
    public IPage<ConsultationListVO> page(ConsultationQueryDTO dto) {
        Long teacherMemberId = currentTeacherMemberId();

        Page<Consultation> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<Consultation>()
                .eq(Consultation::getTeacherMemberId, teacherMemberId)
                .orderByDesc(Consultation::getCreatedAt)
                .orderByDesc(Consultation::getId);

        if (dto.getStatus() != null) {
            ConsultationStatus status = ConsultationStatus.fromValue(dto.getStatus());
            if (status == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "状态值不合法");
            }
            wrapper.eq(Consultation::getStatus, status);
        }
        if (StringUtils.hasText(dto.getBizType())) {
            ConsultationBizType bizType = ConsultationBizType.fromValue(dto.getBizType().trim());
            if (bizType == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "业务类型不合法");
            }
            wrapper.eq(Consultation::getBizType, bizType);
        }

        return consultationMapper.selectPage(page, wrapper).convert(this::toListVO);
    }

    @Override
    public ConsultationDetailVO detail(Long id) {
        return toDetailVO(getOwned(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markHandled(Long id) {
        Consultation consultation = getOwned(id);
        if (consultation.getStatus() == ConsultationStatus.HANDLED) {
            return;
        }
        // 条件更新：并发重复点击只有一条能生效，天然幂等
        int rows = consultationMapper.markHandled(id, consultation.getTeacherMemberId(), OffsetDateTime.now());
        log.info("咨询留言标记已处理: consultationId={}, rows={}", id, rows);
    }

    @Override
    public ConsultationUnhandledCountVO unhandledCount() {
        Long teacherMemberId = currentTeacherMemberId();
        Long count = consultationMapper.selectCount(new LambdaQueryWrapper<Consultation>()
                .eq(Consultation::getTeacherMemberId, teacherMemberId)
                .eq(Consultation::getStatus, ConsultationStatus.PENDING));
        return ConsultationUnhandledCountVO.builder()
                .unhandledCount(count == null ? 0 : count.intValue())
                .build();
    }

    /**
     * 校验业务对象并解析接收教师与名称快照。
     */
    private BizTarget resolveTarget(ConsultationBizType bizType, Long bizId) {
        if (bizType == ConsultationBizType.PLANNER) {
            Planner planner = plannerMapper.selectById(bizId);
            if (planner == null || Boolean.TRUE.equals(planner.getDeleted())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "规划师不存在");
            }
            if (planner.getUserId() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "该规划师暂不可接收留言");
            }
            return new BizTarget(planner.getUserId(), planner.getName());
        }

        Institution institution = institutionMapper.selectById(bizId);
        if (institution == null || institution.getStatus() == null
                || institution.getStatus() != STATUS_PUBLISHED) {
            throw new BusinessException(ResultCode.NOT_FOUND, "培训机构不存在");
        }
        if (institution.getUserId() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该机构暂不可接收留言");
        }
        return new BizTarget(institution.getUserId(), institution.getName());
    }

    /**
     * 接收方必须仍是有效教师，避免留言发给已撤销教师身份、无法进入教师端的账号。
     */
    private void ensureTeacher(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null || Boolean.TRUE.equals(member.getDeleted())
                || !Boolean.TRUE.equals(member.getTeacher())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该教师暂不可接收留言");
        }
    }

    private Consultation getOwned(Long id) {
        Long teacherMemberId = currentTeacherMemberId();
        Consultation consultation = consultationMapper.selectById(id);
        if (consultation == null || !teacherMemberId.equals(consultation.getTeacherMemberId())) {
            // 统一按不存在处理，避免暴露他人留言是否存在
            throw new BusinessException(ResultCode.NOT_FOUND, "留言不存在");
        }
        return consultation;
    }

    private Long currentTeacherMemberId() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return memberId;
    }

    private static String buildNotificationContent(Consultation consultation) {
        StringBuilder content = new StringBuilder()
                .append("来源：").append(consultation.getBizName())
                .append('\n').append("留言：").append(consultation.getMessage());
        if (StringUtils.hasText(consultation.getWechat())) {
            content.append('\n').append("微信号：").append(consultation.getWechat());
        }
        content.append('\n').append("电话：").append(consultation.getPhone());
        return content.toString();
    }

    private ConsultationListVO toListVO(Consultation consultation) {
        return ConsultationListVO.builder()
                .id(consultation.getId())
                .bizType(consultation.getBizType().getValue())
                .bizTypeDesc(consultation.getBizType().getDesc())
                .bizId(consultation.getBizId())
                .bizName(consultation.getBizName())
                .message(consultation.getMessage())
                .wechat(consultation.getWechat())
                .phone(consultation.getPhone())
                .status(consultation.getStatus().getValue())
                .statusDesc(consultation.getStatus().getDesc())
                .createdAt(consultation.getCreatedAt())
                .build();
    }

    private ConsultationDetailVO toDetailVO(Consultation consultation) {
        return ConsultationDetailVO.builder()
                .id(consultation.getId())
                .bizType(consultation.getBizType().getValue())
                .bizTypeDesc(consultation.getBizType().getDesc())
                .bizId(consultation.getBizId())
                .bizName(consultation.getBizName())
                .senderMemberId(consultation.getSenderMemberId())
                .message(consultation.getMessage())
                .wechat(consultation.getWechat())
                .phone(consultation.getPhone())
                .clientIp(consultation.getClientIp())
                .status(consultation.getStatus().getValue())
                .statusDesc(consultation.getStatus().getDesc())
                .createdAt(consultation.getCreatedAt())
                .updatedAt(consultation.getUpdatedAt())
                .build();
    }

    private record BizTarget(Long teacherMemberId, String bizName) {
    }
}
