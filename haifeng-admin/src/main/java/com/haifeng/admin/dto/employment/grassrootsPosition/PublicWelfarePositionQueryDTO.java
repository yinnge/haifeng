package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String developingUnit;
    private String employingUnit;
    private String positionCategory;
    private String province;
    private String city;
    private String district;
    private Integer maxServiceYears;
    private String positionStatus;
}
