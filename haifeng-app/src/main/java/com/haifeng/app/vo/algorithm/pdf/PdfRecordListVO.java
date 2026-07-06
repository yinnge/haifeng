package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * PDF 报告历史记录列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRecordListVO {

    private Integer id;
    private Integer planId;
    private String planName;
    private Short status;
    private OffsetDateTime createdAt;
}
