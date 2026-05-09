package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BatchScoreLineAddDTO {
    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "科类不能为空")
    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次长度不能超过50")
    private String batch;

    @NotNull(message = "分数线不能为空")
    private Integer scoreLine;

    private Integer rankLine;

    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;
}
