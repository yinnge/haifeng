package com.haifeng.admin.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CivilPositionQueryDTO extends BasePageQueryDTO {
    private String positionName;
    private String recruitingDept;
    private String workLocation;
    private String examType;
    private String regStatus;
    private String minEducation;
}
