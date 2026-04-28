package com.haifeng.app.controller;

import com.haifeng.app.dto.RegisterDTO;
import com.haifeng.app.service.AppAuthService;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.response.R;
import com.haifeng.common.vo.TokenVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证控制器
 */
@RestController
@RequestMapping("/api/v1/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final AppAuthService appAuthService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<TokenVO> register(@Valid @RequestBody RegisterDTO dto) {
        TokenVO tokenVO = appAuthService.register(dto);
        return R.ok(tokenVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = appAuthService.login(dto);
        return R.ok(tokenVO);
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public R<TokenVO> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        TokenVO tokenVO = appAuthService.refresh(dto);
        return R.ok(tokenVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        appAuthService.logout();
        return R.ok();
    }
}
