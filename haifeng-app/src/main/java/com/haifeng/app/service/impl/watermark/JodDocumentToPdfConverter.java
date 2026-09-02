package com.haifeng.app.service.impl.watermark;

import com.haifeng.app.config.WatermarkProperties;
import com.haifeng.app.service.watermark.DocumentToPdfConverter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.office.OfficeUtils;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.ExternalOfficeManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/**
 * 基于 JODConverter 的 Office → PDF 转换实现。
 *
 * 连接方式：JODConverter 连接「外部已启动的」LibreOffice 监听实例（socket 8100），
 * 而不是自己在业务进程内拉起 soffice——这样转换的内存/CPU 压力在 LibreOffice 侧，
 * 不会挤占 app 容器的 768m。
 *
 * 启用条件：watermark.converter.enabled=true。未启用时该 Bean 不存在，
 * 调用方（FileWatermarkService）自动降级为返回原文件。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "watermark.converter", name = "enabled", havingValue = "true")
public class JodDocumentToPdfConverter implements DocumentToPdfConverter {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "rtf", "odt", "ods", "odp", "txt"
    );

    private final WatermarkProperties properties;

    private volatile OfficeManager officeManager;
    private volatile DocumentConverter converter;

    public JodDocumentToPdfConverter(WatermarkProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        String host = properties.getConverter().getHost();
        int port = properties.getConverter().getPort();
        try {
            officeManager = ExternalOfficeManager.builder()
                    .hostName(host)
                    .portNumbers(port)
                    .connectOnStart(Boolean.TRUE)
                    .taskExecutionTimeout(properties.getConverter().getTimeoutMillis())
                    .build();
            officeManager.start();
            converter = LocalConverter.make(officeManager);
            log.info("LibreOffice 转换服务已连接: {}:{}", host, port);
        } catch (Exception e) {
            // 连不上不让服务起不来：转换器置空，Office 文件降级为原文件下载
            log.error("连接 LibreOffice 转换服务失败 {}:{}，Office 文件将降级为原文件下载", host, port, e);
            converter = null;
        }
    }

    @PreDestroy
    public void destroy() {
        if (officeManager != null) {
            OfficeUtils.stopQuietly(officeManager);
        }
    }

    @Override
    public boolean supports(String fileType) {
        if (converter == null || fileType == null) {
            return false;
        }
        return SUPPORTED_TYPES.contains(fileType.toLowerCase(Locale.ROOT));
    }

    @Override
    public byte[] convertToPdf(byte[] sourceBytes, String fileType) {
        if (converter == null) {
            throw new IllegalStateException("LibreOffice 转换服务不可用，无法转换 " + fileType);
        }
        Path src = null;
        Path dst = null;
        try {
            src = Files.createTempFile("wm-src-", "." + fileType);
            dst = Files.createTempFile("wm-dst-", ".pdf");
            Files.write(src, sourceBytes);
            converter.convert(src.toFile()).to(dst.toFile()).execute();
            byte[] pdf = Files.readAllBytes(dst);
            if (pdf.length == 0) {
                throw new IllegalStateException("转换结果为空");
            }
            return pdf;
        } catch (Exception e) {
            log.error("Office 转 PDF 失败: type={}", fileType, e);
            throw new IllegalStateException("Office 转 PDF 失败: " + e.getMessage(), e);
        } finally {
            deleteQuietly(src);
            deleteQuietly(dst);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("清理临时文件失败: {}", path, e);
        }
    }
}
