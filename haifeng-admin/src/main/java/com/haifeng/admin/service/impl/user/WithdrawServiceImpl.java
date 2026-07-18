package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.WithdrawProcessDTO;
import com.haifeng.admin.dto.user.WithdrawQueryDTO;
import com.haifeng.admin.service.user.NotificationService;
import com.haifeng.admin.service.user.WithdrawService;
import com.haifeng.admin.vo.user.WithdrawListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.enums.NotificationType;
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawServiceImpl implements WithdrawService {

    private final WithdrawRecordMapper withdrawRecordMapper;
    private final MemberMapper memberMapper;
    private final NotificationService notificationService;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<WithdrawListVO> page(WithdrawQueryDTO dto) {
        Page<WithdrawRecord> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<WithdrawRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WithdrawRecord::getDeleted, false);

        if (StringUtils.hasText(dto.getMemberName())) {
            wrapper.like(WithdrawRecord::getMemberName, dto.getMemberName());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(WithdrawRecord::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(WithdrawRecord::getWechatIdIndex, blindIndex);
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(WithdrawRecord::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(WithdrawRecord::getCreatedAt);

        IPage<WithdrawRecord> withdrawPage = withdrawRecordMapper.selectPage(page, wrapper);

        return withdrawPage.convert(withdraw -> {
            WithdrawListVO vo = new WithdrawListVO();
            vo.setId(withdraw.getId());
            vo.setMemberId(withdraw.getMemberId());
            vo.setMemberName(withdraw.getMemberName());
            vo.setPhone(withdraw.getPhone());
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(withdraw.getWechatId()));
            vo.setAmount(withdraw.getAmount());
            vo.setOperatorName(withdraw.getOperatorName());
            vo.setRemark(withdraw.getRemark());
            vo.setCreatedAt(withdraw.getCreatedAt());
            vo.setUpdatedAt(withdraw.getUpdatedAt());
            if (withdraw.getStatus() != null) {
                vo.setStatus(withdraw.getStatus().getValue());
            }
            return vo;
        });
    }

    @Override
    public String getWechatPlaintext(Long id) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现记录不存在");
        }
        return withdraw.getWechatId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long id, WithdrawProcessDTO dto) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现记录不存在");
        }

        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该提现记录已处理");
        }

        Member member = memberMapper.selectById(withdraw.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        Long operatorId = SecurityUtil.getCurrentAdminId();
        String operatorName = SecurityUtil.getCurrentUser() != null ? SecurityUtil.getCurrentUser().getUsername() : null;

        OffsetDateTime now = OffsetDateTime.now();

        if ("paid".equals(dto.getAction())) {
            int affected = withdrawRecordMapper.updateStatusCas(
                    id, WithdrawStatus.PENDING.getValue(), WithdrawStatus.PAID.getValue(),
                    operatorId, operatorName, dto.getRemark(), now);
            if (affected == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "处理失败，该提现记录状态已变更，请刷新后重试");
            }

            BigDecimal newTotalPaid = member.getCommissionTotalPaid().add(withdraw.getAmount());
            member.setCommissionTotalPaid(newTotalPaid);
            member.setUpdatedAt(now);
            memberMapper.updateById(member);

            notificationService.sendNotification(
                    member.getId(),
                    NotificationType.COMMISSION_PAID,
                    "佣金已发放",
                    String.format("您申请提现的%.2f元佣金已发放到您的微信账户，请注意查收。", withdraw.getAmount()),
                    withdraw.getId()
            );

            log.info("提现处理成功-已打款: withdrawId={}, amount={}, operatorId={}", id, withdraw.getAmount(), operatorId);

        } else if ("rejected".equals(dto.getAction())) {
            int affected = withdrawRecordMapper.updateStatusCas(
                    id, WithdrawStatus.PENDING.getValue(), WithdrawStatus.REJECTED.getValue(),
                    operatorId, operatorName, dto.getRemark(), now);
            if (affected == 0) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "处理失败，该提现记录状态已变更，请刷新后重试");
            }

            BigDecimal newBalance = member.getCommissionBalance().add(withdraw.getAmount());
            member.setCommissionBalance(newBalance);
            member.setUpdatedAt(now);
            memberMapper.updateById(member);

            String rejectReason = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "审核未通过";
            notificationService.sendNotification(
                    member.getId(),
                    NotificationType.COMMISSION_REJECTED,
                    "提现被拒绝",
                    String.format("您申请提现的%.2f元佣金未通过审核，原因：%s。金额已退还至您的佣金余额。", withdraw.getAmount(), rejectReason),
                    withdraw.getId()
            );

            log.info("提现处理成功-已拒绝: withdrawId={}, amount={}, operatorId={}", id, withdraw.getAmount(), operatorId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现记录不存在");
        }

        withdraw.setDeleted(true);
        withdraw.setUpdatedAt(OffsetDateTime.now());
        withdrawRecordMapper.updateById(withdraw);

        log.info("删除提现记录成功: withdrawId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDelete(Long id) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectByIdIgnoreDeleted(id);
        if (withdraw == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现记录不存在");
        }

        withdrawRecordMapper.hardDeleteById(id);
        log.info("硬删除提现记录成功: withdrawId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restore(Long id) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectByIdIgnoreDeleted(id);
        if (withdraw == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "提现记录不存在");
        }

        if (!withdraw.getDeleted()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该提现记录未被禁用，无需恢复");
        }

        withdrawRecordMapper.restoreById(id, OffsetDateTime.now());
        log.info("恢复提现记录成功: withdrawId={}", id);
    }
}
