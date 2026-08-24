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
import com.haifeng.common.config.OssProperties;
import com.haifeng.common.config.PdfPreviewConfig;
import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.service.ai.AiQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
    private final PdfPreviewConfig pdfPreviewConfig;
    private final OssProperties ossProperties;
    private final StringRedisTemplate redisTemplate;

    public PdfReportServiceImpl(PdfReportMapper pdfReportMapper,
                                AiChatService aiChatService,
                                AiQuotaService quotaService,
                                WishPlanService wishPlanService,
                                ObjectMapper objectMapper,
                                WishPlanMapper wishPlanMapper,
                                MemberGaokaoMapper memberGaokaoMapper,
                                PdfRenderService pdfRenderService,
                                ChartService chartService,
                                @Qualifier("pdfMapExecutor") ExecutorService pdfMapExecutor,
                                PdfPreviewConfig pdfPreviewConfig,
                                OssProperties ossProperties,
                                StringRedisTemplate redisTemplate) {
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
        this.pdfPreviewConfig = pdfPreviewConfig;
        this.ossProperties = ossProperties;
        this.redisTemplate = redisTemplate;
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

            // 生成体制内适配度图表（第十部分）
            if (reduceResult.getCivilServiceScores() != null && !reduceResult.getCivilServiceScores().isEmpty()) {
                String scoreChartBase64 = chartService.generateScoreChart(reduceResult.getCivilServiceScores());
                reduceResult.setCivilServiceChartBase64(scoreChartBase64);
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
                    if (group.getUniversityName() != null) {
                        uniNode.put("universityName", group.getUniversityName());
                    }
                    // 院校详细属性
                    if (group.getUniversityInfo() != null) {
                        var uni = group.getUniversityInfo();
                        if (uni.getCategory() != null) uniNode.put("类别", uni.getCategory());
                        if (uni.getEducationLevel() != null) uniNode.put("办学层次", uni.getEducationLevel());
                        if (uni.getNature() != null) uniNode.put("办学性质", uni.getNature());
                        if (Boolean.TRUE.equals(uni.getHasDoctorate())) uniNode.put("博士点", "有");
                        if (Boolean.TRUE.equals(uni.getHasMaster())) uniNode.put("硕士点", "有");
                        if (uni.getRecommendationRate() != null) uniNode.put("保研率", uni.getRecommendationRate());
                        if (uni.getTags() != null && !uni.getTags().isEmpty()) {
                            uniNode.set("标签", objectMapper.valueToTree(uni.getTags()));
                        }
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
                        // 专业详情（课程/培养目标/就业前景）
                        if (group.getMajorDetail() != null) {
                            var detail = group.getMajorDetail();
                            if (detail.getMainCourses() != null) mNode.set("核心课程", objectMapper.valueToTree(detail.getMainCourses()));
                            if (detail.getTrainingObjective() != null) mNode.put("培养目标", detail.getTrainingObjective());
                            if (detail.getCareerProspect() != null) mNode.put("就业前景", detail.getCareerProspect());
                        }
                        // 研究生方向
                        if (group.getPostgradDirections() != null && !group.getPostgradDirections().isEmpty()) {
                            java.util.List<String> dirs = new java.util.ArrayList<>();
                            for (var dir : group.getPostgradDirections()) {
                                if (dir.getPostgradMajorName() != null) dirs.add(dir.getPostgradMajorName());
                            }
                            if (!dirs.isEmpty()) mNode.set("研究生方向", objectMapper.valueToTree(dirs));
                        }
                        break; // 只需从一个 group 获取即可
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
     * <p>新结构：学生画像 + 赛道分类 + 政策红利 + SWOT + 推荐梯队 + 大学/专业/城市专项拆解 + 综合评判
     */
    private String buildReduceSystemPrompt() {
        return """
            你是海枫未来规划院的首席志愿规划专家。请根据提供的院校、专业、城市数据，进行全面分析。

            数据结构说明：
            - 「院校维度」：各专业组对应的大学信息 + Map阶段AI简评 + 院校详细属性（985/211/硕士点/博士点/保研率等）
            - 「专业维度」：去重后的专业详情（就业率、薪资、课程、培养目标、就业前景等）
            - 「城市维度」：去重后的城市详情（GDP、产业、薪资等）
            - 「专业明细」：每个专业组-专业的完整信息（用于综合排名）
            - 「考生画像」：考生个人条件（可选）

            ## 输出要求

            请严格按以下格式输出，每部分用 Markdown ## 标题分隔：

            ## 学生画像
            如果有「考生画像」数据，用1-2句话总结考生核心条件（分数、位次、关键约束）。如果没有，输出"暂无考生画像数据"。

            ## 赛道分类研判
            把「专业维度」中的所有专业归入以下4类赛道，每个赛道列出相关专业名称并给出分析：

            ### 高风险高收益赛道
            - 赛道名称、产业逻辑、薪资上限、必须读研？、风险点、适配哪类考生

            ### 高性价比赛道
            - 赛道名称、产业逻辑、就业起薪区间、城市购买力、向外辐射就业圈、优缺点

            ### 稳健保底赛道（下限高，上限一般）
            - 赛道名称、刚需来源、岗位缺口、潜在隐忧、适配考生

            ### 谨慎/规避赛道（结构性收缩）
            - 赛道名称、收缩原因、仅适合的极小部分人群

            ## 政策红利分析
            国家政策对目标专业的影响：双碳、国产替代、军工、养老医疗、新型电力系统等。对每个相关专业说明机遇或风险。

            ## 大学专项拆解
            对「院校维度」中的每个大学，输出详细分析（500-800字）：

            ### [大学名称]
            #### 6.1 大学基本面
            - 院校属性：公办/民办、985/211/双一流、本科/专科、硕士点/博士点
            - 就读城市：主导产业、本地国企央企资源、生活成本、向外辐射就业圈
            - 本专业在校实力：学科评估等级、是否校级重点、师资力量、实验室条件、竞赛平台
            - 深造数据：保研率、考研去向、往届学长学姐冲刺/稳妥/保底院校
            - 入学限制：转专业政策、专项计划转专业限制、单科成绩要求、身体条件

            #### 6.2 专业赛道研判
            - 本科核心课程清单、学习难度
            - 赛道归类：高风险/性价比/稳健/谨慎
            - 行业前景：人才缺口、岗位饱和、AI替代风险、本科/硕士薪资区间、是否必须读研
            - 岗位工作特征：是否倒班、出差、下现场、工作强度

            #### 6.3 本科直接就业分析
            - 可就业行业清单
            - 可投递企业分层：高竞争池（央企总部）、中竞争池（央企二级/省属国企）、低竞争池（地市县级）
            - 事业单位/公务员岗位清单、头部民企/中小企业
            - 代表性岗位清单、硬性门槛（四六级/计算机证/党员等）
            - 就业地域流向、本科就业风险点

            #### 6.4 考研路径分析
            - 本专业考研：考试科目、学硕/专硕区别、冲刺/稳妥/保底院校、分数线/报录比、复试风险
            - 可跨考方向：需提前自学课程、复试追问的跨考动机、跨考风险
            - 不建议跨考方向及原因
            - 读研成本：学制、学费、预期收益
            - 考研失利预案

            #### 6.5 研究生毕业就业分析
            - 硕士可冲击更高层级岗位：央企研究院、省公司、大厂研发、事业单位
            - 硕士薪资区间、岗位上限
            - 是否适合读博、读博适配人群
            - 硕士就业风险：学历通胀、第一学历歧视、读博沉没成本

            #### 6.6 综合适配结论
            - 适合的学生特质
            - 不适合的学生特质
            - 大学四年关键动作提示（证书、竞赛、科研、实习）
            - 核心风险提示

            ## 专业专项拆解
            对「专业维度」中的每个专业，输出详细分析（500-800字）：

            ### [专业名称]
            #### 7.1 专业基本面
            - 专业全名、学科门类、专业类、授予学位
            - 核心课程清单、学习难度
            - 培养目标、就业前景概述

            #### 7.2 专业赛道研判
            - 赛道归类
            - 行业前景：人才缺口、饱和度、AI替代风险
            - 本科/硕士薪资区间、天花板
            - 是否必须读研
            - 岗位特征：工作强度、出差频率、基层占比

            #### 7.3 本科直接就业分析
            - 可就业行业清单
            - 企业分层：高/中/低竞争池
            - 代表性岗位清单
            - 硬性门槛
            - 就业地域流向
            - 本科就业风险点

            #### 7.4 考研路径分析
            - 本专业考研方向
            - 跨考方向与风险
            - 读研成本与收益

            #### 7.5 研究生就业分析
            - 硕士岗位层级提升
            - 薪资区间
            - 读博适配性
            - 就业风险

            #### 7.6 综合适配结论
            - 适合/不适合的学生特质
            - 大学四年关键动作
            - 核心风险提示

            ## 城市专项拆解
            对「城市维度」中的每个城市，输出详细分析（500-800字）：

            ### [城市名称]
            #### 8.1 城市基本面
            - 城市等级、人口、GDP、增长率
            - 500强企业数量、主要产业、新兴产业

            #### 8.2 城市产业分析
            - 主导产业详细分析
            - 产业结构优劣势
            - 与目标专业的产业匹配度

            #### 8.3 城市就业分析
            - 就业机会总量与质量
            - 体制内/体制外岗位分布
            - 企业薪资水平
            - 就业竞争激烈程度

            #### 8.4 城市生活成本分析
            - 房价/房租水平
            - 生活物价
            - 交通成本
            - 落户难度

            #### 8.5 城市综合适配结论
            - 适合哪类考生
            - 不适合哪类考生
            - 核心建议

            ## 综合评判
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

            ## 就业前景与展望
            基于志愿表中各专业的就业方向，输出以下3个子节：

            ### 央国企对口方向
            针对「专业维度」中的各专业，逐一分析其对口央企/国企方向（如电气工程->国家电网/南方电网、土木->中铁/中建等），200-400字。

            ### 体制内适配分析
            分析各专业报考体制内（事业单位/公务员/选调生）的机会：
            - 岗位类型：省考/国考/事业单位联考/单招/选调生
            - 报考限制：专业目录限制、学历要求、政治面貌、应届身份
            - 适合条件：哪些专业适合走体制内，哪些受限
            本子节末尾**必须**输出一个打分表格（Markdown格式），为每个志愿专业评估体制内适配度：
            | 专业组 | 专业 | 体制内适配度 |
            |--------|------|--------------|
            | [大学名称] | [专业名称] | [0-100整数] |

            ### 民营企业赛道
            对「专业维度」中的每个专业，输出一个小模块：

            #### {专业名称}
            - 代表企业：该专业毕业生可去的头部/中腰部民营企业名称
            - 典型岗位：可投递的代表性岗位清单
            - 优缺点：民企就业的薪资、成长性、稳定性等利弊分析

            ## SWOT象限分析
            基于以上所有分析，输出全局SWOT综合象限分析。每个象限恰好3条，最后附博弈辩证结论。
            **必须使用以下HTML格式输出（带CSS class）**：

            <h3 class="swot-strength">S 优势</h3>
            - [优势1：城市产业红利、院校层次、学科实力等]
            - [优势2：实验室平台、保研考研资源、本地就业认可度等]
            - [优势3：专项计划红利、录取概率优势等]

            <h3 class="swot-weakness">W 劣势（建设性看，全部可补强）</h3>
            - [劣势1：专项计划转专业限制]
            - [劣势2：院校全国知名度不足、城市实习资源薄弱]
            - [劣势3：物价高、岗位内卷]

            <h3 class="swot-opportunity">O 机会</h3>
            - [机会1：国家产业政策扩张、读研深造通道]
            - [机会2：跨城市就业机会、体制内岗位供给]
            - [机会3：专升本通道（专科）]

            <h3 class="swot-threat">T 威胁</h3>
            - [威胁1：行业周期下行、本科学历贬值]
            - [威胁2：AI技术替代、同赛道高分考生竞争]
            - [威胁3：专项计划转专业壁垒]

            <div class="swot-conclusion">
            **博弈辩证结论**：用300-500字说明：
            1. 该套志愿组合属于什么赛道（放大镜赛道/稳定器赛道/保险箱赛道）
            2. 适合什么类型考生，不适合什么类型考生
            3. 核心冲突分析（如院校名气溢价 VS 产业实战能力，城市平台 VS 生活成本）
            </div>

            ## 海枫强烈推荐填报梯队顺序
            基于以上所有分析，输出推荐的填报梯队顺序（搏/冲/稳/保/垫），用3-5句话说明推荐逻辑。

            ## 要求
            1. 大学/专业/城市专项拆解要详细，每个大学500-800字
            2. 综合排名要给出具体分数（0-100分），分数差异要有区分度
            3. 使用Markdown格式输出（**加粗**、- 列表、表格等）
            4. 若某维度数据为空，基于常识判断并说明
            5. SWOT部分必须使用指定的HTML class格式，确保样式正确渲染
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

            // 提取第六、七、八、九部分
            String sixthPartText = extractSection(response, "大学专项拆解");
            String seventhPartText = extractSection(response, "专业专项拆解");
            String eighthPartText = extractSection(response, "城市专项拆解");
            String ninthPartText = extractSection(response, "综合评判");

            List<ReduceResult.HtmlPartResult> sixthPartResults = parseSubPartsFromText(sixthPartText, "大学");
            List<ReduceResult.HtmlPartResult> seventhPartResults = parseSubPartsFromText(seventhPartText, "专业");
            List<ReduceResult.HtmlPartResult> eighthPartResults = parseSubPartsFromText(eighthPartText, "城市");
            ReduceResult.HtmlPartResult ninthPartResult = null;
            if (ninthPartText != null && !ninthPartText.isBlank()) {
                ninthPartResult = ReduceResult.HtmlPartResult.builder()
                        .identifier("综合评判")
                        .title("综合评判")
                        .contentMd(ninthPartText)
                        .build();
            }

            // 提取第十部分：就业前景与展望
            String tenthPartText = extractSection(response, "就业前景与展望");
            ReduceResult.HtmlPartResult soeDirectionResult = null;
            ReduceResult.HtmlPartResult civilServiceResult = null;
            List<ReduceResult.ScoreItem> civilServiceScores = null;
            List<ReduceResult.HtmlPartResult> privateSectorResults = null;
            if (tenthPartText != null && !tenthPartText.isBlank()) {
                String soeText = extractSubSection(tenthPartText, "央国企对口方向");
                if (!soeText.isBlank()) {
                    soeDirectionResult = ReduceResult.HtmlPartResult.builder()
                            .identifier("央国企对口方向")
                            .title("央国企对口方向")
                            .contentMd(soeText)
                            .build();
                }
                String civilText = extractSubSection(tenthPartText, "体制内适配分析");
                if (!civilText.isBlank()) {
                    civilServiceScores = extractScoreItems(civilText);
                    civilServiceResult = ReduceResult.HtmlPartResult.builder()
                            .identifier("体制内适配分析")
                            .title("体制内适配分析")
                            .contentMd(removeMarkdownTables(civilText))
                            .build();
                }
                String privateText = extractSubSection(tenthPartText, "民营企业赛道");
                if (!privateText.isBlank()) {
                    privateSectorResults = parseSubPartsByHeading(privateText, "#### ");
                }
            }

            return ReduceResult.builder()
                    .studentProfile(studentProfile)
                    .macroAnalysis(macroAnalysis)
                    .swot(swot)
                    .recommendation(recommendation)
                    .sixthPartResults(sixthPartResults)
                    .seventhPartResults(seventhPartResults)
                    .eighthPartResults(eighthPartResults)
                    .ninthPartResult(ninthPartResult)
                    .soeDirectionResult(soeDirectionResult)
                    .civilServiceResult(civilServiceResult)
                    .civilServiceScores(civilServiceScores)
                    .privateSectorResults(privateSectorResults)
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

        // 第十部分：就业前景与展望
        if (root.has("soeDirectionResult")) {
            builder.soeDirectionResult(objectMapper.treeToValue(root.get("soeDirectionResult"), ReduceResult.HtmlPartResult.class));
        }
        if (root.has("civilServiceResult")) {
            builder.civilServiceResult(objectMapper.treeToValue(root.get("civilServiceResult"), ReduceResult.HtmlPartResult.class));
        }
        if (root.has("civilServiceScores")) {
            List<ReduceResult.ScoreItem> scores = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : root.get("civilServiceScores")) {
                scores.add(objectMapper.treeToValue(node, ReduceResult.ScoreItem.class));
            }
            builder.civilServiceScores(scores);
        }
        if (root.has("civilServiceChartBase64")) {
            builder.civilServiceChartBase64(root.get("civilServiceChartBase64").asText(null));
        }
        if (root.has("privateSectorResults")) {
            List<ReduceResult.HtmlPartResult> parts = new ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode node : root.get("privateSectorResults")) {
                parts.add(objectMapper.treeToValue(node, ReduceResult.HtmlPartResult.class));
            }
            builder.privateSectorResults(parts);
        }

        return builder.build();
    }

    /**
     * 提取宏观分析部分的完整文本（从"院校分析"或"赛道分类研判"到"SWOT"之前）
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
                // 宏观分析从"赛道分类研判"或"院校分析"开始
                if (line.contains("赛道分类研判") || line.contains("政策红利分析")
                        || line.contains("院校分析") || line.contains("专业分析")
                        || line.contains("城市分析") || line.contains("综合考虑")
                        || line.contains("综合评判")) {
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

        // 解析赛道分类研判
        String trackAnalysis = extractSection(text, "赛道分类研判");
        builder.trackAnalysis(trackAnalysis);

        // 解析政策红利分析
        String policyAnalysis = extractSection(text, "政策红利分析");
        builder.policyAnalysis(policyAnalysis);

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
     * 从专项拆解文本中解析出各子部分（### [名称] 分割）
     */
    private List<ReduceResult.HtmlPartResult> parseSubPartsFromText(String text, String typePrefix) {
        List<ReduceResult.HtmlPartResult> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        String[] lines = text.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentName = null;
        String currentTitle = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("### ")) {
                // 保存上一个
                if (currentName != null && currentContent.length() > 0) {
                    results.add(ReduceResult.HtmlPartResult.builder()
                            .identifier(currentName)
                            .title(currentTitle)
                            .contentMd(currentContent.toString().trim())
                            .build());
                }
                currentTitle = trimmed.substring(4).trim();
                currentName = currentTitle;
                currentContent = new StringBuilder();
            } else if (trimmed.startsWith("#### ") && currentName != null) {
                currentContent.append(line).append("\n");
            } else if (currentName != null) {
                currentContent.append(line).append("\n");
            }
        }
        // 保存最后一个
        if (currentName != null && currentContent.length() > 0) {
            results.add(ReduceResult.HtmlPartResult.builder()
                    .identifier(currentName)
                    .title(currentTitle)
                    .contentMd(currentContent.toString().trim())
                    .build());
        }
        return results;
    }

    /**
     * 从大节文本中提取指定 ### 小节内容（到下一个 ### 或文本末尾）
     */
    private String extractSubSection(String text, String title) {
        if (text == null) return "";
        String[] lines = text.split("\n");
        StringBuilder content = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("### ")) {
                if (inSection) {
                    break;
                }
                if (trimmed.contains(title)) {
                    inSection = true;
                    continue; // 跳过标题行本身
                }
            } else if (inSection) {
                content.append(line).append("\n");
            }
        }
        return content.toString().trim();
    }

    /**
     * 从体制内适配分析文本中解析打分表（| 专业组 | 专业 | 适配度 |）
     */
    private List<ReduceResult.ScoreItem> extractScoreItems(String text) {
        List<ReduceResult.ScoreItem> scores = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("|") || trimmed.startsWith("|--") || trimmed.startsWith("| --")) {
                continue;
            }
            String[] cells = trimmed.split("\\|");
            if (cells.length >= 4) {
                String scoreText = cells[cells.length - 1].trim();
                try {
                    int score = (int) Math.round(Double.parseDouble(scoreText));
                    if (score < 0) score = 0;
                    if (score > 100) score = 100;
                    String majorName = cells[cells.length - 2].trim();
                    if (!majorName.isEmpty()) {
                        String groupName = cells.length >= 5 ? cells[cells.length - 3].trim() : "";
                        scores.add(ReduceResult.ScoreItem.builder()
                                .groupName(groupName)
                                .majorName(majorName)
                                .score(score)
                                .build());
                    }
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse score row: {}", trimmed);
                }
            }
        }
        return scores.isEmpty() ? null : scores;
    }

    /**
     * 移除文本中的 Markdown 表格行（含表头与分隔行）
     */
    private String removeMarkdownTables(String text) {
        String[] lines = text.split("\n");
        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("|")) {
                continue;
            }
            content.append(line).append("\n");
        }
        return content.toString().trim();
    }

    /**
     * 按指定标题前缀（如 "#### "）拆分子模块
     */
    private List<ReduceResult.HtmlPartResult> parseSubPartsByHeading(String text, String headingPrefix) {
        List<ReduceResult.HtmlPartResult> results = new ArrayList<>();
        if (text == null || text.isBlank()) return results;

        String[] lines = text.split("\n");
        StringBuilder currentContent = new StringBuilder();
        String currentName = null;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith(headingPrefix)) {
                if (currentName != null && currentContent.length() > 0) {
                    results.add(ReduceResult.HtmlPartResult.builder()
                            .identifier(currentName)
                            .title(currentName)
                            .contentMd(currentContent.toString().trim())
                            .build());
                }
                currentName = trimmed.substring(headingPrefix.length()).trim();
                currentContent = new StringBuilder();
            } else if (currentName != null) {
                currentContent.append(line).append("\n");
            }
        }
        if (currentName != null && currentContent.length() > 0) {
            results.add(ReduceResult.HtmlPartResult.builder()
                    .identifier(currentName)
                    .title(currentName)
                    .contentMd(currentContent.toString().trim())
                    .build());
        }
        return results;
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

        // 生成 kkfileview 预览 URL
        String previewUrl = null;
        if (report.getStatus() == PdfReportStatus.SUCCESS) {
            String token = generatePreviewToken(recordId);
            String backendUrl = pdfPreviewConfig.getBaseUrl()
                    + "/api/v1/public/pdf/preview/" + recordId
                    + "?token=" + token
                    + "&expire=" + (System.currentTimeMillis() + pdfPreviewConfig.getExpireSeconds() * 1000L);
            String encodedUrl = URLEncoder.encode(backendUrl, StandardCharsets.UTF_8);
            previewUrl = ossProperties.getKkfileviewBaseUrl() + "/onlinePreview?url=" + encodedUrl;
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
                .previewUrl(previewUrl)
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

    @Override
    public String generatePreviewToken(Integer recordId) {
        long expire = System.currentTimeMillis() + pdfPreviewConfig.getExpireSeconds() * 1000L;
        String data = recordId + ":" + expire;
        String sign = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, pdfPreviewConfig.getSecret())
                .hmacHex(data);

        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((data + ":" + sign).getBytes(StandardCharsets.UTF_8));

        String redisKey = RedisKeyConstant.getPdfPreviewTokenKey(token);
        redisTemplate.opsForValue().set(redisKey, String.valueOf(recordId),
                pdfPreviewConfig.getExpireSeconds(), TimeUnit.SECONDS);

        log.debug("PDF preview token generated, recordId={}, expire={}", recordId, expire);
        return token;
    }

    @Override
    public byte[] renderPdf(Integer recordId) {
        return pdfRenderService.renderPdf(recordId);
    }
}
