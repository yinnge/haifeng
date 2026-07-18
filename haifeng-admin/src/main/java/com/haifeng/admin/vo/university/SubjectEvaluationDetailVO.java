package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationDetailVO implements Serializable {
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
