package com.haifeng.app.service.impl.watermark;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.haifeng.app.config.WatermarkProperties;
import com.haifeng.app.service.watermark.DocumentToPdfConverter;
import com.haifeng.app.service.watermark.FileWatermarkService;
import com.haifeng.app.util.watermark.ImageToPdfUtil;
import com.haifeng.app.util.watermark.PdfWatermarkUtil;
import com.haifeng.app.util.watermark.WatermarkConstant;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.entity.resource.FileInfo;
import com.haifeng.common.mapper.resource.FileInfoMapper;
import com.haifeng.common.service.resource.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 文件下载水印服务实现。
 *
 * 并发处理：同一文件只允许一个线程生成（Redis 锁），其余线程轮询等待生成结果，
 * 而不是直接降级——否则同一份文件有的用户拿到带水印的、有的拿到原文件。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileWatermarkServiceImpl implements FileWatermarkService {

    private static final String STATUS_READY = "READY";
    private static final String STATUS_FAILED = "FAILED";

    /** 等待他人生成完成的最长时间（毫秒） */
    private static final long WAIT_READY_TIMEOUT_MS = 60_000L;
    private static final long WAIT_INTERVAL_MS = 500L;

    private static final Set<String> WATERMARK_TYPES = Set.of(
            "pdf", "png", "jpg", "jpeg", "bmp",
            "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    private static final Set<String> IMAGE_TYPES = Set.of("png", "jpg", "jpeg", "bmp");

    private final FileInfoMapper fileInfoMapper;
    private final OssService ossService;
    private final WatermarkProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectProvider<DocumentToPdfConverter> converterProvider;

    @Override
    public String getWatermarkedDownloadUrl(FileInfo fileInfo) {
        if (!properties.isEnabled()) {
            return fallback(fileInfo);
        }

        String type = normalize(fileInfo.getFileType());
        if (!WATERMARK_TYPES.contains(type)) {
            // 无「页面」概念的格式（zip/cad/3d 等）不加水印
            return fallback(fileInfo);
        }

        // 已生成过：直接复用（水印固定，所有用户共享同一份）
        if (STATUS_READY.equals(fileInfo.getWatermarkStatus())
                && StringUtils.hasText(fileInfo.getWatermarkedFileUrl())) {
            return presigned(fileInfo.getWatermarkedFileUrl(), buildDownloadName(fileInfo));
        }

        Long size = fileInfo.getFileSize();
        if (size != null && size > WatermarkConstant.MAX_WATERMARK_SOURCE_BYTES) {
            markFailed(fileInfo.getId(), "文件超过水印处理上限（" + size + " bytes）");
            return fallback(fileInfo);
        }

        if (!canGenerate(type)) {
            return fallback(fileInfo);
        }
        return generateOrFallback(fileInfo, type);
    }

    private String generateOrFallback(FileInfo fileInfo, String type) {
        String lockKey = RedisKeyConstant.getWatermarkGenLockKey(fileInfo.getId());
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                lockKey, "1", properties.getGenerateLockTtlSeconds(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(acquired)) {
            // 别的线程正在生成：等待它完成，而不是自己重算一遍
            return waitForReady(fileInfo);
        }

        try {
            byte[] source = ossService.downloadBytes(fileInfo.getFileUrl());
            byte[] pdf = toPdf(source, type);
            byte[] watermarked = PdfWatermarkUtil.addWatermark(pdf);

            String objectKey = WatermarkConstant.OSS_WATERMARK_PREFIX + fileInfo.getId() + ".pdf";
            ossService.uploadBytes(watermarked, objectKey);
            markReady(fileInfo.getId(), objectKey);
            log.info("带水印PDF生成成功: id={}, sourceType={}, size={} -> {} bytes",
                    fileInfo.getId(), type, source.length, watermarked.length);
            return presigned(objectKey, buildDownloadName(fileInfo));
        } catch (Exception e) {
            log.error("带水印PDF生成失败，降级返回原文件: id={}, type={}", fileInfo.getId(), type, e);
            markFailed(fileInfo.getId(), truncate(e.getMessage()));
            return fallback(fileInfo);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /** 等待并发线程生成完成，超时则降级 */
    private String waitForReady(FileInfo fileInfo) {
        long deadline = System.currentTimeMillis() + WAIT_READY_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            FileInfo fresh = fileInfoMapper.selectById(fileInfo.getId());
            if (fresh != null && STATUS_READY.equals(fresh.getWatermarkStatus())
                    && StringUtils.hasText(fresh.getWatermarkedFileUrl())) {
                return presigned(fresh.getWatermarkedFileUrl(), buildDownloadName(fileInfo));
            }
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.warn("等待带水印PDF生成超时，降级返回原文件: id={}", fileInfo.getId());
        return fallback(fileInfo);
    }

    /** 按类型路由到 PDF：PDF 原样、图片包成 PDF、Office 走转换器 */
    private byte[] toPdf(byte[] source, String type) {
        if ("pdf".equals(type)) {
            return source;
        }
        if (IMAGE_TYPES.contains(type)) {
            return ImageToPdfUtil.imageToPdf(source);
        }
        DocumentToPdfConverter converter = converterProvider.getIfAvailable();
        if (converter == null || !converter.supports(type)) {
            throw new IllegalStateException("无可用的 Office 转换器，无法处理类型: " + type);
        }
        return converter.convertToPdf(source, type);
    }

    private boolean canGenerate(String type) {
        if ("pdf".equals(type) || IMAGE_TYPES.contains(type)) {
            return true;
        }
        DocumentToPdfConverter converter = converterProvider.getIfAvailable();
        return converter != null && converter.supports(type);
    }

    private void markReady(Long id, String objectKey) {
        try {
            LambdaUpdateWrapper<FileInfo> uw = new LambdaUpdateWrapper<>();
            uw.eq(FileInfo::getId, id)
                    .set(FileInfo::getWatermarkedFileUrl, objectKey)
                    .set(FileInfo::getWatermarkStatus, STATUS_READY)
                    .set(FileInfo::getUpdateTime, OffsetDateTime.now())
                    .setSql("watermark_fail_reason = NULL");
            fileInfoMapper.update(null, uw);
        } catch (Exception e) {
            log.error("回写水印状态失败: id={}", id, e);
        }
    }

    private void markFailed(Long id, String reason) {
        try {
            LambdaUpdateWrapper<FileInfo> uw = new LambdaUpdateWrapper<>();
            uw.eq(FileInfo::getId, id)
                    .set(FileInfo::getWatermarkStatus, STATUS_FAILED)
                    .set(FileInfo::getWatermarkFailReason, truncate(reason))
                    .set(FileInfo::getUpdateTime, OffsetDateTime.now());
            fileInfoMapper.update(null, uw);
        } catch (Exception e) {
            log.error("回写水印失败状态出错: id={}", id, e);
        }
    }

    private String presigned(String objectKey, String downloadName) {
        return ossService.generatePresignedUrl(objectKey, downloadName);
    }

    /** 降级：返回源文件签名 URL（无水印，但保证业务可用） */
    private String fallback(FileInfo fileInfo) {
        return ossService.generatePresignedUrl(fileInfo.getFileUrl(), fileInfo.getFileName());
    }

    /** 下载文件名：原文件名去扩展名 + .pdf，让用户知道拿到的是 PDF 版 */
    private String buildDownloadName(FileInfo fileInfo) {
        String name = fileInfo.getFileName();
        if (!StringUtils.hasText(name)) {
            return "download.pdf";
        }
        int dot = name.lastIndexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name) + ".pdf";
    }

    private String normalize(String fileType) {
        return fileType == null ? "" : fileType.toLowerCase(Locale.ROOT);
    }

    private String truncate(String msg) {
        if (msg == null) {
            return null;
        }
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
