package com.haifeng.app.service.impl.algorithm.pdf;

import com.haifeng.app.service.algorithm.pdf.PdfPlanService;
import com.haifeng.app.service.algorithm.wish.WishPlanService;
import com.haifeng.app.service.city.CityService;
import com.haifeng.app.service.major.MajorService;
import com.haifeng.app.service.university.UniversityService;
import com.haifeng.app.vo.algorithm.pdf.PdfCityVO;
import com.haifeng.app.vo.algorithm.pdf.PdfMajorVO;
import com.haifeng.app.vo.algorithm.pdf.PdfUniversityVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * PDF 导出服务实现（框架 stub）
 * <p>依赖已注入，方法体留 TODO，待任务二接入多智能体编排与 PDF 生成。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfPlanServiceImpl implements PdfPlanService {

    private final WishPlanService wishPlanService;
    private final UniversityService universityService;
    private final CityService cityService;
    private final MajorService majorService;

    @Override
    public List<PdfUniversityVO> aggregateUniversities(Integer planId) {
        // TODO 任务二：遍历 wishPlanService.getExportGroupContexts(planId)，
        //  对每个 group 调 universityService.detail(group.getUniversityId())，组装 PdfUniversityVO
        log.debug("aggregateUniversities TODO, planId={}", planId);
        return Collections.emptyList();
    }

    @Override
    public List<PdfCityVO> aggregateCities(Integer planId) {
        // TODO 任务二：按 cityName 分组 wishPlanService.getExportGroupContexts(planId)，
        //  对每个城市调 cityService.detailByName(cityName)，组装 PdfCityVO
        log.debug("aggregateCities TODO, planId={}", planId);
        return Collections.emptyList();
    }

    @Override
    public List<PdfMajorVO> aggregateMajors(Integer planId) {
        // TODO 任务二：遍历 wishPlanService.getExportGroupContexts(planId) 的每个可导出专业，
        //  调 majorService.detail(majorId)，组装 PdfMajorVO
        log.debug("aggregateMajors TODO, planId={}", planId);
        return Collections.emptyList();
    }

    @Override
    public Object exportUniversityPdf(Integer planId) {
        // TODO 任务二：多智能体编排 + SSE 流式 + Thymeleaf/OpenHTMLtoPDF
        log.debug("exportUniversityPdf TODO, planId={}", planId);
        return null;
    }

    @Override
    public Object exportCityPdf(Integer planId) {
        // TODO 任务二：多智能体编排 + SSE 流式 + Thymeleaf/OpenHTMLtoPDF
        log.debug("exportCityPdf TODO, planId={}", planId);
        return null;
    }

    @Override
    public Object exportMajorPdf(Integer planId) {
        // TODO 任务二：多智能体编排 + SSE 流式 + Thymeleaf/OpenHTMLtoPDF
        log.debug("exportMajorPdf TODO, planId={}", planId);
        return null;
    }
}
