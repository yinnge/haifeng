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
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoServiceImpl implements MemberInfoService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;

    @Override
    public MemberInfoVO getInfo() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
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
                .memberType(member.getEffectiveMemberType())
                .expireAt(member.getExpireAt())
                .build();
    }

    @Override
    @Transactional
    public void updateInfo(MemberInfoUpdateDTO dto) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);

        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

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
        }

        if (dto.getAvatar() != null) {
            member.setAvatar(dto.getAvatar());
        }

        memberMapper.updateById(member);

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
}
