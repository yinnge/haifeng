package com.haifeng.admin.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haifeng.admin.dto.user.MemberQueryDTO;
import com.haifeng.admin.dto.user.MemberStatusDTO;
import com.haifeng.admin.service.user.MemberService;
import com.haifeng.admin.vo.user.MemberDetailVO;
import com.haifeng.admin.vo.user.MemberListVO;
import com.haifeng.common.config.SecurityProperties;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.util.CryptoUtil;
import com.haifeng.common.util.DesensitizeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final SecurityProperties securityProperties;

    @Override
    public IPage<MemberListVO> page(MemberQueryDTO dto) {
        Page<Member> page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getDeleted, false);

        if (StringUtils.hasText(dto.getPhone())) {
            wrapper.like(Member::getPhone, dto.getPhone());
        }
        if (StringUtils.hasText(dto.getMemberType())) {
            wrapper.eq(Member::getMemberType, dto.getMemberType());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Member::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getWechatId())) {
            // 将微信号转换为盲索引进行等值查询
            String blindIndex = CryptoUtil.blindIndex(dto.getWechatId(), securityProperties.getHashSalt());
            wrapper.eq(Member::getWechatIdIndex, blindIndex);
        }

        wrapper.orderByDesc(Member::getCreatedAt);

        IPage<Member> memberPage = memberMapper.selectPage(page, wrapper);

        return memberPage.convert(member -> {
            MemberListVO vo = new MemberListVO();
            BeanUtils.copyProperties(member, vo);
            // 微信号脱敏
            vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
            return vo;
        });
    }

    @Override
    public MemberDetailVO detail(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        MemberDetailVO vo = new MemberDetailVO();
        BeanUtils.copyProperties(member, vo);
        // 微信号脱敏
        vo.setWechatId(DesensitizeUtil.desensitizeWechat(member.getWechatId()));
        return vo;
    }

    @Override
    public void updateStatus(Long id, MemberStatusDTO dto) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        member.setStatus(dto.getStatus());
        member.setUpdatedAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("修改用户状态成功: userId={}, status={}", id, dto.getStatus());
    }

    @Override
    public String getWechatPlaintext(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null || member.getDeleted()) {
            throw new BusinessException(404, "用户不存在");
        }

        // 微信号已通过 TypeHandler 自动解密
        return member.getWechatId();
    }
}
