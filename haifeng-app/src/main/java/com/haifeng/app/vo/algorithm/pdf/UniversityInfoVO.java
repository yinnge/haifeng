package com.haifeng.app.vo.algorithm.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 院校详细信息 VO（PDF 第六部分：大学专项拆解）
 * <p>从 University entity 获取静态数据，供 AI 分析与 PDF 展示使用。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityInfoVO {

    /** 院校ID */
    private Long id;

    /** 院校名称 */
    private String name;

    /** 院校类别（综合/理工/师范等） */
    private String category;

    /** 办学层次（本科/专科） */
    private String educationLevel;

    /** 办学性质（公办/民办） */
    private String nature;

    /** 是否拥有博士点 */
    private Boolean hasDoctorate;

    /** 是否拥有硕士点 */
    private Boolean hasMaster;

    /** 保研率（recommendationRate） */
    private BigDecimal recommendationRate;

    /** 推荐年份 */
    private Integer recommendationYear;

    /** 院校标签（985/211/双一流等） */
    private List<String> tags;
}
