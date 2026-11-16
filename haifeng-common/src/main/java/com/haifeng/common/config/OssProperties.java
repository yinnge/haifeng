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

    /**
     * 公开素材 Bucket 名称（公共读）。
     * <p>与 {@link #bucketName} 分离：后者是私有读 bucket，承载 VIP 门禁文件
     * （fileload 模块），其 objectKey 一旦可匿名直连即绕过全部权限校验，
     * 因此严禁把私有 bucket 改成公共读。公开素材（头像 / 轮播图 / 二维码）
     * 必须放在独立的公共读 bucket 里。
     */
    private String publicBucketName;

    /**
     * 公开素材访问前缀（用于拼接完整可访问 URL）。
     * <p>留空则由 publicBucketName + endpoint 自动推导，形如
     * {@code https://{bucket}.oss-cn-beijing.aliyuncs.com}。
     */
    private String publicUrlPrefix;

    /** 预签名 URL 过期时间（秒） */
    private Integer urlExpire = 3600;

    /** KKFileView 预览服务地址（本地: http://127.0.0.1:8012, Docker: http://haifeng-kkfileview:8012） */
    private String kkfileviewBaseUrl = "http://127.0.0.1:8012";
}
