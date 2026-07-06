package com.haifeng.app.service.auth;

import com.haifeng.app.dto.auth.ForgotPasswordResetDTO;
import com.haifeng.app.dto.auth.ForgotPasswordSendCodeDTO;
import com.haifeng.app.dto.auth.RegisterDTO;
import com.haifeng.common.dto.auth.LoginDTO;
import com.haifeng.common.dto.auth.RefreshTokenDTO;
import com.haifeng.common.vo.auth.TokenVO;

public interface AppAuthService {

    TokenVO register(RegisterDTO dto);

    TokenVO login(LoginDTO dto);

    TokenVO refresh(RefreshTokenDTO dto);

    void logout();

    void forgotPasswordSendCode(ForgotPasswordSendCodeDTO dto);

    void forgotPasswordReset(ForgotPasswordResetDTO dto);
}
