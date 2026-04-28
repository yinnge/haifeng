package com.haifeng.admin.service;

import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

/**
 * 管理员认证服务接口
 */
public interface AdminAuthService {

    /**
     * 管理员登录
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
     * 管理员登出
     */
    void logout();
}
