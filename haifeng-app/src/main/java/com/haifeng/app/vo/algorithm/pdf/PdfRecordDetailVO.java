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
}
