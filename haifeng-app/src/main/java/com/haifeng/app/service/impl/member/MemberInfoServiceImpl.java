package com.haifeng.app.service.impl.member;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.member.AvatarUpdateDTO;
import com.haifeng.app.dto.member.MemberInfoUpdateDTO;
import com.haifeng.app.dto.member.PasswordUpdateDTO;
import com.haifeng.app.dto.member.WechatUpdateDTO;
import com.haifeng.app.service.member.MemberInfoService;
import com.haifeng.app.vo.member.MemberInfoVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.mapper.user.MemberOrderMapper;
import com.haifeng.common.mapper.user.ReferralCommissionMapper;
import com.haifeng.common.mapper.user.WithdrawRecordMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoServiceImpl implements MemberInfoService {

    private final MemberMapper memberMapper;
    private final MemberOrderMapper memberOrderMapper;
    private final ReferralCommissionMapper referralCommissionMapper;
    private final WithdrawRecordMapper withdrawRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    public MemberInfoVO getInfo() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // VIP过期后自动恢复挂起的Pro
        if (member.needsSuspendedRestore()) {
            restoreSuspendedPro(member);
        }

        // 根据有效会员类型返回到期时间
        String effectiveType = member.getEffectiveMemberType();
        OffsetDateTime effectiveExpireAt = member.getExpireAt();

        // VIP活跃时，记录挂起的Pro信息（供前端展示"待恢复专业版"）
        String suspendedMemberType = null;
        OffsetDateTime suspendedExpireAt = null;

        if ("pro".equals(effectiveType) && member.getSuspendedExpireAt() != null) {
            // 恢复的Pro：到期时间 = VIP到期日 + 挂起剩余月数
            effectiveExpireAt = member.getExpireAt() != null
                    ? member.getExpireAt().plusMonths(member.getSuspendedRemainingMonths())
                    : null;
        } else if ("vip".equals(effectiveType) && member.getSuspendedMemberType() != null) {
            // VIP活跃 + 有挂起的Pro，前端展示"待恢复"信息
            suspendedMemberType = member.getSuspendedMemberType();
            suspendedExpireAt = member.getExpireAt(); // VIP到期日 = Pro恢复日期
        }

        return MemberInfoVO.builder()
                .username(member.getUsername())
                .phone(member.getPhone())
                .avatar(member.getAvatar())
                .hasWechat(StringUtils.hasText(member.getWechatId()))
                .inviteCode(member.getInviteCode())
                .commissionBalance(member.getCommissionBalance())
                .commissionTotalEarned(member.getCommissionTotalEarned())
                .commissionTotalPaid(member.getCommissionTotalPaid())
                .memberType(effectiveType)
                .expireAt(effectiveExpireAt)
                .suspendedMemberType(suspendedMemberType)
                .suspendedExpireAt(suspendedExpireAt)
                .build();
    }

    /**
     * 恢复挂起的Pro会员
     */
    @Transactional
    public void restoreSuspendedPro(Member member) {
        String suspendedType = member.getSuspendedMemberType();
        Integer remainingMonths = member.getSuspendedRemainingMonths();

        // 计算新的Pro到期时间 = VIP到期日 + 挂起剩余月数
        OffsetDateTime newProExpireAt = member.getExpireAt() != null
                ? member.getExpireAt().plusMonths(remainingMonths)
                : OffsetDateTime.now().plusMonths(remainingMonths);

        member.setMemberType(suspendedType);
        member.setExpireAt(newProExpireAt);
        member.setSuspendedMemberType(null);
        member.setSuspendedExpireAt(null);
        member.setSuspendedRemainingMonths(null);
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("VIP过期，恢复挂起会员: memberId={}, restoredType={}, newExpireAt={}",
                member.getId(), suspendedType, newProExpireAt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(MemberInfoUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        boolean usernameChanged = false;
        boolean phoneChanged = false;

        // 校验用户名唯一性
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(member.getUsername())) {
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getUsername, dto.getUsername())
                            .eq(Member::getDeleted, false)
                            .ne(Member::getId, memberId));
            if (count > 0) {
                throw new BusinessException(400, "用户名已存在");
            }
            member.setUsername(dto.getUsername());
            usernameChanged = true;
        }

        // 校验手机号唯一性
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(member.getPhone())) {
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getPhone, dto.getPhone())
                            .eq(Member::getDeleted, false)
                            .ne(Member::getId, memberId));
            if (count > 0) {
                throw new BusinessException(400, "手机号已存在");
            }
            member.setPhone(dto.getPhone());
            phoneChanged = true;
        }

        if (dto.getAvatar() != null) {
            member.setAvatar(dto.getAvatar());
        }

        int rows = memberMapper.updateById(member);
        if (rows == 0) {
            throw new BusinessException(409, "数据已变更，请刷新后重试");
        }

        if (usernameChanged || phoneChanged) {
            syncMemberInfo(memberId, member);
        }

        log.info("更新用户信息成功: memberId={}", memberId);
    }

    @Override
    public String getWechat() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        return member.getWechatId();
    }

    @Override
    @Transactional
    public void updateWechat(WechatUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setWechatId(dto.getWechatId());
        String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
        member.setWechatIdIndex(blindIndex);
        memberMapper.updateById(member);

        syncMemberInfo(memberId, member);

        log.info("更新微信号成功: memberId={}", memberId);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), member.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        member.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        memberMapper.updateById(member);

        log.info("修改密码成功: memberId={}", memberId);
    }

    @Override
    @Transactional
    public void updateAvatar(AvatarUpdateDTO dto) {
        String avatar = dto.getAvatar();
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setAvatar(avatar);
        memberMapper.updateById(member);

        log.info("更新头像成功: memberId={}", memberId);
    }

    private void syncMemberInfo(Long memberId, Member member) {
        OffsetDateTime now = OffsetDateTime.now();
        memberOrderMapper.updateMemberInfo(memberId, member.getUsername(), member.getPhone(),
                member.getWechatId(), member.getWechatIdIndex(), now);
        referralCommissionMapper.updateReferrerInfo(memberId, member.getUsername(), member.getPhone(), now);
        referralCommissionMapper.updateRefereeInfo(memberId, member.getUsername(), member.getPhone(), now);
        withdrawRecordMapper.updateMemberInfo(memberId, member.getUsername(), member.getPhone(),
                member.getWechatId(), member.getWechatIdIndex(), now);
        memberMapper.updateReferrerUsername(memberId, member.getUsername(), now);
        log.info("同步用户信息到关联表成功: memberId={}", memberId);
    }
}
