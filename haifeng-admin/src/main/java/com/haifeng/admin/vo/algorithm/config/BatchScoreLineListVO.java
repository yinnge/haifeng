package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

@Data
public class BatchScoreLineListVO {
    private Long id;
    private String province;
    private Short year;
    private String subjectType;
    private String batch;
    private Integer scoreLine;
}
