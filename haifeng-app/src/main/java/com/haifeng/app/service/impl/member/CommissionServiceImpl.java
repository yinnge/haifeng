package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.ReferrerBindDTO;
import com.haifeng.app.dto.member.WithdrawDTO;
import com.haifeng.app.service.member.CommissionService;
import com.haifeng.app.vo.member.CommissionVO;
import com.haifeng.app.vo.member.ReferrerPreviewVO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.entity.user.WithdrawRecord;
import com.haifeng.common.enums.WithdrawStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.InviteCodeGenerator;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private static final List<BigDecimal> ALLOWED_AMOUNTS = Arrays.asList(
            new BigDecimal("50.00"),
            new BigDecimal("100.00")
    );

    private final MemberMapper memberMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;

    @Override
    public CommissionVO getCommission() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        Long referralCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getReferrerId, memberId)
                        .eq(Member::getDeleted, false));

        String referrerInviteCode = null;
        if (member.getReferrerId() != null) {
            Member referrer = memberMapper.selectById(member.getReferrerId());
            if (referrer != null) {
                referrerInviteCode = referrer.getInviteCode();
            }
        }

        return CommissionVO.builder()
                .inviteCode(member.getInviteCode())
                .commissionBalance(member.getCommissionBalance())
                .commissionTotalEarned(member.getCommissionTotalEarned())
                .commissionTotalPaid(member.getCommissionTotalPaid())
                .referralCount(referralCount.intValue())
                .referrerInviteCode(referrerInviteCode)
                .build();
    }

    @Override
    @Transactional
    public Long withdraw(WithdrawDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        BigDecimal amount = dto.getAmount();
        if (!ALLOWED_AMOUNTS.contains(amount)) {
            throw new BusinessException(400, "提现金额只能是50或100");
        }

        if (member.getCommissionBalance().compareTo(amount) < 0) {
            throw new BusinessException(400, "余额不足");
        }

        member.setCommissionBalance(member.getCommissionBalance().subtract(amount));
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        Long recordId = SnowflakeIdGenerator.nextId();
        WithdrawRecord record = WithdrawRecord.builder()
                .id(recordId)
                .memberId(memberId)
                .memberName(member.getUsername())
                .phone(member.getPhone())
                .wechatId(member.getWechatId())
                .wechatIdIndex(member.getWechatIdIndex())
                .amount(amount)
                .status(WithdrawStatus.PENDING)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        withdrawRecordMapper.insert(record);

        log.info("提现申请成功: memberId={}, amount={}, recordId={}", memberId, amount, recordId);

        return recordId;
    }

    @Override
    public ReferrerPreviewVO previewReferrer(String inviteCode) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        long referrerId = InviteCodeGenerator.decode(inviteCode);
        if (referrerId == -1) {
            throw new BusinessException(400, "邀请码无效");
        }

        if (referrerId == memberId) {
            throw new BusinessException(400, "不能填写自己的邀请码");
        }

        Member referrer = memberMapper.selectById(referrerId);
        if (referrer == null || referrer.getDeleted()) {
            throw new BusinessException(400, "邀请码对应的用户不存在");
        }

        if (!referrer.isActive()) {
            throw new BusinessException(400, "邀请码对应的用户已被禁用");
        }

        if (referrer.getReferrerId() != null && referrer.getReferrerId().equals(memberId)) {
            throw new BusinessException(400, "不能互相绑定邀请码");
        }

        return ReferrerPreviewVO.builder()
                .username(referrer.getUsername())
                .phone(DesensitizeUtil.desensitizePhone(referrer.getPhone()))
                .build();
    }

    @Override
    @Transactional
    public void bindReferrer(ReferrerBindDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        if (member.getReferrerId() != null) {
            throw new BusinessException(400, "已绑定邀请码，不可修改");
        }

        long referrerId = InviteCodeGenerator.decode(dto.getInviteCode());
        if (referrerId == -1) {
            throw new BusinessException(400, "邀请码无效");
        }

        if (referrerId == memberId) {
            throw new BusinessException(400, "不能填写自己的邀请码");
        }

        Member referrer = memberMapper.selectById(referrerId);
        if (referrer == null || referrer.getDeleted()) {
            throw new BusinessException(400, "邀请码对应的用户不存在");
        }

        if (!referrer.isActive()) {
            throw new BusinessException(400, "邀请码对应的用户已被禁用");
        }

        if (referrer.getReferrerId() != null && referrer.getReferrerId().equals(memberId)) {
            throw new BusinessException(400, "不能互相绑定邀请码");
        }

        member.setReferrerId(referrerId);
        member.setReferrerUsername(referrer.getUsername());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("邀请码绑定成功: memberId={}, referrerId={}", memberId, referrerId);
    }
}
