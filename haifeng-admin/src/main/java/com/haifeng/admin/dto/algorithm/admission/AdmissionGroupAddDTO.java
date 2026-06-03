package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AdmissionGroupAddDTO {
    @NotBlank(message = "大学名称不能为空")
    private String universityName;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "批次不能为空")
    private String batch;

    private String enrollmentCode;

    @NotBlank(message = "专业组代码不能为空")
    private String groupCode;

    private String groupName;
    private List<String> subjects;
    private String requirementType;
    private String description;
    private List<String> constraints;
}
