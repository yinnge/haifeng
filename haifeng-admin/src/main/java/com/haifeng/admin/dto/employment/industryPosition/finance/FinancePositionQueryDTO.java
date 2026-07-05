package com.haifeng.admin.dto.employment.industryPosition.finance;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinancePositionQueryDTO extends BasePageQueryDTO {
    private String institutionName;
    private String positionName;
    private String institutionCategory;
    private String institutionType;
    private String province;
    private String city;
    private String positionStatus;
}
