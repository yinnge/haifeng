package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "haifeng.security")
public class SecurityProperties {

    /**
     * AES 加密密钥（必须是 16/24/32 位），从 .env 中的 AES_SECRET_KEY 读取
     */
    private String aesKey;

    /**
     * 盲索引哈希盐值，从 .env 中的 HASH_SALT 读取
     */
    private String hashSalt;
}
