package com.haifeng.admin.vo.industry;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IndustryListVO {

    private Long id;

    /**
     * 行业名称
     */
    private String industryName;

    /**
     * 行业分类
     */
    private String category;

    /**
     * 人才趋势
     */
    private String talentTrend;

    /**
     * 年增长率（%）
     */
    private BigDecimal annualGrowthRate;

    /**
     * 删除状态
     */
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
