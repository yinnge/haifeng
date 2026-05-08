package com.haifeng.admin.service.auth;

import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.vo.auth.TokenVO;

public interface AdminAuthService {

    /**
     * 管理员登录
     *
     * @param dto 登录信息
     * @return TokenVO（直接登录成功）或 PreAuthVO（需要 TOTP 二次验证）
     */
    Object login(LoginDTO dto);

    /**
     * TOTP 二次验证登录
     *
     * @param preAuthToken 预认证令牌
     * @param totpCode     6位动态验证码
     * @return Token信息
     */
    TokenVO loginWithTotp(String preAuthToken, String totpCode);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();
}
