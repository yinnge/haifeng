package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String positionName;

    @Size(max = 50)
    private String developingUnit;

    @Size(max = 50)
    private String employingUnit;

    @Size(max = 50)
    private String positionCategory;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 50)
    private String district;
    private Integer maxServiceYears;
    @Size(max = 20)
    private String positionStatus;
}
