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
}
