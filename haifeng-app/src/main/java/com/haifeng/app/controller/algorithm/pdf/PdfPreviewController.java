package com.haifeng.app.controller.algorithm.pdf;

import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.common.config.PdfPreviewConfig;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/pdf")
@RequiredArgsConstructor
public class PdfPreviewController {

    private final PdfReportService pdfReportService;
    private final PdfPreviewConfig pdfPreviewConfig;
    private final StringRedisTemplate redisTemplate;

    /**
     * 公开 PDF 预览端点（无鉴权，token + Redis 一次性校验）
     */
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

        // 3. Redis 一次性消费验证（用完即删）
        String redisKey = RedisKeyConstant.getPdfPreviewTokenKey(token);
        String storedRecordId = redisTemplate.opsForValue().get(redisKey);
        if (storedRecordId == null || !String.valueOf(recordId).equals(storedRecordId)) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        redisTemplate.delete(redisKey);

        // 4. 渲染 PDF
        try {
            byte[] pdfBytes = pdfReportService.renderPdf(recordId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentLength(pdfBytes.length);
            headers.add("Content-Disposition", "inline; filename=\"report-" + recordId + ".pdf\"");
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("PDF preview rendering failed, recordId={}", recordId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
