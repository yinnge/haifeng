package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告记录详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRecordDetailVO {

    private Integer id;
    private Integer planId;
    private String planName;
    private Short status;
    private String mapResults;
    private String reduceResult;
    private String planSnapshot;
    private String failReason;
    private OffsetDateTime createdAt;
    /** kkFileView onlinePreview 预览地址（新窗口打开整页预览用） */
    private String previewUrl;
    /** 后端公开预览直链（带签名，多用途）——供 kkFileView 内置 pdf.js viewer 的 file 参数使用 */
    private String directPdfUrl;
}
