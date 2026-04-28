package com.haifeng.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.RegisterDTO;
import com.haifeng.app.entity.Member;
import com.haifeng.app.mapper.MemberMapper;
import com.haifeng.app.service.AppAuthService;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.haifeng.common.vo.TokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户端认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_DISABLED = "disabled";

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long existCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUsername, dto.getUsername())
                        .eq(Member::getDeleted, false)
        );
        if (existCount > 0) {
            log.warn("注册失败，用户名已存在: {}", dto.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(dto.getPhone())) {
            existCount = memberMapper.selectCount(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getPhone, dto.getPhone())
                            .eq(Member::getDeleted, false)
            );
            if (existCount > 0) {
                log.warn("注册失败，手机号已存在: {}", dto.getPhone());
                throw new BusinessException(400, "手机号已存在");
            }
        }

        // 查询推荐人
        Member referrer = null;
        if (StringUtils.hasText(dto.getReferrerCode())) {
            referrer = memberMapper.selectOne(
                    new LambdaQueryWrapper<Member>()
                            .eq(Member::getInviteCode, dto.getReferrerCode())
                            .eq(Member::getDeleted, false)
            );
            if (referrer == null) {
                log.warn("注册失败，邀请码无效: {}", dto.getReferrerCode());
                throw new BusinessException(400, "邀请码无效");
            }
        }

        // 创建会员（邀请码由数据库触发器自动生成）
        OffsetDateTime now = OffsetDateTime.now();
        Member member = Member.builder()
                .id(SnowflakeIdGenerator.nextId())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .memberType(JwtUtil.MEMBER_TYPE_NORMAL)
                .status(STATUS_ACTIVE)
                .commissionBalance(BigDecimal.ZERO)
                .commissionTotalEarned(BigDecimal.ZERO)
                .commissionTotalPaid(BigDecimal.ZERO)
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 设置推荐人信息
        if (referrer != null) {
            member.setReferrerId(referrer.getId());
            member.setReferrerUsername(referrer.getUsername());
        }

        memberMapper.insert(member);

        // 重新查询以获取数据库生成的邀请码
        member = memberMapper.selectById(member.getId());

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, member.getMemberType());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        // 保存RefreshToken到Redis
        String redisKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("用户注册成功: {}, 邀请码: {}", dto.getUsername(), member.getInviteCode());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public TokenVO login(LoginDTO dto) {
        // 查询会员
        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getUsername, dto.getUsername())
                        .eq(Member::getDeleted, false)
        );

        if (member == null) {
            log.warn("用户登录失败，用户不存在: {}", dto.getUsername());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 检查状态
        if (!member.isActive()) {
            log.warn("用户账号已禁用: {}", dto.getUsername());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            log.warn("用户登录失败，密码错误: {}", dto.getUsername());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 获取实际的会员类型（考虑VIP过期）
        String effectiveMemberType = member.getEffectiveMemberType();

        // 生成Token
        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        // 保存RefreshToken到Redis
        String redisKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        // 更新最后登录时间
        member.setLastLoginAt(OffsetDateTime.now());
        memberMapper.updateById(member);

        log.info("用户登录成功: {}", dto.getUsername());

        return TokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public TokenVO refresh(RefreshTokenDTO dto) {
        String refreshToken = dto.getRefreshToken();

        // 验证RefreshToken
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("RefreshToken无效");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 验证用户类型
        String userType = jwtUtil.getUserTypeFromToken(refreshToken);
        if (!JwtUtil.USER_TYPE_MEMBER.equals(userType)) {
            log.warn("RefreshToken用户类型不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long memberId = jwtUtil.getUserIdFromToken(refreshToken);

        // 验证Redis中的RefreshToken
        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("RefreshToken已失效或不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 查询会员信息
        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getDeleted() || !member.isActive()) {
            log.warn("用户不存在或已禁用: {}", memberId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 获取实际的会员类型
        String effectiveMemberType = member.getEffectiveMemberType();

        // 生成新Token
        String newAccessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        // 更新Redis中的RefreshToken
        redisTemplate.opsForValue().set(redisKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("用户Token刷新成功: {}", member.getUsername());

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .build();
    }

    @Override
    public void logout() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        if (memberId == null) {
            return;
        }

        // 删除Redis中的RefreshToken
        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.delete(redisKey);

        log.info("用户登出成功: {}", memberId);
    }
}
