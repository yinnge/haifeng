package com.haifeng.app.service.algorithm.pdf;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.haifeng.app.dto.algorithm.pdf.PdfRecordQueryDTO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordDetailVO;
import com.haifeng.app.vo.algorithm.pdf.PdfRecordListVO;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

/**
 * PDF 报告生成与历史记录服务
 */
public interface PdfReportService {

    /**
     * 生成 PDF 报告（SSE 流式返回进度）
     *
     * @param userId 用户ID
     * @param planId 志愿方案ID
     * @return SSE 进度事件流
     */
    Flux<ServerSentEvent<String>> generateReport(Long userId, Integer planId);

    /**
     * 分页查询历史报告记录
     *
     * @param userId 用户ID
     * @param dto    分页参数
     * @return 分页结果
     */
    IPage<PdfRecordListVO> pageRecords(Long userId, PdfRecordQueryDTO dto);

    /**
     * 查询报告记录详情
     *
     * @param userId   用户ID
     * @param recordId 报告记录ID
     * @return 记录详情
     */
    PdfRecordDetailVO getRecordDetail(Long userId, Integer recordId);

    /**
     * 渲染 PDF 报告为字节流
     *
     * @param userId   用户ID（权限校验）
     * @param recordId 报告记录ID
     * @return PDF 字节数组
     */
    byte[] renderPdf(Long userId, Integer recordId);

    /**
     * 获取 PDF 下载文件名
     *
     * @param userId   用户ID（权限校验）
     * @param recordId 报告记录ID
     * @return 文件名（不含 .pdf 后缀）
     */
    String getDownloadFilename(Long userId, Integer recordId);

    /**
     * 重新生成 PDF 报告（SSE 流式返回进度）
     * <p>失败记录不扣配额，成功记录扣配额。
     *
     * @param userId   用户ID
     * @param recordId 报告记录ID
     * @return SSE 进度事件流
     */
    Flux<ServerSentEvent<String>> regenerateReport(Long userId, Integer recordId);

    /**
     * 删除报告记录（软删除）
     *
     * @param userId   用户ID
     * @param recordId 报告记录ID
     */
    void deleteRecord(Long userId, Integer recordId);

    /**
     * 生成 PDF 预览签名 token（存 Redis，一次性使用）
     *
     * @param recordId 报告记录ID
     * @return 签名 token
     */
    String generatePreviewToken(Integer recordId);

    /**
     * 不校验 userId 的 PDF 渲染（给公开预览端点用）
     *
     * @param recordId 报告记录ID
     * @return PDF 字节数组
     */
    byte[] renderPdf(Integer recordId);
}
