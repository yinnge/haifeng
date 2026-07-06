package com.haifeng.app.service.impl.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.GaokaoArchiveVO;
import com.haifeng.app.vo.algorithm.pdf.*;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.enums.PdfReportStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.AiQuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfReportServiceImpl implements PdfReportService {

    private static final int MAP_MAX_CONCURRENCY = 3;

    private final PdfReportMapper pdfReportMapper;
    private final AiChatService aiChatService;
    private final AiQuotaService quotaService;
    private final WishPlanService wishPlanService;
    private final GaokaoArchiveService gaokaoArchiveService;
    private final ObjectMapper objectMapper;
    private final WishPlanMapper wishPlanMapper;

    @Override
    public Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        CompletableFuture.runAsync(() -> {
            try {
                doGenerate(userId, planId, sink);
            } catch (Exception e) {
                log.error("PDF report generation failed, userId={}, planId={}", userId, planId, e);
                sink.tryEmitNext(errorEvent(e.getMessage(), 500));
            } finally {
                sink.tryEmitComplete();
            }
        });

        return sink.asFlux();
    }

    private void doGenerate(Long userId, Integer planId,
                            Sinks.Many<ServerSentEvent<String>> sink) {
        // 1. 配额校验
        try {
            quotaService.incrAndCheck(userId);
        } catch (QuotaExceededException e) {
            sink.tryEmitNext(errorEvent("今日PDF生成次数已用完", 429));
            return;
        }

        // 2. 创建记录 status=GENERATING
        PdfReport report = PdfReport.builder()
                .memberId(userId)
                .planId(planId)
                .status(PdfReportStatus.GENERATING)
                .build();
        pdfReportMapper.insert(report);
        Integer recordId = report.getId();
        sink.tryEmitNext(sseEvent("{\"stage\":\"quota_checked\",\"recordId\":" + recordId + "}"));

        // 3. 查 wish_plan → 存 plan_snapshot
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || Boolean.TRUE.equals(wishPlan.getDeleted())) {
            updateReportFailed(recordId, "志愿方案不存在");
            sink.tryEmitNext(errorEvent("志愿方案不存在", 404));
            return;
        }

        PlanSnapshot snapshot = PlanSnapshot.builder()
                .planYear(wishPlan.getPlanYear())
                .planProvince(wishPlan.getPlanProvince())
                .reformModel(wishPlan.getReformModel())
                .userScore(wishPlan.getUserScore())
                .userRank(wishPlan.getUserRank())
                .planBatch(wishPlan.getPlanBatch())
                .build();

        // 4. 查可导出专业组
        List<ExportGroupContextVO> groups = wishPlanService.getExportGroupContexts(planId);
        if (groups == null || groups.isEmpty()) {
            updateReportFailed(recordId, "没有可导出的专业组");
            sink.tryEmitNext(errorEvent("没有可导出的专业组，请先在志愿方案中勾选导出专业", 400));
            return;
        }

        // 5. Map 阶段（限流并行）
        List<MapResultItem> mapResults = runMapPhase(userId, groups, sink);

        // 6. 存 map_results + plan_snapshot
        try {
            String mapJson = objectMapper.writeValueAsString(mapResults);
            report.setMapResults(mapJson);
            report.setPlanSnapshot(objectMapper.writeValueAsString(snapshot));
            pdfReportMapper.updateById(report);
        } catch (Exception e) {
            log.error("Failed to serialize map_results", e);
        }

        // 7. Reduce 阶段
        sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"running\"}"));

        String reduceJson = null;
        try {
            String reduceInput = buildReduceInput(mapResults);
            List<ChatMessage> reduceMessages = List.of(
                    new ChatMessage("system", buildReduceSystemPrompt()),
                    new ChatMessage("user", reduceInput)
            );
            String reduceResponse = aiChatService.chatSync(userId, reduceMessages);

            ReduceResult reduceResult = parseReduceResult(reduceResponse);
            reduceJson = objectMapper.writeValueAsString(reduceResult);
            sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"done\"}"));
        } catch (Exception e) {
            log.error("Reduce phase failed, recordId={}", recordId, e);
            updateReportFailed(recordId, "Reduce阶段失败: " + e.getMessage());
            sink.tryEmitNext(errorEvent("Reduce阶段失败", recordId, 500));
            return;
        }

        // 8. 更新 status=SUCCESS
        report.setReduceResult(reduceJson);
        report.setStatus(PdfReportStatus.SUCCESS);
        pdfReportMapper.updateById(report);

        // 9. 完成
        sink.tryEmitNext(sseEvent("{\"stage\":\"done\",\"recordId\":" + recordId + "}"));
    }

    private List<MapResultItem> runMapPhase(Long userId, List<ExportGroupContextVO> groups,
                                            Sinks.Many<ServerSentEvent<String>> sink) {
        int total = groups.size();
        Semaphore semaphore = new Semaphore(MAP_MAX_CONCURRENCY);
        ExecutorService executor = Executors.newFixedThreadPool(MAP_MAX_CONCURRENCY);

        List<CompletableFuture<MapResultItem>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int index = i;
            final ExportGroupContextVO group = groups.get(i);

            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        sink.tryEmitNext(sseEvent(
                                "{\"stage\":\"map\",\"current\":" + (index + 1) +
                                ",\"total\":" + total +
                                ",\"university\":\"" + escapeJson(group.getGroupName()) + "\"}"));

                        return callMapAI(userId, group);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return MapResultItem.builder()
                            .success(false)
                            .commentary(null)
                            .build();
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        sink.tryEmitNext(sseEvent("{\"stage\":\"map_done\"}"));

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private MapResultItem callMapAI(Long userId, ExportGroupContextVO group) {
        List<MapResultItem.MajorBrief> majors = group.getExportableMajors().stream()
                .map(m -> MapResultItem.MajorBrief.builder()
                        .majorName(m.getMajorId() != null ? "专业" + m.getMajorId() : "未知专业")
                        .safetyLevel(m.getSafetyLevel())
                        .levelShort(m.getLevelShort())
                        .build())
                .collect(Collectors.toList());

        try {
            String mapInput = buildMapInput(group, majors);
            List<ChatMessage> messages = List.of(
                    new ChatMessage("system", buildMapSystemPrompt()),
                    new ChatMessage("user", mapInput)
            );
            String commentary = aiChatService.chatSync(userId, messages);

            return MapResultItem.builder()
                    .universityId(group.getUniversityId())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .majors(majors)
                    .commentary(commentary)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("Map AI call failed for group {}, university {}: {}",
                    group.getGroupSnapshotId(), group.getUniversityId(), e.getMessage());
            return MapResultItem.builder()
                    .universityId(group.getUniversityId())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .majors(majors)
                    .commentary(null)
                    .success(false)
                    .build();
        }
    }

    private String buildMapInput(ExportGroupContextVO group, List<MapResultItem.MajorBrief> majors) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"university\":\"").append(escapeJson(group.getGroupName()))
          .append("\",\"city\":\"").append(escapeJson(group.getCityName()))
          .append("\",\"majors\":[");
        for (int i = 0; i < majors.size(); i++) {
            MapResultItem.MajorBrief m = majors.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(escapeJson(m.getMajorName()))
              .append("\",\"safetyLevel\":").append(m.getSafetyLevel() != null ? m.getSafetyLevel() : "0")
              .append(",\"levelShort\":\"").append(escapeJson(m.getLevelShort()))
              .append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildMapSystemPrompt() {
        return """
            你是一位资深高考志愿规划师。请根据提供的大学、城市和专业信息，给出300字以内的客观研判。
            要求：
            1. 结合该校该专业在该城市的产业地缘优势或劣势
            2. 结合行业发展趋势给出前瞻性判断
            3. 不要罗列数据，只给结论性观点
            4. 严格控制在300字以内
            """;
    }

    private String buildReduceInput(List<MapResultItem> mapResults) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < mapResults.size(); i++) {
            MapResultItem item = mapResults.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"大学\":\"").append(escapeJson(item.getGroupName()))
              .append("\",\"城市\":\"").append(escapeJson(item.getCityName()))
              .append("\",\"专业\":[");
            List<MapResultItem.MajorBrief> majors = item.getMajors();
            if (majors != null) {
                for (int j = 0; j < majors.size(); j++) {
                    if (j > 0) sb.append(",");
                    sb.append("\"").append(escapeJson(majors.get(j).getMajorName())).append("\"");
                }
            }
            sb.append("],\"录取概率\":\"");
            if (majors != null && !majors.isEmpty()) {
                sb.append(majors.stream()
                        .map(m -> m.getLevelShort() != null ? m.getLevelShort() : "")
                        .distinct()
                        .collect(Collectors.joining("/")));
            }
            sb.append("\",\"AI简评\":\"")
              .append(item.getCommentary() != null ? escapeJson(item.getCommentary()) : "暂无简评")
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildReduceSystemPrompt() {
        return """
            你是海枫未来规划院的首席志愿规划专家。请根据提供的各大学AI简评浓缩数据，进行全局博弈分析。
            请输出以下三个部分（用 === 分隔）：
            1. 全局宏观全景研判：分析哪些属于高风险高收益，哪些属于性价比之王
            2. SWOT象限分析：城市地域红利VS学校名气光环的博弈辩证
            3. 海枫强烈推荐填报梯队顺序：综合考虑概率、城市、行业，给出排兵布阵建议

            要求：不要重复各校的简评内容，只做交叉对比和全局统筹。
            """;
    }

    private ReduceResult parseReduceResult(String response) {
        if (response == null || response.isBlank()) {
            return ReduceResult.builder()
                    .globalAnalysis("")
                    .swot("")
                    .recommendation("")
                    .build();
        }
        String[] parts = response.split("===", 3);
        return ReduceResult.builder()
                .globalAnalysis(parts.length > 0 ? parts[0].trim() : "")
                .swot(parts.length > 1 ? parts[1].trim() : "")
                .recommendation(parts.length > 2 ? parts[2].trim() : "")
                .build();
    }

    private void updateReportFailed(Integer recordId, String reason) {
        PdfReport update = new PdfReport();
        update.setId(recordId);
        update.setStatus(PdfReportStatus.FAILED);
        update.setFailReason(reason);
        pdfReportMapper.updateById(update);
    }

    private ServerSentEvent<String> sseEvent(String data) {
        return ServerSentEvent.<String>builder().data(data).build();
    }

    private ServerSentEvent<String> errorEvent(String message, int code) {
        return sseEvent("{\"stage\":\"error\",\"message\":\"" + escapeJson(message) + "\",\"code\":" + code + "}");
    }

    private ServerSentEvent<String> errorEvent(String message, Integer recordId, int code) {
        return sseEvent("{\"stage\":\"error\",\"message\":\"" + escapeJson(message) +
                "\",\"recordId\":" + recordId + ",\"code\":" + code + "}");
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    // ===================== 历史记录查询 =====================

    @Override
    public IPage<PdfRecordListVO> pageRecords(Long userId, PdfRecordQueryDTO dto) {
        Page<PdfReport> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<PdfReport> wrapper = new LambdaQueryWrapper<PdfReport>()
                .eq(PdfReport::getMemberId, userId)
                .orderByDesc(PdfReport::getCreatedAt);

        IPage<PdfReport> result = pdfReportMapper.selectPage(page, wrapper);

        return result.convert(report -> PdfRecordListVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .status(report.getStatus() != null ? report.getStatus().getValue() : null)
                .createdAt(report.getCreatedAt())
                .build());
    }

    @Override
    public PdfRecordDetailVO getRecordDetail(Long userId, Integer recordId) {
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId()) || Boolean.TRUE.equals(report.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报告记录不存在");
        }
        return PdfRecordDetailVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .status(report.getStatus() != null ? report.getStatus().getValue() : null)
                .mapResults(report.getMapResults())
                .reduceResult(report.getReduceResult())
                .planSnapshot(report.getPlanSnapshot())
                .failReason(report.getFailReason())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
