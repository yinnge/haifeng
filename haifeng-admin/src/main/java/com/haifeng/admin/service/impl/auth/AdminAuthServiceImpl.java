package com.haifeng.admin.service.impl.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.haifeng.admin.service.auth.AdminAuthService;
import com.haifeng.admin.vo.auth.AdminTokenVO;
import com.haifeng.admin.vo.auth.PreAuthVO;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.CaptchaService;
import com.haifeng.common.service.TotpService;
import com.haifeng.common.util.DesensitizeUtil;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.vo.auth.TokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final long LOGIN_FAIL_EXPIRE_MINUTES = 30;
    private static final long PRE_AUTH_EXPIRE_MINUTES = 2;

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final CaptchaService captchaService;
    private final TotpService totpService;

    @Override
    public Object login(LoginDTO dto) {
        // 1. 验证码校验
        if (!captchaService.validateCaptcha(dto.getUuid(), dto.getCaptchaCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误或已过期");
        }

        // 2. 检查账号锁定（提前检查减少不必要的验证）
        String failKey = RedisKeyConstant.getAdminLoginFailKey(dto.getPhone());
        String failCountStr = redisTemplate.opsForValue().get(failKey);
        if (failCountStr != null && Integer.parseInt(failCountStr) >= MAX_LOGIN_FAIL_COUNT) {
            log.warn("管理员账号已锁定: {}", DesensitizeUtil.desensitizePhone(dto.getPhone()));
            throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
        }

        // 3. 查询管理员
        SysAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
        );

        if (admin == null) {
            log.warn("管理员登录失败，手机号不存在: {}", DesensitizeUtil.desensitizePhone(dto.getPhone()));
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (admin.getStatus() != 1) {
            log.warn("管理员账号已禁用: {}", DesensitizeUtil.desensitizePhone(dto.getPhone()));
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 4. 密码校验
        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            // 记录失败次数（原子自增 + 过期设置）
            Long failCount = redisTemplate.opsForValue().increment(failKey);
            if (failCount == 1) {
                redisTemplate.expire(failKey, LOGIN_FAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            if (failCount > MAX_LOGIN_FAIL_COUNT) {
                log.warn("管理员账号已锁定: {}", DesensitizeUtil.desensitizePhone(dto.getPhone()));
                throw new BusinessException(ResultCode.ACCOUNT_LOCKED);
            }
            log.warn("管理员登录失败，密码错误: {}，已失败 {} 次", DesensitizeUtil.desensitizePhone(dto.getPhone()), failCount);
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 5. 密码正确，清除失败计数
        redisTemplate.delete(failKey);

        // 6. 检查是否开启 TOTP
        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            // 生成预认证令牌
            String preAuthToken = java.util.UUID.randomUUID().toString();
            String preAuthKey = RedisKeyConstant.getAdminPreAuthKey(preAuthToken);
            redisTemplate.opsForValue().set(preAuthKey, admin.getId().toString(),
                    PRE_AUTH_EXPIRE_MINUTES, TimeUnit.MINUTES);

            log.info("管理员需要 TOTP 二次验证: {}", admin.getUsername());
            return PreAuthVO.builder().preAuthToken(preAuthToken).build();
        }

        // 7. 未开启 TOTP，直接发放 Token
        return issueToken(admin);
    }

    @Override
    public TokenVO loginWithTotp(String preAuthToken, String totpCode) {
        // 1. 验证预认证令牌
        String preAuthKey = RedisKeyConstant.getAdminPreAuthKey(preAuthToken);
        String adminIdStr = redisTemplate.opsForValue().get(preAuthKey);

        if (adminIdStr == null) {
            log.warn("预认证令牌无效或已过期");
            throw new BusinessException(ResultCode.UNAUTHORIZED, "验证已过期，请重新登录");
        }

        // 2. 查询管理员
        Long adminId = Long.parseLong(adminIdStr);
        SysAdmin admin = adminMapper.selectById(adminId);

        if (admin == null || admin.getDeleted() || admin.getStatus() != 1) {
            log.warn("管理员不存在或已禁用: {}", adminId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 3. 验证 TOTP 动态码
        if (!totpService.verifyCode(admin.getTotpSecret(), totpCode)) {
            log.warn("TOTP 验证码错误, adminId={}", adminId);
            throw new BusinessException(ResultCode.BAD_REQUEST, "动态验证码错误");
        }

        // 4. 删除预认证令牌（一次性使用）
        redisTemplate.delete(preAuthKey);

        log.info("管理员 TOTP 验证成功: {}", admin.getUsername());

        // 5. 发放 Token
        return issueToken(admin);
    }

    private AdminTokenVO issueToken(SysAdmin admin) {
        Integer tokenVersion = admin.getTokenVersion() != null ? admin.getTokenVersion() : 0;

        // 先更新 DB，再写 Redis（防止 Token 已签发但 lastLoginAt 未更新）
        adminMapper.update(
                Wrappers.lambdaUpdate(SysAdmin.class)
                        .eq(SysAdmin::getId, admin.getId())
                        .set(SysAdmin::getLastLoginAt, OffsetDateTime.now())
                        .set(SysAdmin::getUpdatedAt, OffsetDateTime.now())
        );

        String versionKey = RedisKeyConstant.getTokenVersionKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(versionKey, String.valueOf(tokenVersion),
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        String accessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null, tokenVersion);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        List<String> permissions = adminMapper.selectModuleCodesByAdminId(admin.getId());

        log.info("管理员登录成功: {}", admin.getUsername());

        return AdminTokenVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .permissions(permissions)
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
        if (!JwtUtil.USER_TYPE_ADMIN.equals(userType)) {
            log.warn("RefreshToken用户类型不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long adminId = jwtUtil.getUserIdFromToken(refreshToken);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(adminId, JwtUtil.USER_TYPE_ADMIN);
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("RefreshToken已失效或不匹配");
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getDeleted() || admin.getStatus() != 1) {
            log.warn("管理员不存在或已禁用: {}", adminId);
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 更新登录时间（绕过 @Version 乐观锁）
        adminMapper.update(
                Wrappers.lambdaUpdate(SysAdmin.class)
                        .eq(SysAdmin::getId, admin.getId())
                        .set(SysAdmin::getLastLoginAt, OffsetDateTime.now())
                        .set(SysAdmin::getUpdatedAt, OffsetDateTime.now())
        );

        Integer tokenVersion = admin.getTokenVersion() != null ? admin.getTokenVersion() : 0;

        String versionKey = RedisKeyConstant.getTokenVersionKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(versionKey, String.valueOf(tokenVersion),
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        String newAccessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null, tokenVersion);
        String newRefreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        List<String> permissions = adminMapper.selectModuleCodesByAdminId(admin.getId());

        log.info("管理员Token刷新成功: {}", admin.getUsername());

        return AdminTokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
                .permissions(permissions)
                .build();
    }

    @Override
    public void logout() {
        Long adminId = SecurityUtil.getCurrentAdminId();
        if (adminId == null) {
            return;
        }

        String redisKey = RedisKeyConstant.getRefreshTokenKey(adminId, JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.delete(redisKey);

        log.info("管理员登出成功: {}", adminId);
    }
}
