package com.haifeng.admin.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.home.ConsultationQueryDTO;
import com.haifeng.admin.service.home.ConsultationService;
import com.haifeng.admin.vo.home.ConsultationDetailVO;
import com.haifeng.admin.vo.home.ConsultationListVO;
import com.haifeng.common.entity.consultation.Consultation;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.enums.ConsultationBizType;
import com.haifeng.common.enums.ConsultationStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.consultation.ConsultationMapper;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Admin 端咨询留言（只读，不提供状态修改）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationMapper consultationMapper;
    private final MemberMapper memberMapper;

    @Override
    public IPage<ConsultationListVO> page(ConsultationQueryDTO dto) {
        Page<Consultation> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<Consultation>()
                .orderByDesc(Consultation::getCreatedAt)
                .orderByDesc(Consultation::getId);

        if (dto.getTeacherMemberId() != null) {
            wrapper.eq(Consultation::getTeacherMemberId, dto.getTeacherMemberId());
        }
        if (dto.getBizId() != null) {
            wrapper.eq(Consultation::getBizId, dto.getBizId());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.eq(Consultation::getPhone, dto.getPhone().trim());
        }
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

        IPage<Consultation> entityPage = consultationMapper.selectPage(page, wrapper);
        Map<Long, String> usernameMap = loadTeacherUsernames(entityPage.getRecords());

        return entityPage.convert(consultation -> toListVO(consultation, usernameMap));
    }

    @Override
    public ConsultationDetailVO detail(Long id) {
        Consultation consultation = consultationMapper.selectById(id);
        if (consultation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "留言不存在");
        }
        String teacherUsername = null;
        if (consultation.getTeacherMemberId() != null) {
            Member member = memberMapper.selectById(consultation.getTeacherMemberId());
            teacherUsername = member == null ? null : member.getUsername();
        }
        return toDetailVO(consultation, teacherUsername);
    }

    /**
     * 批量补教师用户名，避免逐行查询。
     */
    private Map<Long, String> loadTeacherUsernames(List<Consultation> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> memberIds = records.stream()
                .map(Consultation::getTeacherMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 用 HashMap 手工填充：用户名可能为 null，Collectors.toMap 会直接抛 NPE
        Map<Long, String> usernameMap = new java.util.HashMap<>();
        for (Member member : memberMapper.selectBatchIds(memberIds)) {
            if (member.getId() != null && member.getUsername() != null) {
                usernameMap.put(member.getId(), member.getUsername());
            }
        }
        return usernameMap;
    }

    private ConsultationListVO toListVO(Consultation consultation, Map<Long, String> usernameMap) {
        return ConsultationListVO.builder()
                .id(consultation.getId())
                .bizType(consultation.getBizType().getValue())
                .bizTypeDesc(consultation.getBizType().getDesc())
                .bizId(consultation.getBizId())
                .bizName(consultation.getBizName())
                .teacherMemberId(consultation.getTeacherMemberId())
                .teacherUsername(usernameMap.get(consultation.getTeacherMemberId()))
                .senderMemberId(consultation.getSenderMemberId())
                .message(consultation.getMessage())
                .wechat(consultation.getWechat())
                .phone(consultation.getPhone())
                .status(consultation.getStatus().getValue())
                .statusDesc(consultation.getStatus().getDesc())
                .createdAt(consultation.getCreatedAt())
                .build();
    }

    private ConsultationDetailVO toDetailVO(Consultation consultation, String teacherUsername) {
        return ConsultationDetailVO.builder()
                .id(consultation.getId())
                .bizType(consultation.getBizType().getValue())
                .bizTypeDesc(consultation.getBizType().getDesc())
                .bizId(consultation.getBizId())
                .bizName(consultation.getBizName())
                .teacherMemberId(consultation.getTeacherMemberId())
                .teacherUsername(teacherUsername)
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
}
