package com.haifeng.admin.service.impl.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.service.profile.ProfileService;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;
import com.haifeng.common.entity.permission.SysAdmin;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.permission.SysAdminMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.TotpService;
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
public class ProfileServiceImpl implements ProfileService {

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final TotpService totpService;

    @Override
    public ProfileVO getProfile() {
        SysAdmin admin = getCurrentAdmin();
        return ProfileVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .realName(admin.getRealName())
                .phone(admin.getPhone())
                .email(admin.getEmail())
                .avatar(admin.getAvatar())
                .roleName(admin.getRoleName())
                .isTotpEnabled(Boolean.TRUE.equals(admin.getTotpEnabled()))
                .lastLoginAt(admin.getLastLoginAt())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(ProfileUpdateDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (StringUtils.hasText(dto.getUsername())) {
            admin.setUsername(dto.getUsername());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            admin.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            admin.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            admin.setAvatar(dto.getAvatar());
        }

        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员更新个人信息: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (!passwordEncoder.matches(dto.getOldPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }

        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员修改密码成功: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TotpEnableVO enableTotp() {
        SysAdmin admin = getCurrentAdmin();

        if (Boolean.TRUE.equals(admin.getTotpEnabled())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "TOTP 已开启");
        }

        // 生成密钥
        String secret = totpService.generateSecret();
        admin.setTotpSecret(secret);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        // 生成二维码
        String qrCode = totpService.generateQrCodeBase64(secret, admin.getUsername());

        log.info("管理员生成 TOTP 密钥: {}", admin.getUsername());

        return TotpEnableVO.builder()
                .secret(secret)
                .qrCodeImage(qrCode)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyTotp(TotpVerifyDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (admin.getTotpSecret() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请先开启 TOTP");
        }

        if (!totpService.verifyCode(admin.getTotpSecret(), dto.getCode())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验证码错误");
        }

        admin.setTotpEnabled(true);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员绑定 TOTP 成功: {}", admin.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableTotp(TotpDisableDTO dto) {
        SysAdmin admin = getCurrentAdmin();

        if (!passwordEncoder.matches(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        admin.setTotpEnabled(false);
        admin.setTotpSecret(null);
        admin.setUpdatedAt(OffsetDateTime.now());
        adminMapper.updateById(admin);

        log.info("管理员关闭 TOTP: {}", admin.getUsername());
    }

    @Override
    public TotpEnableVO getTotpQrCode() {
        SysAdmin admin = getCurrentAdmin();

        if (!Boolean.TRUE.equals(admin.getTotpEnabled()) || admin.getTotpSecret() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "TOTP 未开启");
        }

        String qrCode = totpService.generateQrCodeBase64(admin.getTotpSecret(), admin.getUsername());

        return TotpEnableVO.builder()
                .secret(admin.getTotpSecret())
                .qrCodeImage(qrCode)
                .build();
    }

    private SysAdmin getCurrentAdmin() {
        Long adminId = SecurityUtil.getCurrentAdminId();
        if (adminId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getDeleted()) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        return admin;
    }
}
