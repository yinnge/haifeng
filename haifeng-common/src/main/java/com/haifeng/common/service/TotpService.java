package com.haifeng.common.service;

/**
 * TOTP 双因素认证服务
 */
public interface TotpService {

    /**
     * 生成 TOTP 密钥
     *
     * @return Base32 编码的密钥
     */
    String generateSecret();

    /**
     * 生成二维码 Base64 图片
     *
     * @param secret   TOTP 密钥
     * @param username 用户名（显示在 App 中）
     * @return Base64 编码的 PNG 图片
     */
    String generateQrCodeBase64(String secret, String username);

    /**
     * 验证 TOTP 动态码
     *
     * @param secret TOTP 密钥
     * @param code   6位动态码
     * @return 是否验证通过
     */
    boolean verifyCode(String secret, String code);
}
