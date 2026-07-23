package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationListVO implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
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
