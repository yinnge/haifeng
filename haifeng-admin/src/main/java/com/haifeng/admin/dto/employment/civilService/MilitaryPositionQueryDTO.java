package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String positionName;

    @Size(max = 50)
    private String employerUnit;

    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String positionType;

    @Size(max = 50)
    private String positionStatus;
}
