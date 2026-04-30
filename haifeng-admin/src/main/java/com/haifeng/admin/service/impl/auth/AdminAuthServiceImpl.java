package com.haifeng.admin.service.impl.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.admin.service.auth.AdminAuthService;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.JwtUtil;
import com.haifeng.common.util.SecurityUtil;
import com.haifeng.common.vo.TokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public TokenVO login(LoginDTO dto) {
        SysAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getPhone, dto.getPhone())
                        .eq(SysAdmin::getDeleted, false)
        );

        if (admin == null) {
            log.warn("管理员登录失败，手机号不存在: {}", dto.getPhone());
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (admin.getStatus() != 1) {
            log.warn("管理员账号已禁用: {}", dto.getPhone());
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            log.warn("管理员登录失败，密码错误: {}", dto.getPhone());
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String accessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null);
        String refreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        String redisKey = RedisKeyConstant.getRefreshTokenKey(admin.getId(), JwtUtil.USER_TYPE_ADMIN);
        redisTemplate.opsForValue().set(redisKey, refreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        admin.setLastLoginAt(OffsetDateTime.now());
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员登录成功: {}", admin.getUsername());

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

        String newAccessToken = jwtUtil.generateAccessToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN, null);
        String newRefreshToken = jwtUtil.generateRefreshToken(admin.getId(), JwtUtil.USER_TYPE_ADMIN);

        redisTemplate.opsForValue().set(redisKey, newRefreshToken,
                jwtUtil.getRefreshTokenExpire(), TimeUnit.SECONDS);

        log.info("管理员Token刷新成功: {}", admin.getUsername());

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpire(jwtUtil.getAccessTokenExpire())
                .refreshTokenExpire(jwtUtil.getRefreshTokenExpire())
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
