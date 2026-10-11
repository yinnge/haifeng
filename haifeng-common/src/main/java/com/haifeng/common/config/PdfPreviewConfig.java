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

    /** 签名有效期（秒），默认 2 小时（多用途 token，覆盖一次预览交互会话；
     *  曾用 300s——读者阅读报告超过 5 分钟后目录跳页/刷新必然 410，
     *  pdf.js 表现为“载入PDF时发生错误”） */
    private Integer expireSeconds = 7200;

    /** 后端服务 base URL —— 语义为【服务端可达地址】，仅供 kkfileview 容器回源抓取 PDF。
     *  <p>浏览器侧已不再使用它：directPdfUrl 现在返回同源相对路径
     *  （dev 走 Vite proxy、prod 走 nginx），所以这里写 Docker 内网服务名也不会影响前端预览。
     *  <p>注意：切勿指望它能让浏览器访问——写 127.0.0.1 / localhost 在容器内是自身回环，kkfileview 抓不到 app。 */
    private String baseUrl;
}
