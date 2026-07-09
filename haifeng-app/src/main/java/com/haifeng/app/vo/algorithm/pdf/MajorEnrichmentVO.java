package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 专业增强数据（PDF 报告用）
 * <p>从 t_major + t_major_detail 提取，供 AI 分析与 PDF 静态展示使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorEnrichmentVO {

    private String majorName;
    private String majorCategory;
    private String parentCategory;
    private String majorTags;
    private String degreeAwarded;
    private BigDecimal employmentRate;
    private Integer salaryMin;
    private Integer salaryMax;
    private String careerProspect;
    private String description;
}
