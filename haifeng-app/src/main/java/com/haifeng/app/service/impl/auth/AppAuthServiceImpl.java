package com.haifeng.app.service.impl.auth;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.dto.auth.ForgotPasswordResetDTO;
import com.haifeng.app.dto.auth.ForgotPasswordSendCodeDTO;
import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.app.service.auth.AppAuthService;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.entity.user.Member;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.user.MemberMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.CaptchaService;
import com.haifeng.common.service.SmsService;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.InviteCodeGenerator;
import com.haifeng.common.util.IpUtil;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.util.SnowflakeIdGenerator;
import com.haifeng.common.vo.auth.TokenVO;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private static final String STATUS_ACTIVE = "active";

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final CaptchaService captchaService;
    private final SmsService smsService;

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
        existCount = memberMapper.selectCount(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, dto.getPhone())
                        .eq(Member::getDeleted, false)
        );
        if (existCount > 0) {
            log.warn("注册失败，手机号已存在: {}", dto.getPhone());
            throw new BusinessException(400, "手机号已存在");
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

        // 创建会员
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

        if (referrer != null) {
            member.setReferrerId(referrer.getId());
            member.setReferrerUsername(referrer.getUsername());
        }

        // 生成邀请码（基于雪花ID，绝对唯一）
        member.setInviteCode(InviteCodeGenerator.generate(member.getId()));

        memberMapper.insert(member);

        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, member.getMemberType());
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

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
        // 1. 先验证验证码
        if (!captchaService.validateCaptcha(dto.getUuid(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, dto.getPhone())
                        .eq(Member::getDeleted, false)
        );

        if (member == null) {
            log.warn("用户登录失败，手机号不存在: {}", dto.getPhone());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!member.isActive()) {
            log.warn("用户账号已禁用: {}", dto.getPhone());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            log.warn("用户登录失败，密码错误: {}", dto.getPhone());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String effectiveMemberType = member.getEffectiveMemberType();

        String accessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String refreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        member.setLastLoginAt(OffsetDateTime.now());
        member.setLastLoginIp(IpUtil.getClientIp());
        memberMapper.updateById(member);

        log.info("用户登录成功: {}", member.getUsername());

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

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("RefreshToken无效");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        String userType = jwtUtil.getUserTypeFromToken(refreshToken);
        if (!JwtUtil.USER_TYPE_MEMBER.equals(userType)) {
            log.warn("RefreshToken用户类型不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long memberId = jwtUtil.getUserIdFromToken(refreshToken);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("RefreshToken已失效或不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null || member.getDeleted() || !member.isActive()) {
            log.warn("用户不存在或已禁用: {}", memberId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String effectiveMemberType = member.getEffectiveMemberType();

        String newAccessToken = jwtUtil.generateAccessToken(member.getId(), JwtUtil.USER_TYPE_MEMBER, effectiveMemberType);
        String newRefreshToken = jwtUtil.generateRefreshToken(member.getId(), JwtUtil.USER_TYPE_MEMBER);

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

        String redisKey = RedisKeyConstant.getRefreshTokenKey(memberId, JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.delete(redisKey);

        log.info("用户登出成功: {}", memberId);
    }

    @Override
    public void forgotPasswordSendCode(ForgotPasswordSendCodeDTO dto) {
        if (!captchaService.validateCaptcha(dto.getUuid(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        String phone = dto.getPhone();

        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, phone)
                        .eq(Member::getDeleted, false)
        );
        if (member == null) {
            log.info("发送短信验证码-手机号未注册（假装成功），phone={}",
                    DesensitizeUtil.desensitizePhone(phone));
            return;
        }

        String coolKey = RedisKeyConstant.SMS_SEND_COOL + phone;
        Boolean isCooled = redisTemplate.opsForValue().setIfAbsent(coolKey, "1", 60, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(isCooled)) {
            log.warn("短信发送被限流，phone={}, 原因=冷却中",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "发送过于频繁，请60秒后重试");
        }

        String dateStr = java.time.LocalDate.now().toString().replace("-", "");
        String limitKey = RedisKeyConstant.SMS_SEND_LIMIT + dateStr + ":" + phone;
        Long sendCount = redisTemplate.opsForValue().increment(limitKey);
        if (sendCount != null && sendCount == 1) {
            long secondsUntilEndOfDay = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    java.time.LocalDate.now().plusDays(1).atStartOfDay()
            ).getSeconds();
            redisTemplate.expire(limitKey, secondsUntilEndOfDay, TimeUnit.SECONDS);
        }
        if (sendCount != null && sendCount > 5) {
            log.warn("短信发送被限流，phone={}, 原因=日限达上限",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "今日短信发送次数已达上限");
        }

        String clientIp = IpUtil.getClientIp();
        String ipLimitKey = RedisKeyConstant.getLimitApiKey(clientIp,
                "/api/v1/app/auth/forgot-password/send-code");
        Long ipCount = redisTemplate.opsForValue().increment(ipLimitKey);
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipLimitKey, 60, TimeUnit.SECONDS);
        }
        if (ipCount != null && ipCount > 10) {
            log.warn("短信发送被限流，IP={}, 原因=IP请求过于频繁", clientIp);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后重试");
        }

        String code = RandomUtil.randomNumbers(6);
        try {
            smsService.sendSmsCode(phone, code);
        } catch (BusinessException e) {
            redisTemplate.delete(coolKey);
            throw e;
        }

        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        redisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
    }

    @Override
    public void forgotPasswordReset(ForgotPasswordResetDTO dto) {
        String phone = dto.getPhone();

        Member member = memberMapper.selectOne(
                new LambdaQueryWrapper<Member>()
                        .eq(Member::getPhone, phone)
                        .eq(Member::getDeleted, false)
        );
        if (member == null) {
            log.warn("密码重置失败，手机号不存在: {}", DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String codeKey = RedisKeyConstant.SMS_CODE + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);
        if (storedCode == null) {
            log.warn("密码重置失败，验证码已过期，phone={}",
                    DesensitizeUtil.desensitizePhone(phone));
            throw new BusinessException(ResultCode.SMS_CODE_EXPIRED);
        }

        if (!storedCode.equals(dto.getCode())) {
            String failKey = RedisKeyConstant.SMS_VERIFY_FAIL + phone;
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1) {
                redisTemplate.expire(failKey, 30, TimeUnit.MINUTES);
            }

            log.warn("短信验证码校验失败，phone={}, 已失败次数={}",
                    DesensitizeUtil.desensitizePhone(phone), failCount);

            if (failCount != null && failCount >= 5) {
                redisTemplate.expire(failKey, 30, TimeUnit.MINUTES);
                redisTemplate.delete(codeKey);
                log.warn("短信验证码锁定，phone={}", DesensitizeUtil.desensitizePhone(phone));
                throw new BusinessException(ResultCode.SMS_CODE_LOCKED);
            }

            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }

        redisTemplate.delete(codeKey);
        redisTemplate.delete(RedisKeyConstant.SMS_VERIFY_FAIL + phone);

        if (passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "新密码不能与原密码相同");
        }

        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setUpdatedAt(java.time.OffsetDateTime.now());
        memberMapper.updateById(member);

        String refreshKey = RedisKeyConstant.getRefreshTokenKey(member.getId(), JwtUtil.USER_TYPE_MEMBER);
        redisTemplate.delete(refreshKey);

        log.info("密码重置成功，memberId={}, phone={}",
                member.getId(), DesensitizeUtil.desensitizePhone(phone));
    }
}
