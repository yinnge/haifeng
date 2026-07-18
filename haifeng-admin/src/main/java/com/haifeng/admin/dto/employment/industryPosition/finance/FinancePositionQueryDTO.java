package com.haifeng.admin.dto.employment.industryPosition.finance;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinancePositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String institutionName;

    @Size(max = 50)
    private String positionName;

    private String institutionCategory;
    private String institutionType;
    private String province;
    private String city;
    private String positionStatus;
}
