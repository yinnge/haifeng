package com.haifeng.app.controller.algorithm.pdf;

import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.common.config.PdfPreviewConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 公开 PDF 预览端点（供 kkFileView 及其内置 pdf.js viewer 拉取报告 PDF）。
 * <p>鉴权方式：URL 中的 HMAC 签名 token + 有效期校验（无登录态）。
 * token 为【无状态多用途】签名——预览页目录跳页会用 #page=N 重建 viewer 地址，
 * 同一 token 在有效期内会被浏览器多次拉取，因此不做一次性消费。
 * 路径已在 SecurityConfig.WHITE_LIST 放行。
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/public/pdf")
@RequiredArgsConstructor
public class PdfPreviewController {

    private final PdfReportService pdfReportService;
    private final PdfPreviewConfig pdfPreviewConfig;

    @GetMapping("/preview/{recordId}")
    public ResponseEntity<byte[]> previewPdf(@PathVariable Integer recordId,
                                             @RequestParam String token,
                                             @RequestParam Long expire) {
        // 1. 签名验证
        String data = recordId + ":" + expire;
        String expectedSign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, pdfPreviewConfig.getSecret())
                .hmacHex(data);
        String expectedToken = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((data + ":" + expectedSign).getBytes(StandardCharsets.UTF_8));
        if (!expectedToken.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 2. 过期验证
        if (System.currentTimeMillis() > expire) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        // 3. 渲染 PDF（PdfRenderServiceImpl 内部按 recordId + updatedAt 做渲染缓存，
        //    目录跳页的重复拉取不会重复渲染）
        try {
            byte[] pdfBytes = pdfReportService.renderPdf(recordId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);
            headers.add("Content-Disposition", "inline; filename=\"report-" + recordId + ".pdf\"");
            // 浏览器缓存：pdf.js viewer 每次目录跳页重载都会重新拉取，命中缓存则近乎即时
            headers.setCacheControl("private, max-age=600");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("PDF preview rendering failed, recordId={}", recordId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
