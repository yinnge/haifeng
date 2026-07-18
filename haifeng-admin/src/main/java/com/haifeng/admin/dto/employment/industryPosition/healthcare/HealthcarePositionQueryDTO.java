package com.haifeng.admin.dto.employment.industryPosition.healthcare;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HealthcarePositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String institutionName;

    @Size(max = 50)
    private String positionName;

    private String institutionNature;
    private String department;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
}
