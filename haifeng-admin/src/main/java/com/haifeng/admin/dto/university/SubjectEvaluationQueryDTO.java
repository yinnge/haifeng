package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectEvaluationQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "院校名称长度不能超过50")
    private String universityName;
    @Size(max = 50, message = "学科代码长度不能超过50")
    private String disciplineCode;
    @Size(max = 50, message = "学科名称长度不能超过50")
    private String disciplineName;
    @Size(max = 20, message = "评估轮次长度不能超过20")
    private String evaluationRound;
    @Size(max = 10, message = "评估等级长度不能超过10")
    private String evaluationGrade;
    private Integer status;
}
