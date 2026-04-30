package com.haifeng.admin.controller.auth;

import com.haifeng.admin.service.auth.AdminAuthService;
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

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public R<TokenVO> login(@Valid @RequestBody LoginDTO dto) {
        TokenVO tokenVO = adminAuthService.login(dto);
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
