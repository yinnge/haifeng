package com.haifeng.app.service.algorithm.pdf;

/**
 * PDF 渲染服务
 * <p>将已生成的 PDF 报告记录（AI 分析结果 + 快照数据）渲染为 PDF 字节流。
 */
public interface PdfRenderService {

    /**
     * 渲染 PDF 报告
     *
     * @param userId   用户ID（权限校验）
     * @param recordId PDF 报告记录ID
     * @return PDF 字节数组
     */
    byte[] renderPdf(Long userId, Integer recordId);
}
