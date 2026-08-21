package com.haifeng.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** OSS Endpoint（带协议头） */
    private String endpoint;

    /** 地域 */
    private String region;

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** Bucket 名称 */
    private String bucketName;

    /** 预签名 URL 过期时间（秒） */
    private Integer urlExpire = 3600;

    /** KKFileView 预览服务地址（本地: http://127.0.0.1:8012, Docker: http://haifeng-kkfileview:8012） */
    private String kkfileviewBaseUrl = "http://127.0.0.1:8012";
}
