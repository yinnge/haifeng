package com.haifeng.app.controller.algorithm.pdf;

import com.haifeng.app.dto.algorithm.pdf.AiChatRequestDTO;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.pdf.PdfPlanService;
import com.haifeng.common.annotation.RequireLogin;
import com.haifeng.common.annotation.RequireVip;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.R;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/api/v1/app/algorithm/pdf")
@RequiredArgsConstructor
@RequireLogin
@RequireVip
public class PdfPlanController {

    private final AiChatService aiChatService;
    private final PdfPlanService pdfPlanService;

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@Valid @RequestBody AiChatRequestDTO dto) {
        Long userId = SecurityUtil.getCurrentMemberId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return aiChatService.streamChat(userId, dto);
    }

    /**
     * 大学维度 PDF 导出
     * <p>前端需先调 {@code POST /api/v1/app/algorithm/wish-plan/{planId}/export/save} 持久化 is_exported 状态。
     */
    @PostMapping("/plan/{planId}/university")
    public R<Object> exportUniversity(@PathVariable Integer planId) {
        // TODO 任务二：SSE 流式返回 + PDF 生成（接入多智能体编排）
        return R.ok(pdfPlanService.exportUniversityPdf(planId));
    }

    /**
     * 城市维度 PDF 导出
     * <p>前端需先调 {@code POST /api/v1/app/algorithm/wish-plan/{planId}/export/save} 持久化 is_exported 状态。
     */
    @PostMapping("/plan/{planId}/city")
    public R<Object> exportCity(@PathVariable Integer planId) {
        // TODO 任务二：SSE 流式返回 + PDF 生成（接入多智能体编排）
        return R.ok(pdfPlanService.exportCityPdf(planId));
    }

    /**
     * 专业维度 PDF 导出
     * <p>前端需先调 {@code POST /api/v1/app/algorithm/wish-plan/{planId}/export/save} 持久化 is_exported 状态。
     */
    @PostMapping("/plan/{planId}/major")
    public R<Object> exportMajor(@PathVariable Integer planId) {
        // TODO 任务二：SSE 流式返回 + PDF 生成（接入多智能体编排）
        return R.ok(pdfPlanService.exportMajorPdf(planId));
    }
}
