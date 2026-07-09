package com.haifeng.app.service.impl.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.pdf.PdfRenderService;
import com.haifeng.app.service.algorithm.pdf.PdfReportService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
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
import java.util.Map;
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
    private final ObjectMapper objectMapper;
    private final WishPlanMapper wishPlanMapper;
    private final PdfRenderService pdfRenderService;

    @Override
    public Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().multicast().onBackpressureBuffer();

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
        log.info("PDF report generation started, userId={}, planId={}, recordId={}", userId, planId, recordId);
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
            String reduceInput = buildReduceInput(mapResults, groups);
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
        log.info("PDF report generation completed, recordId={}, planId={}", recordId, planId);

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

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        sink.tryEmitNext(sseEvent("{\"stage\":\"map_done\"}"));

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private MapResultItem callMapAI(Long userId, ExportGroupContextVO group) {
        List<MapResultItem.MajorBrief> majors = group.getExportableMajors().stream()
                .map(m -> {
                    MapResultItem.MajorBrief.MajorBriefBuilder builder = MapResultItem.MajorBrief.builder()
                            .majorName(m.getMajorName() != null ? m.getMajorName() : "未知专业")
                            .safetyLevel(m.getSafetyLevel())
                            .levelShort(m.getLevelShort());

                    if (m.getMajorEnrichment() != null) {
                        builder.employmentRate(m.getMajorEnrichment().getEmploymentRate())
                                .salaryMin(m.getMajorEnrichment().getSalaryMin())
                                .salaryMax(m.getMajorEnrichment().getSalaryMax())
                                .majorCategory(m.getMajorEnrichment().getMajorCategory());
                        // careerProspect 截断80字
                        String prospect = m.getMajorEnrichment().getCareerProspect();
                        if (prospect != null && prospect.length() > 80) {
                            prospect = prospect.substring(0, 80);
                        }
                        builder.careerProspect(prospect);
                    }

                    return builder.build();
                })
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
                    .universityName(group.getUniversityName())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .groupSnapshotId(group.getGroupSnapshotId())
                    .majors(majors)
                    .commentary(commentary)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.warn("Map AI call failed for group {}, university {}: {}",
                    group.getGroupSnapshotId(), group.getUniversityId(), e.getMessage());
            return MapResultItem.builder()
                    .universityId(group.getUniversityId())
                    .universityName(group.getUniversityName())
                    .cityName(group.getCityName())
                    .groupName(group.getGroupName())
                    .groupSnapshotId(group.getGroupSnapshotId())
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
          .append("\"");

        // cityInfo
        if (group.getCityEnrichment() != null) {
            CityEnrichmentVO ci = group.getCityEnrichment();
            sb.append(",\"cityInfo\":{");
            sb.append("\"mainIndustries\":");
            sb.append(ci.getMainIndustries() != null ? jsonArray(ci.getMainIndustries()) : "[]");
            sb.append(",\"gdp\":").append(ci.getGdp() != null ? ci.getGdp() : "null");
            sb.append(",\"gdpGrowthRate\":").append(ci.getGdpGrowthRate() != null ? ci.getGdpGrowthRate() : "null");
            sb.append(",\"fortune500Count\":").append(ci.getFortune500Count() != null ? ci.getFortune500Count() : "null");
            sb.append(",\"avgSalary\":").append(ci.getAvgSalary() != null ? ci.getAvgSalary() : "null");
            sb.append("}");
        }

        sb.append(",\"majors\":[");
        for (int i = 0; i < majors.size(); i++) {
            MapResultItem.MajorBrief m = majors.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"").append(escapeJson(m.getMajorName()))
              .append("\",\"safetyLevel\":").append(m.getSafetyLevel() != null ? m.getSafetyLevel() : "0")
              .append(",\"levelShort\":\"").append(escapeJson(m.getLevelShort())).append("\"");

            if (m.getEmploymentRate() != null) {
                sb.append(",\"employmentRate\":").append(m.getEmploymentRate());
            }
            if (m.getSalaryMin() != null || m.getSalaryMax() != null) {
                String range = (m.getSalaryMin() != null ? m.getSalaryMin() : "?")
                        + "-" + (m.getSalaryMax() != null ? m.getSalaryMax() : "?");
                sb.append(",\"salaryRange\":\"").append(range).append("\"");
            }
            if (m.getMajorCategory() != null) {
                sb.append(",\"category\":\"").append(escapeJson(m.getMajorCategory())).append("\"");
            }
            if (m.getCareerProspect() != null) {
                sb.append(",\"careerProspect\":\"").append(escapeJson(m.getCareerProspect())).append("\"");
            }
            sb.append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String buildMapSystemPrompt() {
        return """
            你是一位资深高考志愿规划师。请根据提供的大学、城市和专业信息，给出300字以内的客观研判。
            要求：
            1. 结合该校该专业在该城市的产业地缘优势或劣势，参考 cityInfo 中的 mainIndustries 和 gdp 数据
            2. 结合行业发展趋势给出前瞻性判断，参考专业的 employmentRate 和 salaryRange 数据
            3. 若 cityInfo 或专业就业数据为 null，则基于常识判断
            4. 不要罗列数据，只给结论性观点
            5. 严格控制在300字以内
            6. 使用Markdown格式输出（可使用**加粗**、- 列表等）
            """;
    }

    private String buildReduceInput(List<MapResultItem> mapResults, List<ExportGroupContextVO> groups) {
        // 按 groupSnapshotId 索引 cityEnrichment 和 majorEnrichment
        Map<Integer, ExportGroupContextVO> groupMap = groups.stream()
                .collect(Collectors.toMap(ExportGroupContextVO::getGroupSnapshotId, g -> g, (a, b) -> a));

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
                    MapResultItem.MajorBrief m = majors.get(j);
                    sb.append("{\"name\":\"").append(escapeJson(m.getMajorName())).append("\"");
                    if (m.getEmploymentRate() != null) {
                        sb.append(",\"就业率\":").append(m.getEmploymentRate());
                    }
                    if (m.getSalaryMin() != null || m.getSalaryMax() != null) {
                        String range = (m.getSalaryMin() != null ? m.getSalaryMin() : "?")
                                + "-" + (m.getSalaryMax() != null ? m.getSalaryMax() : "?");
                        sb.append(",\"薪资\":\"").append(range).append("\"");
                    }
                    sb.append("}");
                }
            }
            sb.append("],\"录取概率\":\"");
            if (majors != null && !majors.isEmpty()) {
                sb.append(majors.stream()
                        .map(m -> m.getLevelShort() != null ? m.getLevelShort() : "")
                        .distinct()
                        .collect(Collectors.joining("/")));
            }
            sb.append("\"");

            // 城市产业信息
            ExportGroupContextVO group = item.getGroupSnapshotId() != null
                    ? groupMap.get(item.getGroupSnapshotId()) : null;
            if (group != null && group.getCityEnrichment() != null) {
                CityEnrichmentVO ci = group.getCityEnrichment();
                if (ci.getMainIndustries() != null && !ci.getMainIndustries().isEmpty()) {
                    sb.append(",\"城市产业\":").append(jsonArray(ci.getMainIndustries()));
                }
            }

            sb.append(",\"AI简评\":\"")
              .append(item.getCommentary() != null ? escapeJson(item.getCommentary()) : "暂无简评")
              .append("\"}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String buildReduceSystemPrompt() {
        return """
            你是海枫未来规划院的首席志愿规划专家。请根据提供的各大学AI简评浓缩数据，进行全局博弈分析。
            数据中包含城市产业信息（城市产业）和专业就业数据（就业率、薪资），请在分析中参考这些数据。
            请输出以下三个部分，每部分用 Markdown ## 标题分隔：
            ## 全局宏观全景研判
            分析哪些属于高风险高收益，哪些属于性价比之王

            ## SWOT象限分析
            城市地域红利VS学校名气光环的博弈辩证

            ## 海枫强烈推荐填报梯队顺序
            综合考虑概率、城市、行业，给出排兵布阵建议

            要求：
            1. 不要重复各校的简评内容，只做交叉对比和全局统筹
            2. 使用Markdown格式输出（可使用**加粗**、- 列表、表格等）
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

        // 优先按 ## 标题分割（新格式）
        if (response.contains("## ")) {
            String globalAnalysis = extractSection(response, "全局宏观全景研判");
            String swot = extractSection(response, "SWOT象限分析");
            String recommendation = extractSection(response, "海枫强烈推荐填报梯队顺序");
            return ReduceResult.builder()
                    .globalAnalysis(globalAnalysis)
                    .swot(swot)
                    .recommendation(recommendation)
                    .build();
        }

        // fallback: 旧格式用 === 分割
        String[] parts = response.split("={3,}", 3);
        return ReduceResult.builder()
                .globalAnalysis(parts.length > 0 ? parts[0].trim() : "")
                .swot(parts.length > 1 ? parts[1].trim() : "")
                .recommendation(parts.length > 2 ? parts[2].trim() : "")
                .build();
    }

    /**
     * 从 Markdown 响应中提取指定 ## 标题下的内容（到下一个 ## 或文本末尾）
     */
    private String extractSection(String text, String sectionTitle) {
        // 匹配 ## {sectionTitle} 开始，到下一个 ## 或文本结束
        String[] lines = text.split("\n");
        StringBuilder content = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            if (line.trim().startsWith("## ")) {
                if (inSection) {
                    break; // 遇到下一个 ## 标题，结束当前段
                }
                if (line.contains(sectionTitle)) {
                    inSection = true;
                    continue; // 跳过标题行本身
                }
            } else if (inSection) {
                content.append(line).append("\n");
            }
        }
        return content.toString().trim();
    }

    private void updateReportFailed(Integer recordId, String reason) {
        log.warn("PDF report marked as failed, recordId={}, reason={}", recordId, reason);
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
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String jsonArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
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

    @Override
    public byte[] renderPdf(Long userId, Integer recordId) {
        return pdfRenderService.renderPdf(userId, recordId);
    }
}
