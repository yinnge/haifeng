package com.haifeng.common.service.resource;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.haifeng.common.config.OssProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

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

    /**
     * 生成预签名上传 URL（前端直传 OSS 用）
     * @param fileName 原始文件名（用于提取扩展名）
     * @return PresignedUploadResult 包含上传 URL 和 objectKey
     */
    public PresignedUploadResult generatePresignedUploadUrl(String fileName) {
        String ext = "";
        if (fileName != null && fileName.contains(".")) {
            ext = fileName.substring(fileName.lastIndexOf("."));
        }
        String objectKey = "haifeng/files/" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 预签名 URL 有效期10分钟
        Date expiration = new Date(System.currentTimeMillis() + 10 * 60 * 1000L);

        OSS ossClient = createClient();
        try {
            // 生成 PUT 方法的预签名 URL
            URL url = ossClient.generatePresignedUrl(
                    ossProperties.getBucketName(),
                    objectKey,
                    expiration,
                    com.aliyun.oss.http.HttpMethod.PUT);

            log.info("生成预签名上传URL成功: key={}", objectKey);
            return new PresignedUploadResult(url.toString(), objectKey);
        } catch (Exception e) {
            log.error("生成预签名上传URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成预签名上传URL失败: " + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 预签名上传结果
     */
    public record PresignedUploadResult(String uploadUrl, String objectKey) {}

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
