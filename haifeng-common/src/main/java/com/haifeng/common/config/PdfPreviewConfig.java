package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * PDF 预览签名配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "pdf.preview")
public class PdfPreviewConfig {

    /** HMAC 签名密钥 */
    private String secret;

    /** 签名有效期（秒），默认 5 分钟 */
    private Integer expireSeconds = 300;

    /** 后端服务 base URL（供 kkfileview 回调获取 PDF） */
    private String baseUrl;
}
