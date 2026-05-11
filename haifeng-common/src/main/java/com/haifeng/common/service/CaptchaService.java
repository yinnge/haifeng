package com.haifeng.common.service;

import com.haifeng.common.vo.auth.CaptchaVO;

public interface CaptchaService {

    /**
     * 生成验证码
     *
     * @return 包含 uuid 和 Base64 图片的 VO
     */
    CaptchaVO generateCaptcha();

    /**
     * 验证验证码
     *
     * @param uuid 验证码标识
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    boolean validateCaptcha(String uuid, String code);
}
