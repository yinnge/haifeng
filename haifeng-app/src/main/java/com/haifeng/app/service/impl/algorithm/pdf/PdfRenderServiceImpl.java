package com.haifeng.app.service.impl.algorithm.pdf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.app.service.algorithm.pdf.PdfRenderService;
import com.haifeng.app.util.algorithm.pdf.EnrichmentLoader;
import com.haifeng.app.vo.algorithm.pdf.CityEnrichmentVO;
import com.haifeng.app.vo.algorithm.pdf.MapResultItem;
import com.haifeng.app.vo.algorithm.pdf.MajorEnrichmentVO;
import com.haifeng.app.vo.algorithm.pdf.PdfRenderData;
import com.haifeng.app.vo.algorithm.pdf.PlanSnapshot;
import com.haifeng.app.vo.algorithm.pdf.ReduceResult;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.entity.algorithm.wish.WishGroupSnapshot;
import com.haifeng.common.entity.algorithm.wish.WishMajorSnapshot;
import com.haifeng.common.enums.PdfReportStatus;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.mapper.algorithm.wish.WishGroupSnapshotMapper;
import com.haifeng.common.mapper.algorithm.wish.WishMajorSnapshotMapper;
import com.haifeng.common.response.ResultCode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdfRenderServiceImpl implements PdfRenderService {

    private static final String LOGO_URL =
            "https://img.imgos.cn/cdn/21/20260513/852338961e39f515618e03596c4b2e71.png";
    private static final String FONT_FAMILY = "Noto Sans SC";

    private final PdfReportMapper pdfReportMapper;
    private final WishGroupSnapshotMapper wishGroupSnapshotMapper;
    private final WishMajorSnapshotMapper wishMajorSnapshotMapper;
    private final ObjectMapper objectMapper;
    private final EnrichmentLoader enrichmentLoader;

    private final Parser flexmarkParser;
    private final HtmlRenderer flexmarkRenderer;
    private final TemplateEngine templateEngine;

    private volatile String cachedLogoDataUri;

    private record CachedPdf(byte[] data, OffsetDateTime updatedAt) {}
    private final ConcurrentHashMap<Integer, CachedPdf> pdfCache = new ConcurrentHashMap<>();

    public PdfRenderServiceImpl(PdfReportMapper pdfReportMapper,
                                 WishGroupSnapshotMapper wishGroupSnapshotMapper,
                                 WishMajorSnapshotMapper wishMajorSnapshotMapper,
                                 ObjectMapper objectMapper,
                                 EnrichmentLoader enrichmentLoader) {
        this.pdfReportMapper = pdfReportMapper;
        this.wishGroupSnapshotMapper = wishGroupSnapshotMapper;
        this.wishMajorSnapshotMapper = wishMajorSnapshotMapper;
        this.objectMapper = objectMapper;
        this.enrichmentLoader = enrichmentLoader;

        this.flexmarkParser = Parser.builder().build();
        this.flexmarkRenderer = HtmlRenderer.builder().build();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(resolver);
    }

    @Override
    public byte[] renderPdf(Long userId, Integer recordId) {
        log.info("Rendering PDF, userId={}, recordId={}", userId, recordId);

        // 1. 加载报告记录
        PdfReport report = pdfReportMapper.selectById(recordId);
        if (report == null || !userId.equals(report.getMemberId())
                || Boolean.TRUE.equals(report.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报告记录不存在");
        }
        if (report.getStatus() != PdfReportStatus.SUCCESS) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "报告尚未生成完成");
        }

        // 2. 检查缓存
        CachedPdf cached = pdfCache.get(recordId);
        if (cached != null && cached.updatedAt().equals(report.getUpdatedAt())) {
            log.debug("PDF cache hit, recordId={}", recordId);
            return cached.data();
        }

        // 2. 解析 JSONB
        PlanSnapshot planSnapshot = parseJson(report.getPlanSnapshot(), PlanSnapshot.class);
        List<MapResultItem> mapResults = parseJsonList(report.getMapResults(), MapResultItem.class);
        ReduceResult reduceResult = parseJson(report.getReduceResult(), ReduceResult.class);

        // 3. 加载快照数据
        Integer planId = report.getPlanId();
        List<WishGroupSnapshot> groupSnapshots = wishGroupSnapshotMapper.selectList(
                new LambdaQueryWrapper<WishGroupSnapshot>()
                        .eq(WishGroupSnapshot::getPlanId, planId)
                        .orderByAsc(WishGroupSnapshot::getGroupSortOrder));

        // 3.1 批量加载所有专业快照（避免按组逐次查询）
        List<Integer> groupSnapshotIds = groupSnapshots.stream()
                .map(WishGroupSnapshot::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        Map<Integer, List<WishMajorSnapshot>> majorsByGroup = new HashMap<>();
        if (!groupSnapshotIds.isEmpty()) {
            List<WishMajorSnapshot> allMajors = wishMajorSnapshotMapper.selectList(
                    new LambdaQueryWrapper<WishMajorSnapshot>()
                            .in(WishMajorSnapshot::getGroupSnapshotId, groupSnapshotIds)
                            .eq(WishMajorSnapshot::getIsExported, true)
                            .orderByAsc(WishMajorSnapshot::getMajorSortOrder));
            for (WishMajorSnapshot m : allMajors) {
                majorsByGroup.computeIfAbsent(m.getGroupSnapshotId(), k -> new ArrayList<>()).add(m);
            }
        }

        // 3.2 批量加载所有专业增强数据（避免 N+1 查询）
        List<Long> allMajorIds = majorsByGroup.values().stream()
                .flatMap(List::stream)
                .map(WishMajorSnapshot::getMajorId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MajorEnrichmentVO> majorEnrichmentMap = enrichmentLoader.loadMajorsBatch(allMajorIds);

        // 4. 组装渲染数据
        List<PdfRenderData.GroupRenderData> groupRenderList = new ArrayList<>();
        List<PdfRenderData.SummaryRow> summaryRows = new ArrayList<>();
        Map<String, CityEnrichmentVO> cityEnrichmentCache = new HashMap<>();

        for (WishGroupSnapshot gs : groupSnapshots) {
            // 匹配 AI 结果
            MapResultItem aiResult = mapResults.stream()
                    .filter(m -> gs.getId().equals(m.getGroupSnapshotId()))
                    .findFirst()
                    .orElse(null);

            // 从批量加载结果中获取可导出专业
            List<WishMajorSnapshot> majorSnapshots = majorsByGroup.getOrDefault(gs.getId(), Collections.emptyList());

            if (majorSnapshots.isEmpty()) {
                continue;
            }

            // 加载城市增强数据（同城缓存）
            CityEnrichmentVO cityEnrichment = gs.getCityName() != null
                    ? cityEnrichmentCache.computeIfAbsent(gs.getCityName(), enrichmentLoader::loadCity)
                    : null;

            List<PdfRenderData.MajorRenderData> majorRenderList = majorSnapshots.stream()
                    .map(m -> {
                        // 汇总表行
                        summaryRows.add(PdfRenderData.SummaryRow.builder()
                                .universityName(gs.getUniversityName())
                                .groupName(gs.getGroupName())
                                .groupCode(gs.getGroupCode())
                                .majorName(m.getMajorName())
                                .majorCode(m.getMajorCode())
                                .levelShort(m.getLevelShort())
                                .safetyLevel(m.getSafetyLevel())
                                .tuition(m.getTuition())
                                .cityName(gs.getCityName())
                                .build());

                        // 历史录取分
                        List<PdfRenderData.HistoryScoreRender> historyRenders = m.getHistoryScores() != null
                                ? m.getHistoryScores().stream()
                                        .map(h -> PdfRenderData.HistoryScoreRender.builder()
                                                .year(h.getYear())
                                                .minScore(h.getMinScore())
                                                .minRank(h.getMinRank())
                                                .avgScore(h.getAvgScore())
                                                .avgRank(h.getAvgRank())
                                                .maxScore(h.getMaxScore())
                                                .maxRank(h.getMaxRank())
                                                .admissionCount(h.getAdmissionCount())
                                                .build())
                                        .collect(Collectors.toList())
                                : null;

                        // 从批量加载结果中获取专业增强数据
                        MajorEnrichmentVO majorEnrichment = m.getMajorId() != null
                                ? majorEnrichmentMap.get(m.getMajorId()) : null;

                        return PdfRenderData.MajorRenderData.builder()
                                .majorId(m.getMajorId())
                                .majorName(m.getMajorName())
                                .majorCode(m.getMajorCode())
                                .duration(m.getDuration())
                                .tuition(m.getTuition())
                                .admissionCount(m.getAdmissionCount())
                                .safetyLevel(m.getSafetyLevel())
                                .levelShort(m.getLevelShort())
                                .historyScores(historyRenders)
                                .majorEnrichment(majorEnrichment)
                                .build();
                    })
                    .collect(Collectors.toList());

            // AI 评语 Markdown → HTML
            String commentaryHtml = null;
            Boolean aiSuccess = false;
            if (aiResult != null) {
                aiSuccess = aiResult.getSuccess();
                if (aiResult.getCommentary() != null && !aiResult.getCommentary().isBlank()) {
                    commentaryHtml = markdownToHtml(aiResult.getCommentary());
                }
            }

            groupRenderList.add(PdfRenderData.GroupRenderData.builder()
                    .groupSnapshotId(gs.getId())
                    .universityId(gs.getUniversityId())
                    .universityName(gs.getUniversityName())
                    .cityName(gs.getCityName())
                    .groupCode(gs.getGroupCode())
                    .groupName(gs.getGroupName())
                    .category(gs.getCategory())
                    .nature(gs.getNature())
                    .tags(gs.getTags())
                    .subjects(gs.getSubjects())
                    .constraintsDescription(gs.getConstraintsDescription())
                    .groupSortOrder(gs.getGroupSortOrder())
                    .commentaryHtml(commentaryHtml)
                    .aiSuccess(aiSuccess)
                    .cityEnrichment(cityEnrichment)
                    .majors(majorRenderList)
                    .build());
        }

        // 5. Reduce 结果 Markdown → HTML
        String globalAnalysisHtml = reduceResult != null && reduceResult.getGlobalAnalysis() != null
                ? markdownToHtml(reduceResult.getGlobalAnalysis()) : "";
        String swotHtml = reduceResult != null && reduceResult.getSwot() != null
                ? markdownToHtml(reduceResult.getSwot()) : "";
        String recommendationHtml = reduceResult != null && reduceResult.getRecommendation() != null
                ? markdownToHtml(reduceResult.getRecommendation()) : "";

        // 6. 组装 PdfRenderData
        PdfRenderData data = PdfRenderData.builder()
                .planYear(planSnapshot != null ? planSnapshot.getPlanYear() : null)
                .planProvince(planSnapshot != null ? planSnapshot.getPlanProvince() : null)
                .reformModel(planSnapshot != null ? planSnapshot.getReformModel() : null)
                .userScore(planSnapshot != null ? planSnapshot.getUserScore() : null)
                .userRank(planSnapshot != null ? planSnapshot.getUserRank() : null)
                .planBatch(planSnapshot != null ? planSnapshot.getPlanBatch() : null)
                .generatedAt(OffsetDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy年MM月dd日")))
                .logoDataUri(getLogoDataUri())
                .globalAnalysisHtml(globalAnalysisHtml)
                .swotHtml(swotHtml)
                .recommendationHtml(recommendationHtml)
                .summaryRows(summaryRows)
                .groups(groupRenderList)
                .build();

        // 7. Thymeleaf 渲染
        Context context = new Context();
        context.setVariable("data", data);
        String html = templateEngine.process("pdf-report", context);

        // 8. OpenHTMLtoPDF 渲染
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            registerFont(builder);
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            log.info("PDF rendered successfully, recordId={}, size={}bytes", recordId, os.size());
            byte[] pdfBytes = os.toByteArray();
            pdfCache.put(recordId, new CachedPdf(pdfBytes, report.getUpdatedAt()));
            return pdfBytes;
        } catch (Exception e) {
            log.error("PDF rendering failed, recordId={}", recordId, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "PDF渲染失败: " + e.getMessage());
        }
    }

    private String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) return "";
        Node document = flexmarkParser.parse(markdown);
        return flexmarkRenderer.render(document);
    }

    private void registerFont(PdfRendererBuilder builder) {
        // 优先使用 classpath 字体
        if (getClass().getResource("/fonts/NotoSansSC-Regular.ttf") != null) {
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/NotoSansSC-Regular.ttf"),
                    FONT_FAMILY);
            log.debug("Registered classpath CJK font");
            return;
        }
        // 回退到 Windows 系统字体
        String[] systemFonts = {
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc"
        };
        for (String fontPath : systemFonts) {
            if (new File(fontPath).exists()) {
                final String path = fontPath;
                builder.useFont(() -> {
                    try {
                        return new FileInputStream(path);
                    } catch (java.io.FileNotFoundException e) {
                        log.error("Font file not found: {}", path);
                        return null;
                    }
                }, FONT_FAMILY);
                log.debug("Registered system CJK font: {}", fontPath);
                return;
            }
        }
        log.warn("No CJK font found, Chinese characters may not render correctly in PDF");
    }

    private synchronized String getLogoDataUri() {
        if (cachedLogoDataUri != null) return cachedLogoDataUri;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(LOGO_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            try (InputStream is = conn.getInputStream();
                 ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) != -1) {
                    os.write(buffer, 0, n);
                }
                byte[] imageBytes = os.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                cachedLogoDataUri = "data:image/png;base64," + base64;
                log.info("Logo downloaded and cached");
            }
        } catch (Exception e) {
            log.warn("Failed to download logo: {}", e.getMessage());
            // M2: 失败时不缓存空串，保持 null 以便下次请求可以重试
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return cachedLogoDataUri;
    }

    private <T> T parseJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to parse JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private <T> List<T> parseJsonList(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("Failed to parse JSON list to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Collections.emptyList();
        }
    }
}
