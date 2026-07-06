package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.service.algorithm.pdf.AiChatService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.vo.algorithm.pdf.ChatMessage;
import com.haifeng.app.vo.algorithm.pdf.ExportGroupContextVO;
import com.haifeng.app.vo.algorithm.wish.WishExportMajorVO;
import com.haifeng.common.entity.algorithm.wish.WishPlan;
import com.haifeng.common.enums.PdfReportStatus;
import com.haifeng.common.mapper.algorithm.pdf.PdfReportMapper;
import com.haifeng.common.mapper.algorithm.wish.WishPlanMapper;
import com.haifeng.common.entity.algorithm.pdf.PdfReport;
import com.haifeng.common.service.ai.AiQuotaService;
import com.haifeng.app.service.algorithm.GaokaoArchiveService;
import com.haifeng.app.vo.algorithm.GaokaoArchiveVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class PdfReportServiceImplTest {

    private PdfReportMapper pdfReportMapper = mock(PdfReportMapper.class);
    private AiChatService aiChatService = mock(AiChatService.class);
    private AiQuotaService quotaService = mock(AiQuotaService.class);
    private WishPlanService wishPlanService = mock(WishPlanService.class);
    private GaokaoArchiveService gaokaoArchiveService = mock(GaokaoArchiveService.class);
    private WishPlanMapper wishPlanMapper = mock(WishPlanMapper.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    private PdfReportServiceImpl service;

    @BeforeEach
    void setup() {
        service = new PdfReportServiceImpl(
                pdfReportMapper, aiChatService, quotaService,
                wishPlanService, gaokaoArchiveService, objectMapper, wishPlanMapper);
    }

    @Test
    void generateReport_quotaExceeded_emitsError() {
        doThrow(new com.haifeng.common.exception.QuotaExceededException())
                .when(quotaService).incrAndCheck(1L);

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"error\""))
                .verifyComplete();
    }

    @Test
    void generateReport_noExportableGroups_emitsError() {
        WishPlan wishPlan = WishPlan.builder()
                .id(100).memberId(1L).planYear((short)2026).planProvince("北京")
                .reformModel("3+3").planBatch("本科").userScore(615).userRank(8500)
                .deleted(false).build();
        when(wishPlanMapper.selectById(100)).thenReturn(wishPlan);
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.emptyList());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"quota_checked\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"error\""))
                .verifyComplete();
    }

    @Test
    void generateReport_success_emitsMapProgressAndDone() {
        ExportGroupContextVO group1 = ExportGroupContextVO.builder()
                .groupSnapshotId(1)
                .universityId(10L)
                .cityName("北京")
                .groupSortOrder(1)
                .groupCode("001")
                .groupName("专业组1")
                .exportableMajors(Arrays.asList(
                        WishExportMajorVO.builder()
                                .majorId(100L)
                                .safetyLevel(new BigDecimal("0.78"))
                                .levelShort("稳")
                                .build()
                ))
                .build();

        WishPlan wishPlan = WishPlan.builder()
                .id(100).memberId(1L).planYear((short)2026).planProvince("北京")
                .reformModel("3+3").planBatch("本科").userScore(615).userRank(8500)
                .deleted(false).build();

        doNothing().when(quotaService).incrAndCheck(1L);
        when(wishPlanMapper.selectById(100)).thenReturn(wishPlan);
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.singletonList(group1));
        when(aiChatService.chatSync(eq(1L), anyList()))
                .thenReturn("北交大自动化不错");
        when(gaokaoArchiveService.getMyArchive())
                .thenReturn(GaokaoArchiveVO.builder()
                        .gaokaoYear((short) 2026)
                        .gaokaoProvince("北京")
                        .score(615)
                        .rank(8500)
                        .build());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"quota_checked\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map_done\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"done\""))
                .verifyComplete();

        ArgumentCaptor<PdfReport> captor = ArgumentCaptor.forClass(PdfReport.class);
        verify(pdfReportMapper, atLeastOnce()).updateById(captor.capture());
        List<PdfReport> captured = captor.getAllValues();
        assertThat(captured).anyMatch(r -> r.getStatus() == PdfReportStatus.SUCCESS);
    }

    @Test
    void generateReport_mapFails_marksFailedButContinues() {
        ExportGroupContextVO group1 = ExportGroupContextVO.builder()
                .groupSnapshotId(1)
                .universityId(10L)
                .cityName("北京")
                .groupSortOrder(1)
                .groupCode("001")
                .groupName("专业组1")
                .exportableMajors(Arrays.asList(
                        WishExportMajorVO.builder()
                                .majorId(100L)
                                .safetyLevel(new BigDecimal("0.78"))
                                .levelShort("稳")
                                .build()
                ))
                .build();

        WishPlan wishPlan = WishPlan.builder()
                .id(100).memberId(1L).planYear((short)2026).planProvince("北京")
                .reformModel("3+3").planBatch("本科").userScore(615).userRank(8500)
                .deleted(false).build();

        doNothing().when(quotaService).incrAndCheck(1L);
        when(wishPlanMapper.selectById(100)).thenReturn(wishPlan);
        when(wishPlanService.getExportGroupContexts(100))
                .thenReturn(Collections.singletonList(group1));
        when(aiChatService.chatSync(eq(1L), anyList()))
                .thenThrow(new com.haifeng.common.exception.BusinessException(
                        com.haifeng.common.response.ResultCode.AI_ALL_KEYS_FAILED))
                .thenReturn("全局分析结果");
        when(gaokaoArchiveService.getMyArchive())
                .thenReturn(GaokaoArchiveVO.builder()
                        .gaokaoYear((short) 2026)
                        .gaokaoProvince("北京")
                        .score(615)
                        .rank(8500)
                        .build());

        Flux<ServerSentEvent<String>> flux = service.generateReport(1L, 100);

        StepVerifier.create(flux)
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"quota_checked\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"map_done\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"reduce\""))
                .expectNextMatches(sse -> sse.data() != null && sse.data().contains("\"stage\":\"done\""))
                .verifyComplete();

        ArgumentCaptor<PdfReport> captor = ArgumentCaptor.forClass(PdfReport.class);
        verify(pdfReportMapper, atLeastOnce()).updateById(captor.capture());
        List<PdfReport> captured = captor.getAllValues();
        assertThat(captured).anyMatch(r -> r.getStatus() == PdfReportStatus.SUCCESS);
    }
}
