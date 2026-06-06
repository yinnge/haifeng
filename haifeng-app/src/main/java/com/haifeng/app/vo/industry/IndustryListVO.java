package com.haifeng.app.vo.industry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * C 端行业列表 VO（任务 2 接口 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String industryName;
    private String category;
    private String description;
    private BigDecimal annualGrowthRate;
    private String marketScale;
    private String talentGap;
    private BigDecimal investmentHeat;
}
