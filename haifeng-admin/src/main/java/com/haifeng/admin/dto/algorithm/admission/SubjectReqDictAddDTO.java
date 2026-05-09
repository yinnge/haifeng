package com.haifeng.admin.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SubjectReqDictAddDTO {
    @NotBlank(message = "标准代码不能为空")
    private String code;

    @NotBlank(message = "展示名称不能为空")
    private String displayName;

    @NotNull(message = "严格等级不能为空")
    private Short requirementLevel;

    private List<String> subjects;

    @NotBlank(message = "类型不能为空")
    private String requirementType;

    private Integer sortOrder;
}
