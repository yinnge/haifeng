package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SubjectEvaluationListVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String disciplineCode;
    private String disciplineName;
    private String evaluationRound;
    private String evaluationGrade;
    private Integer status;
    private OffsetDateTime createdAt;
}
