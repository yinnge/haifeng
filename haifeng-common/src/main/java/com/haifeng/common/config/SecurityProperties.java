package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "haifeng.security")
public class SecurityProperties {

    /**
     * AES 加密密钥（必须是 16/24/32 位）
     */
    private String aesKey = "haifeng_aes_key_16";

    /**
     * 盲索引哈希盐值
     */
    private String hashSalt = "haifeng_blind_index_salt_2024";
}
