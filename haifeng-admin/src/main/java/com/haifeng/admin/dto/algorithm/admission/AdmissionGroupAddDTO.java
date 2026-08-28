package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class AdmissionGroupAddDTO {
    @NotBlank(message = "大学名称不能为空")
    @Size(max = 50, message = "大学名称不能超过50个字符")
    private String universityName;

    @NotNull(message = "年份不能为空")
    private Short year;

    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份不能超过20个字符")
    private String province;

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次不能超过50个字符")
    private String batch;

    @Size(max = 30, message = "招生代码不能超过30个字符")
    private String enrollmentCode;

    @NotBlank(message = "专业组代码不能为空")
    @Size(max = 30, message = "专业组代码不能超过30个字符")
    private String groupCode;

    @Size(max = 100, message = "专业组名称不能超过100个字符")
    private String groupName;
    private List<String> subjects;

    @Size(max = 10, message = "选科要求类型不能超过10个字符")
    private String requirementType;
    private String description;
}
