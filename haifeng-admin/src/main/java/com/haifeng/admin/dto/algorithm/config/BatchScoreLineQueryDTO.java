package com.haifeng.admin.dto.algorithm.config;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchScoreLineQueryDTO extends BasePageQueryDTO {

    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    private Short year;

    @Size(max = 20, message = "科类长度不能超过20")
    private String subjectType;

    @Size(max = 50, message = "批次长度不能超过50")
    private String batch;

    private Integer scoreLine;
}
