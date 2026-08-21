package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AdmissionMajorScoreAddDTO {
    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Long majorId;

    @NotBlank(message = "专业代码不能为空")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private Integer admissionCount;
    private Integer minScore;
    private Integer minRank;
    private BigDecimal avgScore;
    private Integer avgRank;
    private Integer maxScore;
    private Integer maxRank;
    private List<String> constraints;

    /**
     * 可选：多年度分数数组（优先于平铺分数字段）。
     * <p>元素结构：{ year, admissionCount, minScore, minRank, avgScore, avgRank, maxScore, maxRank }。
     * 新增时整体作为 history；修改时整体替换 history（以表单提交为准）。
     */
    private List<Map<String, Object>> history;
}
