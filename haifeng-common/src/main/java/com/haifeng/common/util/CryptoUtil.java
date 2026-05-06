package com.haifeng.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CryptoUtil {

    private CryptoUtil() {
    }

    /**
     * AES 加密
     *
     * @param plainText 明文
     * @param key       密钥（16/24/32位）
     * @return 加密后的 Base64 字符串，输入为空返回 null
     */
    public static String encrypt(String plainText, String key) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(padKey(key));
            return aes.encryptBase64(plainText);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES 解密
     *
     * @param cipherText 密文（Base64）
     * @param key        密钥
     * @return 解密后的明文，输入为空返回 null
     */
    public static String decrypt(String cipherText, String key) {
        if (cipherText == null || cipherText.isEmpty()) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(padKey(key));
            return aes.decryptStr(cipherText);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成盲索引（SHA-256）
     *
     * @param plainText 原文
     * @param salt      盐值
     * @return 64位十六进制哈希值，输入为空返回 null
     */
    public static String blindIndex(String plainText, String salt) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        String normalized = normalize(plainText);
        String salted = salt + normalized;
        return SecureUtil.sha256(salted);
    }

    /**
     * 规范化处理：转小写、去除首尾空格
     */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase();
    }

    /**
     * 补齐密钥到 16 位
     */
    private static byte[] padKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[16];
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 16));
        return paddedKey;
    }
}
