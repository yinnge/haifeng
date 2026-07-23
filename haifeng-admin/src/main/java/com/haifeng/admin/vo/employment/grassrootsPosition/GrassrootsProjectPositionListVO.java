package com.haifeng.admin.vo.employment.grassrootsPosition;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class GrassrootsProjectPositionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String projectType;
    private String year;
    private String positionName;
    private String serviceType;
    private String organizingDept;
    private String serviceUnit;
    private String province;
    private String city;
    private String county;
    private String positionStatus;
}
