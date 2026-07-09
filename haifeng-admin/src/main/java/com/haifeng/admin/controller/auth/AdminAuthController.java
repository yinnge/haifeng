package com.haifeng.admin.controller.auth;

import com.haifeng.admin.dto.auth.TotpLoginDTO;
import com.haifeng.admin.service.auth.AdminAuthService;
import com.haifeng.admin.vo.auth.PreAuthVO;
import com.haifeng.common.annotation.RateLimit;
import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.CaptchaService;
import com.haifeng.common.vo.auth.CaptchaVO;
import com.haifeng.common.vo.auth.TokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员认证入口
 * 流程：获取验证码 → 登录（密码正确还需TOTP则返回PreAuthVO）→ TOTP二次校验 → 下发Token
 * 同时支持 Token 刷新和登出
 */
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public R<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = captchaService.generateCaptcha();
        return R.ok(captcha);
    }

    @RateLimit(10)
    @PostMapping("/login")
    public R<?> login(@Valid @RequestBody LoginDTO dto) {
        Object result = adminAuthService.login(dto);
        if (result instanceof PreAuthVO) {
            return R.fail(ResultCode.TOTP_REQUIRED, (PreAuthVO) result);
        }
        return R.ok((TokenVO) result);
    }

    @RateLimit(10)
    @PostMapping("/login/totp")
    public R<TokenVO> loginWithTotp(@Valid @RequestBody TotpLoginDTO dto) {
        TokenVO tokenVO = adminAuthService.loginWithTotp(dto.getPreAuthToken(), dto.getTotpCode());
        return R.ok(tokenVO);
    }

    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO tokenVO = adminAuthService.refresh(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        adminAuthService.logout();
        return R.ok();
    }
}
