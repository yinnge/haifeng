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

    /**
     * 会员头像专用 Bucket（公共读）。
     * <p>与 {@link #publicBucketName} 分离：头像使用固定 objectKey（每用户一个），
     * 覆盖上传即「删除旧图更新新图」，且与 admin 上传的公开素材互不干扰。
     */
    private String avatarBucketName;

    /**
     * 会员头像访问前缀；留空则由 avatarBucketName + endpoint 推导。
     */
    private String avatarUrlPrefix;

    /**
     * 院校 Logo 专用 Bucket（公共读）。
     * <p>院校基础信息 imageUrl 上传目标，与通用公开 bucket 分离，便于独立管理与防盗链。
     */
    private String universityLogoBucketName;

    /** 院校 Logo 访问前缀；留空则由 universityLogoBucketName + endpoint 推导。 */
    private String universityLogoUrlPrefix;

    /**
     * 院校详情轮播图专用 Bucket（公共读）。
     * <p>院校详情 carouselImages 上传目标。
     */
    private String universityDetailBucketName;

    /** 院校详情轮播图访问前缀；留空则由 universityDetailBucketName + endpoint 推导。 */
    private String universityDetailUrlPrefix;

    /**
     * 校园图册图片专用 Bucket（公共读）。
     * <p> CampusGallery.imageUrl 上传目标。
     */
    private String universityAlbumBucketName;

    /** 校园图册图片访问前缀；留空则由 universityAlbumBucketName + endpoint 推导。 */
    private String universityAlbumUrlPrefix;

    /**
     * 城市封面图专用 Bucket（公共读）。
     * <p> City.coverImageUrl 上传目标。
     */
    private String cityCoverBucketName;

    /** 城市封面图访问前缀；留空则由 cityCoverBucketName + endpoint 推导。 */
    private String cityCoverUrlPrefix;

    /** 预签名 URL 过期时间（秒） */
    private Integer urlExpire = 3600;

    /** KKFileView 预览服务地址（本地: http://127.0.0.1:8012, Docker: http://haifeng-kkfileview:8012） */
    private String kkfileviewBaseUrl = "http://127.0.0.1:8012";
}
