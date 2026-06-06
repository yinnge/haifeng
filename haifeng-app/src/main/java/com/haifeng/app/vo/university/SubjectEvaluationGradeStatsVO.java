package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端学科评估等级统计 VO（spec §3.6）
 * grade 取值固定为 A+/A/A-/B+/B/B-/C+/C/C- 之一；count 缺数据时为 0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectEvaluationGradeStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String grade;
    private Integer count;
}
