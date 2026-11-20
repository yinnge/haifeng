package com.haifeng.common.service.resource;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.haifeng.common.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    /** 公共读 bucket 中通用图片 objectKey 的固定前缀（规划师头像等），与 uploadPublicBytes 保持一致 */
    private static final String PUBLIC_IMAGE_KEY_PREFIX = "haifeng/images/";
    /** 培训机构图片 objectKey 前缀，与 V83 种子数据的 haifeng/institutions/ 同目录、同 bucket */
    public static final String PUBLIC_INSTITUTION_KEY_PREFIX = "haifeng/institutions/";

    private final OssProperties ossProperties;

    private OSS createClient() {
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
    }

    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = "haifeng/files/" + UUID.randomUUID().toString().replace("-", "") + ext;

        OSS ossClient = createClient();
        try {
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    ossProperties.getBucketName(), objectKey, inputStream);
            ossClient.putObject(putObjectRequest);
            inputStream.close();
            log.info("文件上传OSS成功: bucket={}, key={}", ossProperties.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("文件上传OSS失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return objectKey;
    }

    /**
     * 上传字节数组到OSS（服务端生成的文件，如加水印后的PDF）。
     * 注意：整个文件会驻留内存，调用方需自行控制文件大小。
     *
     * @param data      文件内容
     * @param objectKey 完整的OSS对象key（调用方自行拼装，便于按业务规划目录）
     * @return objectKey
     */
    public String uploadBytes(byte[] data, String objectKey) {
        OSS ossClient = createClient();
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            ossClient.putObject(ossProperties.getBucketName(), objectKey, inputStream);
            log.info("字节流上传OSS成功: bucket={}, key={}, size={}",
                    ossProperties.getBucketName(), objectKey, data.length);
            return objectKey;
        } catch (Exception e) {
            log.error("字节流上传OSS失败: key={}, error={}", objectKey, e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 从OSS下载对象为字节数组（仅用于服务端加工，如加水印）。
     * 注意：整个文件会驻留内存，大文件需谨慎（推荐配合大小上限校验使用）。
     */
    /**
     * 上传公开素材图片到「公共读」Bucket，返回可直接访问的完整 URL。
     *
     * <p>与 {@link #uploadFile(MultipartFile)} 的关键区别：
     * <ul>
     *   <li>{@code uploadFile} 写入私有读 bucket（承载 VIP 门禁文件），只返回 objectKey，
     *       访问必须经后端签发预签名 URL；</li>
     *   <li>本方法写入独立的公共读 bucket，返回永久有效的完整 URL，
     *       供头像 / 轮播图 / 二维码等公开素材直接被 C 端 {@code <img src>} 引用。</li>
     * </ul>
     * 严禁把私有 bucket 改成公共读来复用本方法 —— 那会让 fileload 的 VIP 门禁失效。
     *
     * @param file      图片文件
     * @param extension 扩展名（含点，如 {@code .png}）
     * @return 可直接访问的完整 URL
     */
    public String uploadPublicImage(MultipartFile file, String extension) {
        try {
            return uploadPublicBytes(file.getBytes(), extension);
        } catch (IOException e) {
            throw new RuntimeException("图片读取失败: " + e.getMessage());
        }
    }

    /**
     * 上传公开素材字节流到「公共读」Bucket，返回可直接访问的完整 URL。
     *
     * @param data      图片字节内容
     * @param extension 扩展名（含点，如 {@code .png}）
     * @return 可直接访问的完整 URL
     */
    public String uploadPublicBytes(byte[] data, String extension) {
        return uploadPublicBytes(data, extension, PUBLIC_IMAGE_KEY_PREFIX);
    }

    /**
     * 上传公开素材字节流到「公共读」Bucket 的指定前缀下。
     * <p>OSS 没有真实目录，前缀即「文件夹」。同 bucket 下可用不同 keyPrefix 区分业务，
     * 如培训机构图片走 {@link #PUBLIC_INSTITUTION_KEY_PREFIX}，与种子数据同目录。
     *
     * @param data      图片字节内容
     * @param extension 扩展名（含点，如 {@code .png}）
     * @param keyPrefix objectKey 固定前缀
     * @return 可直接访问的完整 URL
     */
    public String uploadPublicBytes(byte[] data, String extension, String keyPrefix) {
        String publicBucket = ossProperties.getPublicBucketName();
        if (publicBucket == null || publicBucket.isBlank()) {
            throw new IllegalStateException("公开素材 Bucket 未配置（oss.public-bucket-name），无法上传图片");
        }

        String objectKey = keyPrefix + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = createClient();
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(publicBucket, objectKey, inputStream);
            // 必须显式设置 Content-Type：SDK 用 InputStream 构造时默认 application/octet-stream，
            // 浏览器会因此把 SVG 当成附件下载而不是内联渲染（PNG/JPG 靠魔数嗅探侥幸能显示，SVG 不行）。
            // 注意：OSS 不支持 putObject 自定义 HTTP 响应头（实测 X-Content-Type-Options 会被静默丢弃），
            // 故这里只设 Content-Type；SVG 的 XSS 防护由「上传前内容消毒 + bucket 强制下载」承担。
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(resolveContentType(extension));
            putObjectRequest.setMetadata(metadata);
            ossClient.putObject(putObjectRequest);
            log.info("公开素材图片上传OSS成功: bucket={}, key={}, size={}, contentType={}",
                    publicBucket, objectKey, data.length, metadata.getContentType());
        } catch (Exception e) {
            log.error("公开素材图片上传OSS失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return buildPublicUrl(objectKey);
    }

    /** 上传培训机构图片（轮播图 / 微信二维码），落到 haifeng/institutions/ 前缀 */
    public String uploadInstitutionImage(byte[] data, String extension) {
        return uploadPublicBytes(data, extension, PUBLIC_INSTITUTION_KEY_PREFIX);
    }

    /**
     * 按扩展名解析 Content-Type。
     */
    private String resolveContentType(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        switch (extension.toLowerCase(Locale.ROOT)) {
            case ".png":
                return "image/png";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".webp":
                return "image/webp";
            case ".svg":
                return "image/svg+xml";
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 拼接公开素材的完整访问 URL。
     * <p>优先使用显式配置的 {@code publicUrlPrefix}；未配置时按
     * {@code https://{bucket}.{endpointHost}} 推导（endpoint 形如
     * {@code https://oss-cn-beijing.aliyuncs.com}）。
     */
    public String buildPublicUrl(String objectKey) {
        String prefix = ossProperties.getPublicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            String bucket = ossProperties.getPublicBucketName();
            String endpoint = ossProperties.getEndpoint();
            if (bucket == null || bucket.isBlank() || endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("公开素材访问地址未配置（oss.public-url-prefix / oss.public-bucket-name）");
            }
            // 去掉 endpoint 末尾斜杠与协议头，取 host 部分
            String host = endpoint.replaceFirst("^https?://", "").replaceAll("/+$", "");
            prefix = "https://" + bucket + "." + host;
        }
        return prefix.replaceAll("/+$", "") + "/" + objectKey;
    }

    /**
     * 拼接指定公共读 bucket 的完整访问 URL（通用版）。
     * <p>优先用显式 {@code urlPrefix}；留空则按 {@code https://{bucket}.{endpointHost}} 推导。
     *
     * @param bucket    bucket 名（用于留空时推导前缀）
     * @param urlPrefix 显式访问前缀，可空
     * @param objectKey 对象 key
     * @param label     配置缺失时的提示标签（如「院校 Logo」）
     */
    public String buildBucketUrl(String bucket, String urlPrefix, String objectKey, String label) {
        String prefix = urlPrefix;
        if (prefix == null || prefix.isBlank()) {
            String endpoint = ossProperties.getEndpoint();
            if (bucket == null || bucket.isBlank() || endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException(label + "访问地址未配置，请检查 bucket 与 url-prefix 配置");
            }
            String host = endpoint.replaceFirst("^https?://", "").replaceAll("/+$", "");
            prefix = "https://" + bucket + "." + host;
        }
        return prefix.replaceAll("/+$", "") + "/" + objectKey;
    }

    /**
     * 上传图片字节到指定公共读 bucket，objectKey = keyPrefix + UUID + 扩展名，返回完整访问 URL。
     * <p>admin 的院校 Logo / 轮播图上传都走这里，各 bucket 用不同 keyPrefix 区分，
     * 便于取消上传时从 URL 反推所属 bucket 并删除。
     *
     * @param bucket    publid-read bucket 名
     * @param urlPrefix 访问前缀，可空（留空时按 bucket+endpoint 推导）
     * @param keyPrefix objectKey 固定前缀（如 universities/logo/）
     * @param label     日志/异常里的可读标签
     * @return 完整可访问 URL
     */
    public String uploadImageToBucket(byte[] data, String extension, String bucket,
                                      String urlPrefix, String keyPrefix, String label) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException(label + "Bucket 未配置，无法上传");
        }
        String objectKey = keyPrefix + UUID.randomUUID().toString().replace("-", "") + extension;

        OSS ossClient = createClient();
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, objectKey, inputStream);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(resolveContentType(extension));
            putObjectRequest.setMetadata(metadata);
            ossClient.putObject(putObjectRequest);
            log.info("{}上传OSS成功: bucket={}, key={}, size={}, contentType={}",
                    label, bucket, objectKey, data.length, metadata.getContentType());
        } catch (Exception e) {
            log.error("{}上传OSS失败: bucket={}, key={}, error={}", label, bucket, objectKey, e.getMessage(), e);
            throw new RuntimeException(label + "上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return buildBucketUrl(bucket, urlPrefix, objectKey, label);
    }

    /**
     * 删除指定 bucket 中的对象（公共读 bucket 通用版，按完整 URL 反推 objectKey）。
     * <p>只处理 URL 中含 {@code keyPrefix} 的对象；外部地址 / 其它 bucket 跳过，避免误删。
     * 删除异常只记日志不抛出，不影响主流程。
     *
     * @param url       完整访问 URL
     * @param keyPrefix 该 bucket 的 objectKey 前缀（如 universities/logo/）
     * @param bucket    目标 bucket
     * @param label     日志标签
     */
    public void deleteBucketFileByUrl(String url, String keyPrefix, String bucket, String label) {
        if (url == null || url.isBlank()) {
            return;
        }
        int idx = url.indexOf(keyPrefix);
        if (idx < 0) {
            return;
        }
        String objectKey = url.substring(idx);
        int query = objectKey.indexOf('?');
        if (query >= 0) {
            objectKey = objectKey.substring(0, query);
        }
        int fragment = objectKey.indexOf('#');
        if (fragment >= 0) {
            objectKey = objectKey.substring(0, fragment);
        }
        if (bucket == null || bucket.isBlank()) {
            log.warn("{}Bucket 未配置，跳过删除: key={}", label, objectKey);
            return;
        }
        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(bucket, objectKey);
            log.info("{}删除OSS成功: bucket={}, key={}", label, bucket, objectKey);
        } catch (Exception e) {
            log.error("{}删除OSS失败: bucket={}, key={}, error={}", label, bucket, objectKey, e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * SVG 消毒：剥离 script 块、on*= 事件属性与 javascript: 伪协议。
     * <p>公共读 bucket 上的 SVG 可被任意人直接访问，内嵌脚本会形成存储型 XSS。
     * 提到公共工具供 admin 各上传接口复用，避免多处重复实现。
     */
    public static byte[] sanitizeSvg(byte[] raw) {
        String content = new String(raw, StandardCharsets.UTF_8);
        String cleaned = content
                .replaceAll("(?is)<script[^>]*>.*?</script>", "")
                .replaceAll("(?is)<script[^>]*/?>", "")
                .replaceAll("(?is)\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)", "")
                .replaceAll("(?is)javascript\\s*:", "");
        return cleaned.length() == content.length() ? raw : cleaned.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 院校 Logo / 轮播图 objectKey 前缀，供上传与按 URL 删除共用。
     */
    public static final String UNIVERSITY_LOGO_KEY_PREFIX = "universities/logo/";
    public static final String UNIVERSITY_DETAIL_KEY_PREFIX = "universities/detail/";
    /** 校园图册图片 objectKey 前缀（universities-album bucket） */
    public static final String UNIVERSITY_ALBUM_KEY_PREFIX = "universities/album/";
    /** 城市封面图 objectKey 前缀（city-cover-image bucket） */
    public static final String CITY_COVER_KEY_PREFIX = "city/cover/";

    /** 上传院校 Logo，返回完整访问 URL */
    public String uploadUniversityLogo(byte[] data, String extension) {
        return uploadImageToBucket(data, extension,
                ossProperties.getUniversityLogoBucketName(),
                ossProperties.getUniversityLogoUrlPrefix(),
                UNIVERSITY_LOGO_KEY_PREFIX, "院校Logo");
    }

    /** 上传院校详情轮播图，返回完整访问 URL */
    public String uploadUniversityDetailImage(byte[] data, String extension) {
        return uploadImageToBucket(data, extension,
                ossProperties.getUniversityDetailBucketName(),
                ossProperties.getUniversityDetailUrlPrefix(),
                UNIVERSITY_DETAIL_KEY_PREFIX, "院校轮播图");
    }

    /** 从完整 URL 删除院校 Logo 对象 */
    public void deleteUniversityLogoByUrl(String url) {
        deleteBucketFileByUrl(url, UNIVERSITY_LOGO_KEY_PREFIX,
                ossProperties.getUniversityLogoBucketName(), "院校Logo");
    }

    /** 从完整 URL 删除院校详情轮播图对象 */
    public void deleteUniversityDetailImageByUrl(String url) {
        deleteBucketFileByUrl(url, UNIVERSITY_DETAIL_KEY_PREFIX,
                ossProperties.getUniversityDetailBucketName(), "院校轮播图");
    }

    /** 上传校园图册图片，返回完整访问 URL */
    public String uploadUniversityAlbumImage(byte[] data, String extension) {
        return uploadImageToBucket(data, extension,
                ossProperties.getUniversityAlbumBucketName(),
                ossProperties.getUniversityAlbumUrlPrefix(),
                UNIVERSITY_ALBUM_KEY_PREFIX, "校园图册图片");
    }

    /** 从完整 URL 删除校园图册图片对象 */
    public void deleteUniversityAlbumImageByUrl(String url) {
        deleteBucketFileByUrl(url, UNIVERSITY_ALBUM_KEY_PREFIX,
                ossProperties.getUniversityAlbumBucketName(), "校园图册图片");
    }

    /** 上传城市封面图，返回完整访问 URL */
    public String uploadCityCoverImage(byte[] data, String extension) {
        return uploadImageToBucket(data, extension,
                ossProperties.getCityCoverBucketName(),
                ossProperties.getCityCoverUrlPrefix(),
                CITY_COVER_KEY_PREFIX, "城市封面图");
    }

    /** 从完整 URL 删除城市封面图对象 */
    public void deleteCityCoverImageByUrl(String url) {
        deleteBucketFileByUrl(url, CITY_COVER_KEY_PREFIX,
                ossProperties.getCityCoverBucketName(), "城市封面图");
    }

    /**
     * 会员头像 objectKey：按 memberId 固定，覆盖上传即为「更换头像」，无需手动删旧对象。
     * 注意 key 不带扩展名，Content-Type 由上传时按实际格式写入元数据。
     */
    public String memberAvatarObjectKey(Long memberId) {
        return "haifeng/avatars/" + memberId;
    }

    /**
     * 拼接会员头像完整访问 URL。
     */
    public String buildMemberAvatarUrl(String objectKey) {
        String prefix = ossProperties.getAvatarUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            String bucket = ossProperties.getAvatarBucketName();
            String endpoint = ossProperties.getEndpoint();
            if (bucket == null || bucket.isBlank() || endpoint == null || endpoint.isBlank()) {
                throw new IllegalStateException("会员头像访问地址未配置（oss.avatar-url-prefix / oss.avatar-bucket-name）");
            }
            String host = endpoint.replaceFirst("^https?://", "").replaceAll("/+$", "");
            prefix = "https://" + bucket + "." + host;
        }
        return prefix.replaceAll("/+$", "") + "/" + objectKey;
    }

    /**
     * 上传会员头像到「头像专用公共读 Bucket」，使用固定 objectKey 覆盖更新。
     * <p>返回可直接被 C 端 {@code <img src>} 引用的完整 URL。
     */
    public String uploadMemberAvatar(byte[] data, String extension, Long memberId) {
        String bucket = ossProperties.getAvatarBucketName();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("会员头像 Bucket 未配置（oss.avatar-bucket-name），无法上传头像");
        }
        String objectKey = memberAvatarObjectKey(memberId);

        OSS ossClient = createClient();
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, objectKey, inputStream);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(resolveContentType(extension));
            putObjectRequest.setMetadata(metadata);
            ossClient.putObject(putObjectRequest);
            log.info("会员头像上传OSS成功: bucket={}, key={}, size={}, contentType={}",
                    bucket, objectKey, data.length, metadata.getContentType());
        } catch (Exception e) {
            log.error("会员头像上传OSS失败: key={}, error={}", objectKey, e.getMessage(), e);
            throw new RuntimeException("头像上传失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }

        return buildMemberAvatarUrl(objectKey);
    }

    /**
     * 判断会员是否已上传过自定义头像（用于前端展示「我的头像」回切选项）。
     */
    public boolean memberAvatarExists(Long memberId) {
        String bucket = ossProperties.getAvatarBucketName();
        if (bucket == null || bucket.isBlank()) {
            return false;
        }
        OSS ossClient = createClient();
        try {
            return ossClient.doesObjectExist(bucket, memberAvatarObjectKey(memberId));
        } catch (Exception e) {
            log.error("查询会员头像失败: memberId={}, error={}", memberId, e.getMessage(), e);
            return false;
        } finally {
            ossClient.shutdown();
        }
    }

    public byte[] downloadBytes(String objectKey) {
        OSS ossClient = createClient();
        try (OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), objectKey);
             InputStream in = ossObject.getObjectContent()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        } catch (Exception e) {
            log.error("OSS文件下载失败: key={}, error={}", objectKey, e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    public String generatePresignedUrl(String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + ossProperties.getUrlExpire() * 1000L);

        OSS ossClient = createClient();
        try {
            URL url = ossClient.generatePresignedUrl(
                    ossProperties.getBucketName(), objectKey, expiration);
            return url.toString();
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 生成带下载文件名的预签名 URL。
     * 通过 response-content-disposition 指定下载文件名（与源文件名一致），
     * 浏览器下载时同名文件会自动追加 (1)、(2) 等后缀。
     *
     * @param objectKey         OSS 对象 key
     * @param downloadFileName  下载文件名（如「高中数学指南.pdf」）
     */
    public String generatePresignedUrl(String objectKey, String downloadFileName) {
        Date expiration = new Date(System.currentTimeMillis() + ossProperties.getUrlExpire() * 1000L);

        OSS ossClient = createClient();
        try {
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossProperties.getBucketName(), objectKey);
            request.setExpiration(expiration);
            if (downloadFileName != null && !downloadFileName.isBlank()) {
                // attachment + filename*=UTF-8''<percent-encoded>：正确支持中文文件名
                // URLEncoder 是表单编码（空格→+），但 RFC 5987 attr-value 应是 percent-encoded（空格→%20），
                // 否则浏览器按字面 "+" 显示文件名。预览路径另走干净 URL（不带 disposition），见 FileLoadServiceImpl。
                // 兼容性：3.17.4 SDK 无 setResponseDisposition，统一用 setResponseHeaders(ResponseHeaderOverrides)（3.17.4 原生 API）。
                String disposition = "attachment; filename*=UTF-8''"
                        + URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8)
                                .replace("+", "%20");
                ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
                responseHeaders.setContentDisposition(disposition);
                request.setResponseHeaders(responseHeaders);
            }
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } catch (Exception e) {
            log.error("生成预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名URL失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    public String calculateMd5(InputStream inputStream) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, bytesRead);
            }
            return HexFormat.of().formatHex(md5.digest());
        } catch (Exception e) {
            log.error("计算MD5失败: {}", e.getMessage(), e);
            throw new RuntimeException("计算MD5失败");
        }
    }

    public void deleteFile(String objectKey) {
        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), objectKey);
            log.info("OSS文件删除成功: key={}", objectKey);
        } catch (Exception e) {
            log.error("OSS文件删除失败: key={}, error={}", objectKey, e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 删除「公共读」bucket 中的素材对象（头像 / 轮播图 / 二维码等）。
     * 与 {@link #deleteFile(String)} 严格区分：后者删的是私有 bucket（VIP 门禁文件），
     * 公共素材在 publicBucketName，切勿混用。
     */
    public void deletePublicFile(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        String publicBucket = ossProperties.getPublicBucketName();
        if (publicBucket == null || publicBucket.isBlank()) {
            log.warn("公共素材 Bucket 未配置，跳过删除: key={}", objectKey);
            return;
        }
        OSS ossClient = createClient();
        try {
            ossClient.deleteObject(publicBucket, objectKey);
            log.info("公共素材OSS删除成功: bucket={}, key={}", publicBucket, objectKey);
        } catch (Exception e) {
            log.error("公共素材OSS删除失败: bucket={}, key={}, error={}", publicBucket, objectKey, e.getMessage(), e);
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 按完整 URL 删除本系统公共读图片，自动识别所属 bucket。
     * <p>支持以下 objectKey 前缀：
     * <ul>
     *   <li>{@code haifeng/images/}        — 通用公开素材（规划师头像等）</li>
     *   <li>{@code haifeng/institutions/}  — 培训机构图片（轮播图 / 二维码，与种子数据同目录）</li>
     *   <li>{@code universities/logo/}   — 院校 Logo</li>
     *   <li>{@code universities/detail/} — 院校详情轮播图</li>
     *   <li>{@code universities/album/}  — 校园图册图片</li>
     *   <li>{@code city/cover/}          — 城市封面图</li>
     * </ul>
     * 外部 http(s) 地址、相对路径、私有 bucket 文件一律跳过，避免误删。
     * <p>这样上一轮的 {@code /cleanup-images} 接口无需改动即可回收所有本系统图片。</p>
     */
    public void deletePublicFileByUrl(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        if (url.contains(UNIVERSITY_LOGO_KEY_PREFIX)) {
            deleteUniversityLogoByUrl(url);
            return;
        }
        if (url.contains(UNIVERSITY_DETAIL_KEY_PREFIX)) {
            deleteUniversityDetailImageByUrl(url);
            return;
        }
        if (url.contains(UNIVERSITY_ALBUM_KEY_PREFIX)) {
            deleteUniversityAlbumImageByUrl(url);
            return;
        }
        if (url.contains(CITY_COVER_KEY_PREFIX)) {
            deleteCityCoverImageByUrl(url);
            return;
        }
        // 培训机构图片与通用素材在同一个 public bucket，只是前缀不同
        String institutionKey = extractPublicObjectKey(url, PUBLIC_INSTITUTION_KEY_PREFIX);
        if (institutionKey != null) {
            deletePublicFile(institutionKey);
            return;
        }
        String objectKey = extractPublicObjectKey(url, PUBLIC_IMAGE_KEY_PREFIX);
        if (objectKey == null) {
            // 非本系统公共素材 URL，跳过（外部地址 / 相对路径 / 其它 bucket）
            return;
        }
        deletePublicFile(objectKey);
    }

    /** 从完整 URL 截取 objectKey，并去掉可能携带的 query 参数或 fragment */
    private static String extractPublicObjectKey(String url, String keyPrefix) {
        int idx = url.indexOf(keyPrefix);
        if (idx < 0) {
            return null;
        }
        String objectKey = url.substring(idx);
        int query = objectKey.indexOf('?');
        if (query >= 0) {
            objectKey = objectKey.substring(0, query);
        }
        int fragment = objectKey.indexOf('#');
        if (fragment >= 0) {
            objectKey = objectKey.substring(0, fragment);
        }
        return objectKey;
    }
}
