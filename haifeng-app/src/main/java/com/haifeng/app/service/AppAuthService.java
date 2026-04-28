package com.haifeng.app.service;

import com.haifeng.app.dto.RegisterDTO;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

/**
 * 用户端认证服务接口
 */
public interface AppAuthService {

    /**
     * 用户注册
     *
     * @param dto 注册请求
     * @return Token信息
     */
    TokenVO register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录请求
     * @return Token信息
     */
    TokenVO login(LoginDTO dto);

    /**
     * 刷新Token
     *
     * @param dto 刷新请求
     * @return 新的Token信息
     */
    TokenVO refresh(RefreshTokenDTO dto);

    /**
     * 用户登出
     */
    void logout();
}
