package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdmissionMajorScoreAddDTO {
    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    private Long majorId;

    @NotBlank(message = "专业代码不能为空")
    private String majorCode;

    @NotBlank(message = "专业名称不能为空")
    private String majorName;

    private String subjectRequirements;
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
}
