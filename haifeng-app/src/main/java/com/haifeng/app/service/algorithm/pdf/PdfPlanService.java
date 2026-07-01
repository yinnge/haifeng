package com.haifeng.app.service.algorithm.pdf;

import com.haifeng.app.vo.algorithm.pdf.PdfCityVO;
import com.haifeng.app.vo.algorithm.pdf.PdfMajorVO;
import com.haifeng.app.vo.algorithm.pdf.PdfUniversityVO;

import java.util.List;

/**
 * PDF 导出服务（university / city / major 三维度）
 * <p>框架阶段：接口签名已定义，impl 体与 AI 编排、PDF 生成留待任务二实现。
 */
public interface PdfPlanService {

    /**
     * 大学维度数据聚合
     * <p>对每个非跳过专业组调用 {@link com.haifeng.app.service.university.UniversityService#detail}。
     */
    List<PdfUniversityVO> aggregateUniversities(Integer planId);

    /**
     * 城市维度数据聚合
     * <p>按 cityName 聚合专业组，调用 {@link com.haifeng.app.service.city.CityService#detailByName}。
     */
    List<PdfCityVO> aggregateCities(Integer planId);

    /**
     * 专业维度数据聚合
     * <p>对每个可导出专业调用 {@link com.haifeng.app.service.major.MajorService#detail}。
     */
    List<PdfMajorVO> aggregateMajors(Integer planId);

    /**
     * 大学维度导出（AI 分析 + PDF 生成）
     * <p>TODO：接多智能体编排 + SSE 流式返回 + Thymeleaf/OpenHTMLtoPDF。
     */
    Object exportUniversityPdf(Integer planId);

    /**
     * 城市维度导出（AI 分析 + PDF 生成）
     * <p>TODO：接多智能体编排 + SSE 流式返回 + Thymeleaf/OpenHTMLtoPDF。
     */
    Object exportCityPdf(Integer planId);

    /**
     * 专业维度导出（AI 分析 + PDF 生成）
     * <p>TODO：接多智能体编排 + SSE 流式返回 + Thymeleaf/OpenHTMLtoPDF。
     */
    Object exportMajorPdf(Integer planId);
}
