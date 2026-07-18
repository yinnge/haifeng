package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubjectEvaluationAddDTO {
    @NotNull(message = "院校ID不能为空")
    private Long universityId;
    @NotBlank(message = "学科代码不能为空")
    @Size(max = 50, message = "学科代码长度不能超过50")
    private String disciplineCode;
    @NotBlank(message = "学科名称不能为空")
    @Size(max = 50, message = "学科名称长度不能超过50")
    private String disciplineName;
    @Size(max = 20, message = "评估轮次长度不能超过20")
    private String evaluationRound;
    @NotBlank(message = "评估等级不能为空")
    @Pattern(regexp = "^(A\\+|A|A-|B\\+|B|B-|C\\+|C|C-)$", message = "评估等级格式不正确")
    private String evaluationGrade;
    private Integer sortOrder;
}
