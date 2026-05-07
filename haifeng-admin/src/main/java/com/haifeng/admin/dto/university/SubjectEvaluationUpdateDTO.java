package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SubjectEvaluationUpdateDTO {
    private String disciplineCode;
    private String disciplineName;
    private String evaluationRound;
    @Pattern(regexp = "^(A\\+|A|A-|B\\+|B|B-|C\\+|C|C-)$", message = "评估等级格式不正确")
    private String evaluationGrade;
    private Integer sortOrder;
    private Integer status;
}
