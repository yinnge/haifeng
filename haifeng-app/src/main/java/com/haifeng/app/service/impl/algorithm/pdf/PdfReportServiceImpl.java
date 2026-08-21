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
import com.haifeng.common.entity.algorithm.MemberGaokao;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.enums.PdfReportStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.exception.QuotaExceededException;
import com.haifeng.common.mapper.algorithm.MemberGaokaoMapper;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.AiQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdfReportServiceImpl implements PdfReportService {

    /**
     * Map 阶段全部完成的最大等待时间：3 分钟。
     * 覆盖单组 AI 调用 60s 超时 × 多组串行 fallback 场景，避免无限阻塞。
     */
    private static final long MAP_ALL_OF_TIMEOUT_SECONDS = 180L;

    private final PdfReportMapper pdfReportMapper;
    private final AiChatService aiChatService;
    private final AiQuotaService quotaService;
    private final WishPlanService wishPlanService;
    private final ObjectMapper objectMapper;
    private final WishPlanMapper wishPlanMapper;
    private final MemberGaokaoMapper memberGaokaoMapper;
    private final PdfRenderService pdfRenderService;
    private final ChartService chartService;
    private final ExecutorService pdfMapExecutor;

    public PdfReportServiceImpl(PdfReportMapper pdfReportMapper,
                                AiChatService aiChatService,
                                AiQuotaService quotaService,
                                WishPlanService wishPlanService,
                                ObjectMapper objectMapper,
                                WishPlanMapper wishPlanMapper,
                                MemberGaokaoMapper memberGaokaoMapper,
                                PdfRenderService pdfRenderService,
                                ChartService chartService,
                                @Qualifier("pdfMapExecutor") ExecutorService pdfMapExecutor) {
        this.pdfReportMapper = pdfReportMapper;
        this.aiChatService = aiChatService;
        this.quotaService = quotaService;
        this.wishPlanService = wishPlanService;
        this.objectMapper = objectMapper;
        this.wishPlanMapper = wishPlanMapper;
        this.memberGaokaoMapper = memberGaokaoMapper;
        this.pdfRenderService = pdfRenderService;
        this.chartService = chartService;
        this.pdfMapExecutor = pdfMapExecutor;
    }

    @Override
    public Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        CompletableFuture.runAsync(() -> {
            try {
                doGenerate(userId, planId, sink, null);
            } catch (Exception e) {
                log.error("PDF report generation failed, userId={}, planId={}", userId, planId, e);
                sink.tryEmitNext(errorEvent(e.getMessage(), 500));
            } finally {
                sink.tryEmitComplete();
            }
        }, pdfMapExecutor);

        return sink.asFlux();
    }

    @Override
    public Flux<ServerSentEvent<String>> regenerateReport(Long userId, Integer recordId) {
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        CompletableFuture.runAsync(() -> {
            try {
                doRegenerate(userId, recordId, sink);
            } catch (Exception e) {
                log.error("PDF report regeneration failed, userId={}, recordId={}", userId, recordId, e);
                sink.tryEmitNext(errorEvent(e.getMessage(), 500));
            } finally {
                sink.tryEmitComplete();
            }
        }, pdfMapExecutor);

        return sink.asFlux();
    }

    private void doRegenerate(Long userId, Integer recordId,
                              Sinks.Many<ServerSentEvent<String>> sink) {
        // 1. 校验记录
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId())) {
            sink.tryEmitNext(errorEvent("报告记录不存在", 404));
            return;
        }
        if (report.getStatus() == PdfReportStatus.GENERATING) {
            sink.tryEmitNext(errorEvent("报告正在生成中，请稍后重试", 400));
            return;
        }

        // 2. 配额处理：失败不扣配额，成功扣配额
        boolean isFailed = report.getStatus() == PdfReportStatus.FAILED;
        if (!isFailed) {
            try {
                quotaService.incrAndCheck(userId);
            } catch (QuotaExceededException e) {
                sink.tryEmitNext(errorEvent("今日PDF生成次数已用完", 429));
                return;
            }
        }

        // 3. 重置记录
        report.setStatus(PdfReportStatus.GENERATING);
        report.setMapResults(null);
        report.setReduceResult(null);
        report.setFailReason(null);
        report.setPlanSnapshot(null);
        pdfReportMapper.updateById(report);

        log.info("PDF report regeneration started, userId={}, recordId={}, isFailed={}",
                userId, recordId, isFailed);
        sink.tryEmitNext(sseEvent("{\"stage\":\"quota_checked\",\"recordId\":" + recordId + "}"));

        // 4. 复用生成核心逻辑
        doGenerate(userId, report.getPlanId(), sink, report);
    }

    private void doGenerate(Long userId, Integer planId,
                            Sinks.Many<ServerSentEvent<String>> sink,
                            PdfReport existingReport) {
        // 1. 配额校验（仅新建记录时）
        boolean quotaConsumed = existingReport != null
                && existingReport.getStatus() != PdfReportStatus.FAILED;
        if (existingReport == null) {
            try {
                quotaService.incrAndCheck(userId);
            } catch (QuotaExceededException e) {
                sink.tryEmitNext(errorEvent("今日PDF生成次数已用完", 429));
                return;
            }
            quotaConsumed = true;
        }

        // 2. 创建或复用记录
        PdfReport report;
        Integer recordId;
        if (existingReport != null) {
            report = existingReport;
            recordId = report.getId();
        } else {
            report = PdfReport.builder()
                    .memberId(userId)
                    .planId(planId)
                    .status(PdfReportStatus.GENERATING)
                    .build();
            pdfReportMapper.insert(report);
            recordId = report.getId();
            log.info("PDF report generation started, userId={}, planId={}, recordId={}", userId, planId, recordId);
            sink.tryEmitNext(sseEvent("{\"stage\":\"quota_checked\",\"recordId\":" + recordId + "}"));
        }

        // 3. 查 wish_plan → 存 plan_snapshot
        WishPlan wishPlan = wishPlanMapper.selectById(planId);
        if (wishPlan == null || Boolean.TRUE.equals(wishPlan.getDeleted())) {
            updateReportFailed(recordId, "志愿方案不存在");
            if (quotaConsumed) quotaService.decr(userId);
            sink.tryEmitNext(errorEvent("志愿方案不存在", 404));
            return;
        }

        // 3.1 加载用户高考档案（可选）
        MemberGaokao memberGaokao = memberGaokaoMapper.selectByMemberId(userId);
        MemberGaokaoContextVO memberProfile = buildMemberProfile(memberGaokao);

        PlanSnapshot snapshot = PlanSnapshot.builder()
                .planYear(wishPlan.getPlanYear())
                .planProvince(wishPlan.getPlanProvince())
                .reformModel(wishPlan.getReformModel())
                .userScore(wishPlan.getUserScore())
                .userRank(wishPlan.getUserRank())
                .planBatch(wishPlan.getPlanBatch())
                .memberProfile(memberProfile)
                .build();

        // 4. 查可导出专业组
        List<ExportGroupContextVO> groups = wishPlanService.getExportGroupContexts(planId);
        if (groups == null || groups.isEmpty()) {
            updateReportFailed(recordId, "没有可导出的专业组");
            if (quotaConsumed) quotaService.decr(userId);
            sink.tryEmitNext(errorEvent("没有可导出的专业组，请先在志愿方案中勾选导出专业", 400));
            return;
        }

        // 5. Map 阶段（限流并行）
        List<MapResultItem> mapResults = runMapPhase(userId, groups, memberProfile, sink);

        // 6. 存 map_results + plan_snapshot
        try {
            String mapJson = objectMapper.writeValueAsString(mapResults);
            report.setMapResults(mapJson);
            report.setPlanSnapshot(objectMapper.writeValueAsString(snapshot));
            pdfReportMapper.updateById(report);
        } catch (Exception e) {
            log.error("Failed to serialize map_results, recordId={}", recordId, e);
            updateReportFailed(recordId, "Map结果序列化失败: " + e.getMessage());
            if (quotaConsumed) quotaService.decr(userId);
            sink.tryEmitNext(errorEvent("Map结果序列化失败", recordId, 500));
            return;
        }

        // 7. Reduce 阶段
        sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"running\"}"));

        String reduceJson = null;
        try {
            String reduceInput = buildReduceInput(mapResults, groups, memberProfile);
            List<ChatMessage> reduceMessages = List.of(
                    new ChatMessage("system", buildReduceSystemPrompt()),
                    new ChatMessage("user", reduceInput)
            );
            String reduceResponse = aiChatService.chatSync(userId, reduceMessages);

            ReduceResult reduceResult = parseReduceResult(reduceResponse);

            // 生成优先级图表
            if (reduceResult.getMacroAnalysis() != null
                    && reduceResult.getMacroAnalysis().getComprehensiveAnalysis() != null
                    && reduceResult.getMacroAnalysis().getComprehensiveAnalysis().getRanking() != null) {
                String chartBase64 = chartService.generateRankingChart(
                        reduceResult.getMacroAnalysis().getComprehensiveAnalysis().getRanking());
                reduceResult.getMacroAnalysis().getComprehensiveAnalysis().setChartBase64(chartBase64);
            }

            // M8: 若 Reduce 三段内容全空，视为失败
            if (isReduceResultEmpty(reduceResult)) {
                log.warn("Reduce result is empty, recordId={}", recordId);
                updateReportFailed(recordId, "Reduce阶段返回空内容");
                if (quotaConsumed) quotaService.decr(userId);
                sink.tryEmitNext(errorEvent("Reduce阶段返回空内容，请稍后重试", recordId, 500));
                return;
            }

            reduceJson = objectMapper.writeValueAsString(reduceResult);
            sink.tryEmitNext(sseEvent("{\"stage\":\"reduce\",\"status\":\"done\"}"));
        } catch (Exception e) {
            log.error("Reduce phase failed, recordId={}", recordId, e);
            updateReportFailed(recordId, "Reduce阶段失败: " + e.getMessage());
            if (quotaConsumed) quotaService.decr(userId);
            sink.tryEmitNext(errorEvent("Reduce阶段失败", recordId, 500));
            return;
        }

        // 8. 更新 status=SUCCESS
        try {
            report.setReduceResult(reduceJson);
            report.setStatus(PdfReportStatus.SUCCESS);
            pdfReportMapper.updateById(report);
        } catch (Exception e) {
            log.error("Failed to save final result, recordId={}", recordId, e);
            updateReportFailed(recordId, "保存最终结果失败: " + e.getMessage());
            if (quotaConsumed) quotaService.decr(userId);
            sink.tryEmitNext(errorEvent("保存最终结果失败", recordId, 500));
            return;
        }
        log.info("PDF report generation completed, recordId={}, planId={}", recordId, planId);

        // 9. 完成
        sink.tryEmitNext(sseEvent("{\"stage\":\"done\",\"recordId\":" + recordId + "}"));
    }

    private List<MapResultItem> runMapPhase(Long userId, List<ExportGroupContextVO> groups,
                                            MemberGaokaoContextVO memberProfile,
                                            Sinks.Many<ServerSentEvent<String>> sink) {
        int total = groups.size();

        List<CompletableFuture<MapResultItem>> futures = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            final int index = i;
            final ExportGroupContextVO group = groups.get(i);

            sink.tryEmitNext(sseEvent(
                    "{\"stage\":\"map\",\"current\":" + (index + 1) +
                    ",\"total\":" + total +
                    ",\"university\":\"" + escapeJson(group.getGroupName()) + "\"}"));

            futures.add(CompletableFuture.supplyAsync(() -> callMapAI(userId, group, memberProfile), pdfMapExecutor));
        }

        try {
            // H2: 为 allOf 加超时，避免 AI 调用挂起导致整条链路无限阻塞
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .orTimeout(MAP_ALL_OF_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .join();
        } catch (java.util.concurrent.CompletionException e) {
            log.error("Map phase timed out or failed after {}s", MAP_ALL_OF_TIMEOUT_SECONDS, e);
        }

        sink.tryEmitNext(sseEvent("{\"stage\":\"map_done\"}"));

        return futures.stream()
                .map(f -> {
                    try {
                        return f.join();
                    } catch (Exception e) {
                        log.warn("Map future join failed: {}", e.getMessage());
                        return MapResultItem.builder()
                                .success(false)
                                .commentary(null)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    private MapResultItem callMapAI(Long userId, ExportGroupContextVO group,
                                    MemberGaokaoContextVO memberProfile) {
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
            String mapInput = buildMapInput(group, majors, memberProfile);
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

    private String buildMapInput(ExportGroupContextVO group, List<MapResultItem.MajorBrief> majors,
                                 MemberGaokaoContextVO memberProfile) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
            root.put("university", group.getGroupName());
            root.put("city", group.getCityName());

            if (group.getCityEnrichment() != null) {
                CityEnrichmentVO ci = group.getCityEnrichment();
                com.fasterxml.jackson.databind.node.ObjectNode cityInfo = objectMapper.createObjectNode();
                if (ci.getMainIndustries() != null) {
                    cityInfo.set("mainIndustries", objectMapper.valueToTree(ci.getMainIndustries()));
                } else {
                    cityInfo.set("mainIndustries", objectMapper.createArrayNode());
                }
                if (ci.getGdp() != null) cityInfo.put("gdp", ci.getGdp());
                else cityInfo.putNull("gdp");
                if (ci.getGdpGrowthRate() != null) cityInfo.put("gdpGrowthRate", ci.getGdpGrowthRate());
                else cityInfo.putNull("gdpGrowthRate");
                if (ci.getFortune500Count() != null) cityInfo.put("fortune500Count", ci.getFortune500Count());
                else cityInfo.putNull("fortune500Count");
                if (ci.getAvgSalary() != null) cityInfo.put("avgSalary", ci.getAvgSalary());
                else cityInfo.putNull("avgSalary");
                root.set("cityInfo", cityInfo);
            }

            com.fasterxml.jackson.databind.node.ArrayNode majorsNode = objectMapper.createArrayNode();
            for (MapResultItem.MajorBrief m : majors) {
                com.fasterxml.jackson.databind.node.ObjectNode mNode = objectMapper.createObjectNode();
                mNode.put("name", m.getMajorName());
                mNode.put("safetyLevel", m.getSafetyLevel() != null ? m.getSafetyLevel().doubleValue() : 0);
                mNode.put("levelShort", m.getLevelShort());
                if (m.getEmploymentRate() != null) mNode.put("employmentRate", m.getEmploymentRate());
                if (m.getSalaryMin() != null || m.getSalaryMax() != null) {
                    String range = (m.getSalaryMin() != null ? m.getSalaryMin() : "?")
                            + "-" + (m.getSalaryMax() != null ? m.getSalaryMax() : "?");
                    mNode.put("salaryRange", range);
                }
                if (m.getMajorCategory() != null) mNode.put("category", m.getMajorCategory());
                if (m.getCareerProspect() != null) mNode.put("careerProspect", m.getCareerProspect());
                majorsNode.add(mNode);
            }
            root.set("majors", majorsNode);

            // 添加考生信息（可选）
            if (memberProfile != null) {
                com.fasterxml.jackson.databind.node.ObjectNode memberNode = objectMapper.createObjectNode();
                if (memberProfile.getProvince() != null) memberNode.put("省份", memberProfile.getProvince());
                if (memberProfile.getScore() != null) memberNode.put("总分", memberProfile.getScore());
                if (memberProfile.getRank() != null) memberNode.put("位次", memberProfile.getRank());
                if (memberProfile.getSubjectType() != null) memberNode.put("选科", memberProfile.getSubjectType());

                // 可选：各科成绩（仅在有值时添加）
                if (memberProfile.getScoreChinese() != null) memberNode.put("语文", memberProfile.getScoreChinese());
                if (memberProfile.getScoreMath() != null) memberNode.put("数学", memberProfile.getScoreMath());
                if (memberProfile.getScoreEnglish() != null) memberNode.put("英语", memberProfile.getScoreEnglish());
                if (memberProfile.getScorePhysics() != null) memberNode.put("物理", memberProfile.getScorePhysics());
                if (memberProfile.getScoreChemistry() != null) memberNode.put("化学", memberProfile.getScoreChemistry());
                if (memberProfile.getScoreBiology() != null) memberNode.put("生物", memberProfile.getScoreBiology());
                if (memberProfile.getScorePolitics() != null) memberNode.put("政治", memberProfile.getScorePolitics());
                if (memberProfile.getScoreHistory() != null) memberNode.put("历史", memberProfile.getScoreHistory());
                if (memberProfile.getScoreGeography() != null) memberNode.put("地理", memberProfile.getScoreGeography());

                // 可选：身体条件（建议性参考）
                StringBuilder physicalInfo = new StringBuilder();
                if (Boolean.TRUE.equals(memberProfile.getIsColorBlind())) physicalInfo.append("色盲;");
                if (Boolean.TRUE.equals(memberProfile.getIsColorWeak())) physicalInfo.append("色弱;");
                if (memberProfile.getVisionLeft() != null || memberProfile.getVisionRight() != null) {
                    physicalInfo.append("视力左").append(memberProfile.getVisionLeft())
                            .append("/右").append(memberProfile.getVisionRight()).append(";");
                }
                if (memberProfile.getHeightCm() != null) physicalInfo.append("身高").append(memberProfile.getHeightCm()).append("cm;");
                if (!physicalInfo.isEmpty()) memberNode.put("身体条件", physicalInfo.toString());

                // 可选：身份条件
                StringBuilder identityInfo = new StringBuilder();
                if (Boolean.TRUE.equals(memberProfile.getIsFreshGraduate())) identityInfo.append("应届生;");
                if (Boolean.TRUE.equals(memberProfile.getIsPovertyCounty())) identityInfo.append("贫困县户籍;");
                if (!identityInfo.isEmpty()) memberNode.put("身份条件", identityInfo.toString());

                // 可选：批次线差
                if (memberProfile.getScoreAboveLine() != null) {
                    memberNode.put("线差", memberProfile.getScoreAboveLine());
                }

                // 可选：考生画像与约束条件
                if (memberProfile.getGender() != null) memberNode.put("性别", memberProfile.getGender());
                if (memberProfile.getPersonalityTraits() != null) memberNode.put("性格特质", memberProfile.getPersonalityTraits());
                if (memberProfile.getOtherHealthConditions() != null) memberNode.put("其他疾病", memberProfile.getOtherHealthConditions());
                if (memberProfile.getPoliticalReviewStatus() != null) memberNode.put("政审情况", memberProfile.getPoliticalReviewStatus());

                // 接受度
                StringBuilder acceptInfo = new StringBuilder();
                if (Boolean.TRUE.equals(memberProfile.getAcceptGrassroot())) acceptInfo.append("基层岗位;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptShiftWork())) acceptInfo.append("倒班;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptNightWork())) acceptInfo.append("夜班;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptBusinessTrip())) acceptInfo.append("长期出差;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptRelocation())) acceptInfo.append("异地工作;");
                if (!acceptInfo.isEmpty()) memberNode.put("接受度", acceptInfo.toString());

                if (memberProfile.getInterestDirection() != null) memberNode.put("兴趣倾向", memberProfile.getInterestDirection());
                if (memberProfile.getRejectedIndustries() != null) memberNode.put("排斥行业", memberProfile.getRejectedIndustries());
                if (memberProfile.getTuitionAffordability() != null) memberNode.put("学费承受度", memberProfile.getTuitionAffordability());
                if (Boolean.TRUE.equals(memberProfile.getStayInProvince())) memberNode.put("留本省", "是");
                if (memberProfile.getFamilyResources() != null) memberNode.put("家庭资源", memberProfile.getFamilyResources());
                if (memberProfile.getCareerDevPath() != null) memberNode.put("发展定位", memberProfile.getCareerDevPath());
                if (memberProfile.getRejectedDirections() != null) memberNode.put("排斥方向", memberProfile.getRejectedDirections());

                root.set("考生情况", memberNode);
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("Failed to build map input JSON", e);
            return "{}";
        }
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

            ## 个性化分析（建议性参考，权重较低）
            若数据中包含「考生情况」，请结合考生个人条件给出适当建议：
            - 若有「身体条件」限制（如色盲、色弱、视力等），在分析相关专业时适当提醒，但不排除该专业
            - 若有「选科」信息，可简单说明选科匹配度
            - 若有「线差」信息，可说明该考生分数层次
            - 若有「性别」信息，在分析军事/公安/护理等有性别倾向的专业时适当参考
            - 若有「接受度」信息（基层岗位、倒班、夜班、长期出差、异地工作），在分析相关岗位时适当提醒
            - 若有「排斥行业/岗位」或「排斥方向」，在分析时避开相关领域
            - 若有「兴趣倾向」，可适当推荐匹配度高的方向
            - 若有「发展定位」（本科就业/考研深造/并行），在分析时侧重对应方向
            - 若有「学费承受度」，在分析高学费专业时适当提醒
            - 若有「留本省」约束，优先分析本省院校
            注意：考生信息权重较低，主要分析维度仍是大学、城市、专业本身。
            """;
    }

    /**
     * 构建 Reduce 阶段输入 JSON
     * <p>新结构：包含院校/专业/城市三维数据 + Map阶段AI简评 + 考生画像
     */
    private String buildReduceInput(List<MapResultItem> mapResults, List<ExportGroupContextVO> groups,
                                    MemberGaokaoContextVO memberProfile) {
        try {
            Map<Integer, ExportGroupContextVO> groupMap = groups.stream()
                    .collect(Collectors.toMap(ExportGroupContextVO::getGroupSnapshotId, g -> g, (a, b) -> a));

            com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();

            // 1. 院校维度数据（不去重，每个专业组对应一个大学）
            com.fasterxml.jackson.databind.node.ArrayNode universitiesNode = objectMapper.createArrayNode();
            for (MapResultItem item : mapResults) {
                com.fasterxml.jackson.databind.node.ObjectNode uniNode = objectMapper.createObjectNode();
                uniNode.put("大学名称", item.getGroupName());
                uniNode.put("城市", item.getCityName());
                uniNode.put("专业组代码", item.getGroupName());

                ExportGroupContextVO group = item.getGroupSnapshotId() != null
                        ? groupMap.get(item.getGroupSnapshotId()) : null;
                if (group != null) {
                    // 从 WishGroupSnapshot 获取院校信息
                    if (group.getUniversityName() != null) {
                        uniNode.put("universityName", group.getUniversityName());
                    }
                }

                // Map阶段AI简评
                uniNode.put("AI简评", item.getCommentary() != null ? item.getCommentary() : "暂无简评");

                // 录取概率汇总
                if (item.getMajors() != null && !item.getMajors().isEmpty()) {
                    String probability = item.getMajors().stream()
                            .map(m -> m.getLevelShort() != null ? m.getLevelShort() : "")
                            .distinct()
                            .collect(Collectors.joining("/"));
                    uniNode.put("录取概率", probability);
                }

                universitiesNode.add(uniNode);
            }
            root.set("院校维度", universitiesNode);

            // 2. 专业维度数据（去重，同名专业只分析一次）
            com.fasterxml.jackson.databind.node.ArrayNode majorsNode = objectMapper.createArrayNode();
            java.util.Set<String> addedMajors = new java.util.HashSet<>();
            for (MapResultItem item : mapResults) {
                if (item.getMajors() == null) continue;
                for (MapResultItem.MajorBrief m : item.getMajors()) {
                    String key = m.getMajorName();
                    if (key == null || addedMajors.contains(key)) continue;
                    addedMajors.add(key);

                    com.fasterxml.jackson.databind.node.ObjectNode mNode = objectMapper.createObjectNode();
                    mNode.put("专业名称", m.getMajorName());
                    if (m.getMajorCategory() != null) mNode.put("学科门类", m.getMajorCategory());
                    if (m.getEmploymentRate() != null) mNode.put("就业率", m.getEmploymentRate());
                    if (m.getSalaryMin() != null) mNode.put("薪资下限", m.getSalaryMin());
                    if (m.getSalaryMax() != null) mNode.put("薪资上限", m.getSalaryMax());

                    // 从 groups 获取更详细的专业信息
                    for (ExportGroupContextVO group : groups) {
                        if (group.getExportableMajors() != null) {
                            for (var em : group.getExportableMajors()) {
                                if (key.equals(em.getMajorName()) && em.getMajorEnrichment() != null) {
                                    var enrich = em.getMajorEnrichment();
                                    if (enrich.getParentCategory() != null) mNode.put("专业类", enrich.getParentCategory());
                                    if (enrich.getMajorTags() != null) mNode.put("专业标签", enrich.getMajorTags());
                                    if (enrich.getDegreeAwarded() != null) mNode.put("授予学位", enrich.getDegreeAwarded());
                                    break;
                                }
                            }
                        }
                    }

                    majorsNode.add(mNode);
                }
            }
            root.set("专业维度", majorsNode);

            // 3. 城市维度数据（去重，同城市只分析一次）
            com.fasterxml.jackson.databind.node.ArrayNode citiesNode = objectMapper.createArrayNode();
            java.util.Set<String> addedCities = new java.util.HashSet<>();
            for (MapResultItem item : mapResults) {
                String cityName = item.getCityName();
                if (cityName == null || addedCities.contains(cityName)) continue;
                addedCities.add(cityName);

                com.fasterxml.jackson.databind.node.ObjectNode cityNode = objectMapper.createObjectNode();
                cityNode.put("城市名称", cityName);

                ExportGroupContextVO group = item.getGroupSnapshotId() != null
                        ? groupMap.get(item.getGroupSnapshotId()) : null;
                if (group != null && group.getCityEnrichment() != null) {
                    CityEnrichmentVO ci = group.getCityEnrichment();
                    if (ci.getCityLevel() != null) cityNode.put("城市等级", ci.getCityLevel());
                    if (ci.getGdp() != null) cityNode.put("GDP", ci.getGdp());
                    if (ci.getGdpGrowthRate() != null) cityNode.put("GDP增长率", ci.getGdpGrowthRate());
                    if (ci.getFortune500Count() != null) cityNode.put("世界500强数量", ci.getFortune500Count());
                    if (ci.getMainIndustries() != null) cityNode.set("主要产业", objectMapper.valueToTree(ci.getMainIndustries()));
                    if (ci.getEmergingIndustries() != null) cityNode.set("新兴产业", objectMapper.valueToTree(ci.getEmergingIndustries()));
                    if (ci.getAvgSalary() != null) cityNode.put("平均薪资", ci.getAvgSalary());
                    if (ci.getUnemploymentRate() != null) cityNode.put("失业率", ci.getUnemploymentRate());
                }

                citiesNode.add(cityNode);
            }
            root.set("城市维度", citiesNode);

            // 4. 专业明细（用于综合考虑排名）
            com.fasterxml.jackson.databind.node.ArrayNode detailsNode = objectMapper.createArrayNode();
            for (MapResultItem item : mapResults) {
                if (item.getMajors() == null) continue;
                ExportGroupContextVO group = item.getGroupSnapshotId() != null
                        ? groupMap.get(item.getGroupSnapshotId()) : null;

                for (MapResultItem.MajorBrief m : item.getMajors()) {
                    com.fasterxml.jackson.databind.node.ObjectNode detailNode = objectMapper.createObjectNode();
                    detailNode.put("大学名称", item.getGroupName());
                    detailNode.put("城市名称", item.getCityName());
                    detailNode.put("专业名称", m.getMajorName());
                    if (m.getSafetyLevel() != null) detailNode.put("安全系数", m.getSafetyLevel().doubleValue());
                    if (m.getLevelShort() != null) detailNode.put("档位", m.getLevelShort());
                    if (m.getEmploymentRate() != null) detailNode.put("就业率", m.getEmploymentRate());
                    if (m.getSalaryMin() != null) detailNode.put("薪资下限", m.getSalaryMin());
                    if (m.getSalaryMax() != null) detailNode.put("薪资上限", m.getSalaryMax());

                    // 城市GDP
                    if (group != null && group.getCityEnrichment() != null && group.getCityEnrichment().getGdp() != null) {
                        detailNode.put("城市GDP", group.getCityEnrichment().getGdp());
                    }

                    detailsNode.add(detailNode);
                }
            }
            root.set("专业明细", detailsNode);

            // 5. 考生画像（如果存在）
            if (memberProfile != null) {
                com.fasterxml.jackson.databind.node.ObjectNode memberNode = objectMapper.createObjectNode();
                if (memberProfile.getProvince() != null) memberNode.put("省份", memberProfile.getProvince());
                if (memberProfile.getScore() != null) memberNode.put("总分", memberProfile.getScore());
                if (memberProfile.getRank() != null) memberNode.put("位次", memberProfile.getRank());
                if (memberProfile.getSubjectType() != null) memberNode.put("选科", memberProfile.getSubjectType());

                // 各科成绩
                if (memberProfile.getScoreChinese() != null) memberNode.put("语文", memberProfile.getScoreChinese());
                if (memberProfile.getScoreMath() != null) memberNode.put("数学", memberProfile.getScoreMath());
                if (memberProfile.getScoreEnglish() != null) memberNode.put("英语", memberProfile.getScoreEnglish());
                if (memberProfile.getScorePhysics() != null) memberNode.put("物理", memberProfile.getScorePhysics());
                if (memberProfile.getScoreChemistry() != null) memberNode.put("化学", memberProfile.getScoreChemistry());
                if (memberProfile.getScoreBiology() != null) memberNode.put("生物", memberProfile.getScoreBiology());
                if (memberProfile.getScorePolitics() != null) memberNode.put("政治", memberProfile.getScorePolitics());
                if (memberProfile.getScoreHistory() != null) memberNode.put("历史", memberProfile.getScoreHistory());
                if (memberProfile.getScoreGeography() != null) memberNode.put("地理", memberProfile.getScoreGeography());

                // 身体条件
                StringBuilder physicalInfo = new StringBuilder();
                if (Boolean.TRUE.equals(memberProfile.getIsColorBlind())) physicalInfo.append("色盲;");
                if (Boolean.TRUE.equals(memberProfile.getIsColorWeak())) physicalInfo.append("色弱;");
                if (memberProfile.getHeightCm() != null) physicalInfo.append("身高").append(memberProfile.getHeightCm()).append("cm;");
                if (!physicalInfo.isEmpty()) memberNode.put("身体条件", physicalInfo.toString());

                // 身份条件
                if (Boolean.TRUE.equals(memberProfile.getIsPovertyCounty())) memberNode.put("贫困县户籍", "是");
                if (Boolean.TRUE.equals(memberProfile.getIsFreshGraduate())) memberNode.put("应届生", "是");

                // 批次线差
                if (memberProfile.getScoreAboveLine() != null) {
                    memberNode.put("线差", memberProfile.getScoreAboveLine());
                }

                // 画像约束条件
                if (memberProfile.getGender() != null) memberNode.put("性别", memberProfile.getGender());
                if (memberProfile.getPersonalityTraits() != null) memberNode.put("性格特质", memberProfile.getPersonalityTraits());
                if (memberProfile.getOtherHealthConditions() != null) memberNode.put("其他疾病", memberProfile.getOtherHealthConditions());
                if (memberProfile.getPoliticalReviewStatus() != null) memberNode.put("政审情况", memberProfile.getPoliticalReviewStatus());

                // 接受度
                StringBuilder acceptInfo = new StringBuilder();
                if (Boolean.TRUE.equals(memberProfile.getAcceptGrassroot())) acceptInfo.append("基层岗位;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptShiftWork())) acceptInfo.append("倒班;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptNightWork())) acceptInfo.append("夜班;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptBusinessTrip())) acceptInfo.append("长期出差;");
                if (Boolean.TRUE.equals(memberProfile.getAcceptRelocation())) acceptInfo.append("异地工作;");
                if (!acceptInfo.isEmpty()) memberNode.put("接受度", acceptInfo.toString());

                if (memberProfile.getInterestDirection() != null) memberNode.put("兴趣倾向", memberProfile.getInterestDirection());
                if (memberProfile.getRejectedIndustries() != null) memberNode.put("排斥行业", memberProfile.getRejectedIndustries());
                if (memberProfile.getTuitionAffordability() != null) memberNode.put("学费承受度", memberProfile.getTuitionAffordability());
                if (Boolean.TRUE.equals(memberProfile.getStayInProvince())) memberNode.put("留本省", "是");
                if (memberProfile.getFamilyResources() != null) memberNode.put("家庭资源", memberProfile.getFamilyResources());
                if (memberProfile.getCareerDevPath() != null) memberNode.put("发展定位", memberProfile.getCareerDevPath());
                if (memberProfile.getRejectedDirections() != null) memberNode.put("排斥方向", memberProfile.getRejectedDirections());

                root.set("考生画像", memberNode);
            }

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.error("Failed to build reduce input JSON", e);
            return "{}";
        }
    }

    /**
     * 构建 Reduce 阶段 System Prompt
     * <p>新结构：院校/专业/城市分析 + 综合排名
     */
    private String buildReduceSystemPrompt() {
        return """
            你是海枫未来规划院的首席志愿规划专家。请根据提供的院校、专业、城市数据，进行外部宏观全景研判。

            数据结构说明：
            - 「院校维度」：各专业组对应的大学信息 + Map阶段AI简评
            - 「专业维度」：去重后的专业详情（就业率、薪资等）
            - 「城市维度」：去重后的城市详情（GDP、产业等）
            - 「专业明细」：每个专业组-专业的完整信息（用于排名）
            - 「考生画像」：考生个人条件（可选）

            ## 输出要求

            请严格按以下格式输出，每部分用 Markdown ## 标题分隔：

            ## 学生画像
            如果有「考生画像」数据，用1-2句话总结考生核心条件（分数、位次、关键约束）。如果没有，输出"暂无考生画像数据"。

            ## 院校分析
            对「院校维度」中的每个大学，输出以下内容（每个大学100-200字）：
            ### [大学名称]
            - **标签**：展示标签、类别、办学性质（从数据中提取）
            - **分析**：结合该校的学科实力、就业资源、城市区位，给出客观研判
            - 若有考生画像，结合考生条件说明匹配度

            ## 专业分析
            对「专业维度」中的每个专业，输出以下内容（每个专业100-200字）：
            ### [专业名称]
            - **信息**：学科门类、专业类、授予学位、就业率、薪资范围
            - **分析**：结合就业前景、行业趋势、薪资水平，给出客观研判
            - 若有考生画像，结合考生兴趣、身体条件、发展定位说明匹配度

            ## 城市分析
            对「城市维度」中的每个城市，输出以下内容（每个城市100-200字）：
            ### [城市名称]
            - **信息**：城市等级、GDP、增长率、500强数量、主要产业、新兴产业、平均薪资
            - **分析**：结合产业结构、就业机会、发展潜力，给出客观研判
            - 若有考生画像，结合考生留本省意愿、接受异地工作等条件说明匹配度

            ## 综合考虑
            对「专业明细」中的每个专业进行综合排名，输出以下内容：

            ### 排名表
            用Markdown表格输出排名，格式：
            | 排名 | 专业组 | 大学 | 城市 | 专业 | 综合得分 | 档位 |
            |------|--------|------|------|------|----------|------|

            ### 排序理由
            用300-500字说明排序逻辑，包括：
            1. 核心排序依据（录取概率、就业率、薪资、城市GDP等）
            2. 考生画像匹配度如何影响排名
            3. 哪些是高风险高收益，哪些是稳妥选择
            4. 给出明确的填报建议

            ## 要求
            1. 院校/专业/城市分析要独立成章，不要重复Map阶段的简评内容
            2. 综合排名要给出具体分数（0-100分），分数差异要有区分度
            3. 使用Markdown格式输出（**加粗**、- 列表、表格等）
            4. 若某维度数据为空，基于常识判断并说明
            """;
    }

    /**
     * 解析 Reduce 阶段 AI 响应
     * <p>新结构：学生画像 + 外部宏观全景研判（院校/专业/城市/综合） + SWOT + 推荐梯队
     */
    private ReduceResult parseReduceResult(String response) {
        if (response == null || response.isBlank()) {
            return ReduceResult.builder()
                    .studentProfile("")
                    .swot("")
                    .recommendation("")
                    .build();
        }

        try {
            // 尝试解析为JSON（AI可能返回JSON格式的排名数据）
            if (response.trim().startsWith("{")) {
                return parseReduceResultFromJson(response);
            }
        } catch (Exception e) {
            log.debug("Response is not JSON, falling back to markdown parsing");
        }

        // 按 ## 标题分割（Markdown格式）
        if (response.contains("## ")) {
            String studentProfile = extractSection(response, "学生画像");
            String macroAnalysisText = extractMacroAnalysisSection(response);
            String swot = extractSection(response, "SWOT象限分析");
            String recommendation = extractSection(response, "海枫强烈推荐填报梯队顺序");

            // 解析宏观分析文本中的排名数据
            MacroAnalysisVO macroAnalysis = parseMacroAnalysisFromText(macroAnalysisText);

            return ReduceResult.builder()
                    .studentProfile(studentProfile)
                    .macroAnalysis(macroAnalysis)
                    .swot(swot)
                    .recommendation(recommendation)
                    .build();
        }

        // fallback: 旧格式用 === 分割
        String[] parts = response.split("={3,}", 3);
        return ReduceResult.builder()
                .studentProfile(parts.length > 0 ? parts[0].trim() : "")
                .swot(parts.length > 1 ? parts[1].trim() : "")
                .recommendation(parts.length > 2 ? parts[2].trim() : "")
                .build();
    }

    /**
     * 从JSON格式解析Reduce结果
     */
    private ReduceResult parseReduceResultFromJson(String response) throws Exception {
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);

        ReduceResult.ReduceResultBuilder builder = ReduceResult.builder();

        // 学生画像
        if (root.has("studentProfile")) {
            builder.studentProfile(root.get("studentProfile").asText(""));
        }

        // 宏观分析
        if (root.has("macroAnalysis")) {
            MacroAnalysisVO macroAnalysis = objectMapper.treeToValue(root.get("macroAnalysis"), MacroAnalysisVO.class);
            builder.macroAnalysis(macroAnalysis);
        }

        // SWOT
        if (root.has("swot")) {
            builder.swot(root.get("swot").asText(""));
        }

        // 推荐
        if (root.has("recommendation")) {
            builder.recommendation(root.get("recommendation").asText(""));
        }

        return builder.build();
    }

    /**
     * 提取宏观分析部分的完整文本（从"院校分析"到"SWOT"之前）
     */
    private String extractMacroAnalysisSection(String text) {
        String[] lines = text.split("\n");
        StringBuilder content = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            if (line.trim().startsWith("## ")) {
                if (inSection) {
                    break; // 遇到下一个 ## 标题，结束
                }
                // 宏观分析从"院校分析"开始
                if (line.contains("院校分析") || line.contains("专业分析") || line.contains("城市分析") || line.contains("综合考虑")) {
                    inSection = true;
                }
            } else if (inSection) {
                content.append(line).append("\n");
            }
        }
        return content.toString().trim();
    }

    /**
     * 从宏观分析文本中解析出结构化数据
     */
    private MacroAnalysisVO parseMacroAnalysisFromText(String text) {
        MacroAnalysisVO.MacroAnalysisVOBuilder builder = MacroAnalysisVO.builder();

        // 解析排名表
        List<MacroAnalysisVO.RankingItem> ranking = extractRankingFromText(text);
        String reasoning = extractSection(text, "排序理由");

        MacroAnalysisVO.ComprehensiveAnalysis comprehensive = MacroAnalysisVO.ComprehensiveAnalysis.builder()
                .ranking(ranking)
                .reasoning(reasoning)
                .build();

        builder.comprehensiveAnalysis(comprehensive);
        return builder.build();
    }

    /**
     * 从文本中提取排名表
     */
    private List<MacroAnalysisVO.RankingItem> extractRankingFromText(String text) {
        List<MacroAnalysisVO.RankingItem> ranking = new ArrayList<>();
        String[] lines = text.split("\n");

        boolean inTable = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("|") && trimmed.contains("排名")) {
                inTable = true;
                continue;
            }
            if (inTable && trimmed.startsWith("|---")) {
                continue; // 跳过分隔行
            }
            if (inTable && trimmed.startsWith("|")) {
                String[] cells = trimmed.split("\\|");
                if (cells.length >= 7) {
                    try {
                        MacroAnalysisVO.RankingItem item = MacroAnalysisVO.RankingItem.builder()
                                .rank(Integer.parseInt(cells[1].trim()))
                                .groupName(cells[2].trim())
                                .universityName(cells[3].trim())
                                .cityName(cells[4].trim())
                                .majorName(cells[5].trim())
                                .score(new java.math.BigDecimal(cells[6].trim()))
                                .levelShort(cells.length > 7 ? cells[7].trim() : "")
                                .build();
                        ranking.add(item);
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse ranking row: {}", trimmed);
                    }
                }
            } else if (inTable && !trimmed.startsWith("|")) {
                inTable = false;
            }
        }
        return ranking;
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

    private boolean isReduceResultEmpty(ReduceResult reduceResult) {
        if (reduceResult == null) return true;
        boolean pEmpty = reduceResult.getStudentProfile() == null || reduceResult.getStudentProfile().isBlank();
        boolean mEmpty = reduceResult.getMacroAnalysis() == null;
        boolean sEmpty = reduceResult.getSwot() == null || reduceResult.getSwot().isBlank();
        boolean rEmpty = reduceResult.getRecommendation() == null || reduceResult.getRecommendation().isBlank();
        return pEmpty && mEmpty && sEmpty && rEmpty;
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
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    // JSON 规范要求转义 U+0000 ~ U+001F 控制字符
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    // ===================== 历史记录查询 =====================

    @Override
    public IPage<PdfRecordListVO> pageRecords(Long userId, PdfRecordQueryDTO dto) {
        Page<PdfReport> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<PdfReport> wrapper = new LambdaQueryWrapper<PdfReport>()
                .eq(PdfReport::getMemberId, userId)
                .eq(dto.getStatus() != null, PdfReport::getStatus, dto.getStatus())
                .eq(dto.getPlanId() != null, PdfReport::getPlanId, dto.getPlanId())
                .orderByDesc(PdfReport::getCreatedAt);

        IPage<PdfReport> result = pdfReportMapper.selectPage(page, wrapper);

        List<Integer> planIds = result.getRecords().stream()
                .map(PdfReport::getPlanId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, String> planNameMap = new java.util.HashMap<>();
        if (!planIds.isEmpty()) {
            List<WishPlan> plans = wishPlanMapper.selectBatchIds(planIds);
            for (WishPlan p : plans) {
                planNameMap.put(p.getId(), p.getPlanName());
            }
        }

        Map<Integer, String> finalPlanNameMap = planNameMap;
        return result.convert(report -> PdfRecordListVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .planName(finalPlanNameMap.get(report.getPlanId()))
                .status(report.getStatus() != null ? report.getStatus().getValue() : null)
                .createdAt(report.getCreatedAt())
                .build());
    }

    @Override
    public PdfRecordDetailVO getRecordDetail(Long userId, Integer recordId) {
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报告记录不存在");
        }

        String planName = null;
        if (report.getPlanId() != null) {
            WishPlan plan = wishPlanMapper.selectById(report.getPlanId());
            planName = plan != null ? plan.getPlanName() : null;
        }

        return PdfRecordDetailVO.builder()
                .id(report.getId())
                .planId(report.getPlanId())
                .planName(planName)
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

    @Override
    public String getDownloadFilename(Long userId, Integer recordId) {
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId())) {
            return "haifeng-report-" + recordId;
        }
        try {
            if (report.getPlanSnapshot() != null && !report.getPlanSnapshot().isBlank()) {
                PlanSnapshot snapshot = objectMapper.readValue(report.getPlanSnapshot(), PlanSnapshot.class);
                StringBuilder name = new StringBuilder("海枫报告");
                if (snapshot.getPlanYear() != null) name.append("-").append(snapshot.getPlanYear());
                if (snapshot.getPlanProvince() != null) name.append(snapshot.getPlanProvince());
                if (snapshot.getUserScore() != null) name.append("-").append(snapshot.getUserScore()).append("分");
                return name.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to parse planSnapshot for filename, recordId={}", recordId);
        }
        return "haifeng-report-" + recordId;
    }

    @Override
    public void deleteRecord(Long userId, Integer recordId) {
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报告记录不存在");
        }
        if (report.getStatus() == PdfReportStatus.GENERATING) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "生成中的报告不能删除");
        }
        pdfReportMapper.deleteById(recordId);
        pdfRenderService.evictCache(recordId);
        log.info("PDF report deleted, userId={}, recordId={}", userId, recordId);
    }

    /**
     * 从 MemberGaokao 构建 MemberGaokaoContextVO
     * <p>仅提取 AI 分析所需的精简字段，null 值不传给 AI。
     */
    private MemberGaokaoContextVO buildMemberProfile(MemberGaokao gaokao) {
        if (gaokao == null) {
            return null;
        }
        return MemberGaokaoContextVO.builder()
                .province(gaokao.getGaokaoProvince())
                .score(gaokao.getScore())
                .rank(gaokao.getRank())
                .subjectType(gaokao.getSubjectType())
                .scoreChinese(gaokao.getScoreChinese())
                .scoreMath(gaokao.getScoreMath())
                .scoreEnglish(gaokao.getScoreEnglish())
                .scorePhysics(gaokao.getScorePhysics())
                .scoreChemistry(gaokao.getScoreChemistry())
                .scoreBiology(gaokao.getScoreBiology())
                .scorePolitics(gaokao.getScorePolitics())
                .scoreHistory(gaokao.getScoreHistory())
                .scoreGeography(gaokao.getScoreGeography())
                .isColorBlind(gaokao.getIsColorBlind())
                .isColorWeak(gaokao.getIsColorWeak())
                .visionLeft(gaokao.getVisionLeft())
                .visionRight(gaokao.getVisionRight())
                .heightCm(gaokao.getHeightCm())
                .isFreshGraduate(gaokao.getIsFreshGraduate())
                .isPovertyCounty(gaokao.getIsPovertyCounty())
                .batch(gaokao.getBatch())
                .batchLineScore(gaokao.getBatchLineScore())
                .scoreAboveLine(gaokao.getScoreAboveLine())
                .gender(gaokao.getGender())
                .otherHealthConditions(gaokao.getOtherHealthConditions())
                .politicalReviewStatus(gaokao.getPoliticalReviewStatus())
                .personalityTraits(gaokao.getPersonalityTraits())
                .acceptGrassroot(gaokao.getAcceptGrassroot())
                .acceptShiftWork(gaokao.getAcceptShiftWork())
                .acceptNightWork(gaokao.getAcceptNightWork())
                .acceptBusinessTrip(gaokao.getAcceptBusinessTrip())
                .acceptRelocation(gaokao.getAcceptRelocation())
                .interestDirection(gaokao.getInterestDirection())
                .rejectedIndustries(gaokao.getRejectedIndustries())
                .tuitionAffordability(gaokao.getTuitionAffordability())
                .stayInProvince(gaokao.getStayInProvince())
                .familyResources(gaokao.getFamilyResources())
                .careerDevPath(gaokao.getCareerDevPath())
                .rejectedDirections(gaokao.getRejectedDirections())
                .build();
    }
}
