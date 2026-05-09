package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

@Data
public class ScoreRankListVO {
    private Long id;
    private String province;
    private Short year;
    private String subjectType;
    private Short score;
    private Integer rank;
}
