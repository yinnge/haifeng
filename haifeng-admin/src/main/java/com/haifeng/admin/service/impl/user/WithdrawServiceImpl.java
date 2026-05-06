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
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
            BeanUtils.copyProperties(withdraw, vo);
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(withdraw.getWechatId()));
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
            throw new BusinessException(404, "提现记录不存在");
        }
        return withdraw.getWechatId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void process(Long id, WithdrawProcessDTO dto) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(404, "提现记录不存在");
        }

        if (withdraw.getStatus() != WithdrawStatus.PENDING) {
            throw new BusinessException(400, "该提现记录已处理");
        }

        Member member = memberMapper.selectById(withdraw.getMemberId());
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        Long operatorId = SecurityUtil.getCurrentAdminId();
        String operatorName = SecurityUtil.getCurrentUser() != null ? SecurityUtil.getCurrentUser().getUsername() : null;

        OffsetDateTime now = OffsetDateTime.now();

        if ("paid".equals(dto.getAction())) {
            withdraw.setStatus(WithdrawStatus.PAID);
            withdraw.setOperatorId(operatorId);
            withdraw.setOperatorName(operatorName);
            withdraw.setRemark(dto.getRemark());
            withdraw.setUpdatedAt(now);
            withdrawRecordMapper.updateById(withdraw);

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
            withdraw.setStatus(WithdrawStatus.REJECTED);
            withdraw.setOperatorId(operatorId);
            withdraw.setOperatorName(operatorName);
            withdraw.setRemark(dto.getRemark());
            withdraw.setUpdatedAt(now);
            withdrawRecordMapper.updateById(withdraw);

            BigDecimal newBalance = member.getCommissionBalance().add(withdraw.getAmount());
            member.setCommissionBalance(newBalance);
            member.setUpdatedAt(now);
            memberMapper.updateById(member);

            String rejectReason = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : "审核未通过";
            notificationService.sendNotification(
                    member.getId(),
                    NotificationType.COMMISSION_PAID,
                    "提现被拒绝",
                    String.format("您申请提现的%.2f元佣金未通过审核，原因：%s。金额已退还至您的佣金余额。", withdraw.getAmount(), rejectReason),
                    withdraw.getId()
            );

            log.info("提现处理成功-已拒绝: withdrawId={}, amount={}, operatorId={}", id, withdraw.getAmount(), operatorId);
        }
    }

    @Override
    public void delete(Long id) {
        WithdrawRecord withdraw = withdrawRecordMapper.selectById(id);
        if (withdraw == null || withdraw.getDeleted()) {
            throw new BusinessException(404, "提现记录不存在");
        }

        withdraw.setDeleted(true);
        withdraw.setUpdatedAt(OffsetDateTime.now());
        withdrawRecordMapper.updateById(withdraw);

        log.info("删除提现记录成功: withdrawId={}", id);
    }
}
