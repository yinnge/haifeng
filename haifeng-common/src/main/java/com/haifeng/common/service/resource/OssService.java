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
        String publicBucket = ossProperties.getPublicBucketName();
        if (publicBucket == null || publicBucket.isBlank()) {
            throw new IllegalStateException("公开素材 Bucket 未配置（oss.public-bucket-name），无法上传图片");
        }

        String objectKey = "haifeng/images/" + UUID.randomUUID().toString().replace("-", "") + extension;

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
}
