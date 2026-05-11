package com.haifeng.app.controller.auth;

import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.app.service.auth.AppAuthService;
import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.response.R;
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

@RestController
@RequestMapping("/api/v1/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public R<CaptchaVO> getCaptcha() {
        CaptchaVO captcha = captchaService.generateCaptcha();
        return R.ok(captcha);
    }

    @PostMapping("/register")
    public R<TokenVO> register(@Valid @RequestBody RegisterDTO dto) {
        TokenVO tokenVO = appAuthService.register(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = appAuthService.login(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO tokenVO = appAuthService.refresh(dto);
        return R.ok(tokenVO);
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        appAuthService.logout();
        return R.ok();
    }
}
