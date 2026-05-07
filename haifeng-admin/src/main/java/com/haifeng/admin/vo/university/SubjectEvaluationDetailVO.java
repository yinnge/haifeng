package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SubjectEvaluationDetailVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String disciplineCode;
    private String disciplineName;
    private String evaluationRound;
    private String evaluationGrade;
    private Integer sortOrder;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
