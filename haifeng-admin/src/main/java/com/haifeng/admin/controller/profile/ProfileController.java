package com.haifeng.admin.controller.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.service.profile.ProfileService;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;
import com.haifeng.common.annotation.OperationLog;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.response.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心 - 管理员个人信息、修改密码、TOTP 两步验证设置
 */
@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
@RequireLogin(userType = "admin")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public R<ProfileVO> getProfile() {
        ProfileVO vo = profileService.getProfile();
        return R.ok(vo);
    }

    @PutMapping
    @OperationLog(module = "个人中心", action = "修改个人信息")
    public R<Void> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        profileService.updateProfile(dto);
        return R.ok();
    }

    @PutMapping("/password")
    @OperationLog(module = "个人中心", action = "修改密码")
    public R<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto) {
        profileService.updatePassword(dto);
        return R.ok();
    }

    @PostMapping("/totp/enable")
    @OperationLog(module = "个人中心", action = "开启 TOTP")
    public R<TotpEnableVO> enableTotp() {
        TotpEnableVO vo = profileService.enableTotp();
        return R.ok(vo);
    }

    @PostMapping("/totp/verify")
    @OperationLog(module = "个人中心", action = "验证 TOTP")
    public R<Void> verifyTotp(@Valid @RequestBody TotpVerifyDTO dto) {
        profileService.verifyTotp(dto);
        return R.ok();
    }

    @PostMapping("/totp/disable")
    @OperationLog(module = "个人中心", action = "关闭 TOTP")
    public R<Void> disableTotp(@Valid @RequestBody TotpDisableDTO dto) {
        profileService.disableTotp(dto);
        return R.ok();
    }

    @GetMapping("/totp/qrcode")
    public R<TotpEnableVO> getTotpQrCode() {
        TotpEnableVO vo = profileService.getTotpQrCode();
        return R.ok(vo);
    }
}
