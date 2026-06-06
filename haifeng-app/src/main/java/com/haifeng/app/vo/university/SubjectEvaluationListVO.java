package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端学科评估明细列表 VO（spec §3.5） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String disciplineCode;
    private String disciplineName;
    private String evaluationRound;
    private String evaluationGrade;
}
