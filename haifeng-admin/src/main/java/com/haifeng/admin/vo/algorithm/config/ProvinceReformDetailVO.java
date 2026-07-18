package com.haifeng.admin.vo.algorithm.config;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ProvinceReformDetailVO {
    private Long id;
    private String province;
    private Short reformYear;
    private String reformModel;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer version;
}
