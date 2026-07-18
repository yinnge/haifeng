package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScoreRankAddDTO {
    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotNull(message = "分数不能为空")
    private Short score;

    @NotNull(message = "位次不能为空")
    private Integer rank;

    @Min(value = 0, message = "同分人数不能为负")
    private Integer sameScoreCount;

    @Min(value = 0, message = "累计人数不能为负")
    private Integer cumulativeCount;
}
