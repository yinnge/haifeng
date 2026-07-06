package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String organizingDept;
    private String serviceUnit;
    private String projectType;
    private String year;
    private String serviceType;
    private String province;
    private String city;
    private String county;
    private String positionStatus;
}
