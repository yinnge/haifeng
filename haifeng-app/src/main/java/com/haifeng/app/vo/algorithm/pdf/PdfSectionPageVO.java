package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PDF 报告章节目录项（前端目录栏用，与报告 PDF 目录页一致）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfSectionPageVO {

    /** 目录序号：一 / 二 / ... / 十一 */
    private String num;

    /** 章节标题（不带序号） */
    private String title;

    /** 章节在 PDF 中的起始页码（1-based；未定位到为 null） */
    private Integer page;
}
