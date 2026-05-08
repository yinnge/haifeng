package com.haifeng.admin.service.profile;

import com.haifeng.admin.dto.profile.PasswordUpdateDTO;
import com.haifeng.admin.dto.profile.ProfileUpdateDTO;
import com.haifeng.admin.dto.profile.TotpDisableDTO;
import com.haifeng.admin.dto.profile.TotpVerifyDTO;
import com.haifeng.admin.vo.profile.ProfileVO;
import com.haifeng.admin.vo.profile.TotpEnableVO;

public interface ProfileService {

    /**
     * 获取当前管理员信息
     */
    ProfileVO getProfile();

    /**
     * 修改个人信息
     */
    void updateProfile(ProfileUpdateDTO dto);

    /**
     * 修改密码
     */
    void updatePassword(PasswordUpdateDTO dto);

    /**
     * 开启 TOTP（生成密钥和二维码）
     */
    TotpEnableVO enableTotp();

    /**
     * 验证并确认绑定 TOTP
     */
    void verifyTotp(TotpVerifyDTO dto);

    /**
     * 关闭 TOTP
     */
    void disableTotp(TotpDisableDTO dto);

    /**
     * 获取当前 TOTP 二维码
     */
    TotpEnableVO getTotpQrCode();
}
