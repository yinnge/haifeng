package com.haifeng.app.service.auth;

import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.common.dto.LoginDTO;
import com.haifeng.common.dto.RefreshTokenDTO;
import com.haifeng.common.vo.TokenVO;

public interface AppAuthService {

    TokenVO register(RegisterDTO dto);

    TokenVO login(LoginDTO dto);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();
}
