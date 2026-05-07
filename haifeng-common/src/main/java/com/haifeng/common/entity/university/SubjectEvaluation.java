package com.haifeng.common.entity.university;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_subject_evaluation")
public class SubjectEvaluation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long universityId;

    private String universityName;

    private String disciplineCode;

    private String disciplineName;

    private String evaluationRound;

    private String evaluationGrade;

    private Integer sortOrder;

    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
