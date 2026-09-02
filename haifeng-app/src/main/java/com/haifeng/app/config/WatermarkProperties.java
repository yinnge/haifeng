package com.haifeng.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件下载水印配置。
 *
 * 关键开关：watermark.converter.enabled
 * - false（默认）：不做 Office→PDF 转换，Office 文件下载时降级返回原文件（业务不中断）
 * - true：连接外部 LibreOffice 服务做转换，转换后再加水印
 */
@Data
@Component
@ConfigurationProperties(prefix = "watermark")
public class WatermarkProperties {

    /** 是否启用下载水印（关闭则完全走原来的原文件签名 URL 逻辑） */
    private boolean enabled = true;

    /** 单次生成带水印 PDF 的并发锁过期时间（秒），防止同一文件被并发重复生成 */
    private long generateLockTtlSeconds = 180L;

    private final Converter converter = new Converter();

    /**
     * Office / 老格式 → PDF 转换器配置（底层为 LibreOffice，通过 JODConverter 远程连接）
     */
    @Data
    public static class Converter {

        /** 是否启用转换；未部署 LibreOffice 时保持 false，Office 文件会降级返回原文件 */
        private boolean enabled = false;

        /** LibreOffice 监听主机（Docker 内网填服务名，如 haifeng-libreoffice） */
        private String host = "127.0.0.1";

        /** LibreOffice 监听端口（默认 8100） */
        private int port = 8100;

        /** 单次转换超时（毫秒） */
        private long timeoutMillis = 120000L;
    }
}
