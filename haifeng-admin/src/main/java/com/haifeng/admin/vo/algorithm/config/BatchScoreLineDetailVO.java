package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class BatchScoreLineDetailVO {
    private Long id;
    private String province;
    private Short year;
    private String subjectType;
    private String batch;
    private Integer scoreLine;
    private Integer rankLine;
    private String remark;
    private OffsetDateTime createdAt;
}
