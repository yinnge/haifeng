package com.haifeng.admin.dto.algorithm.config;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchScoreLineQueryDTO extends BasePageQueryDTO {
    private String province;
    private Short year;
    private String subjectType;
    private String batch;
    private Integer scoreLine;
}
