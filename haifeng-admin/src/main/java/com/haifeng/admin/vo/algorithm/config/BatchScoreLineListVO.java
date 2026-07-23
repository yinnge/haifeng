package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class BatchScoreLineListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String province;
    private Short year;
    private String subjectType;
    private String batch;
    private Integer scoreLine;
}
