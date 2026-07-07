package com.haifeng.app.controller.algorithm.pdf;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordDetailVO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordListVO;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.response.R;
import com.haifeng.common.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/pdf")
@RequiredArgsConstructor
@RequireLogin
@RequireVip
public class PdfPlanController {

    private final PdfReportService pdfReportService;

    /**
     * 生成 PDF 报告（SSE 流式返回进度）
     */
    @PostMapping(value = "/generate/{planId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateReport(@PathVariable Integer planId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return pdfReportService.generateReport(userId, planId);
    }

    /**
     * 历史报告记录列表（分页）
     */
    @GetMapping("/records")
    public R<IPage<PdfRecordListVO>> pageRecords(@Valid PdfRecordQueryDTO dto) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return R.ok(pdfReportService.pageRecords(userId, dto));
    }

    /**
     * 报告记录详情
     */
    @GetMapping("/records/{recordId}")
    public R<PdfRecordDetailVO> getRecordDetail(@PathVariable Integer recordId) {
        Long userId = SecurityUtil.getCurrentMemberId();
        return R.ok(pdfReportService.getRecordDetail(userId, recordId));
    }
}
