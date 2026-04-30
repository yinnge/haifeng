package com.haifeng.admin.service.auth;

import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

public interface AdminAuthService {

    TokenVO login(LoginDTO dto);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();
}
