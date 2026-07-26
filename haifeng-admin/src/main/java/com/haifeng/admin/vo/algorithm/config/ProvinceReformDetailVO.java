package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ProvinceReformDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String province;
    private Short reformYear;
    private String reformModel;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer version;
}
