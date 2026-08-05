package com.haifeng.admin.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterprisePositionQueryDTO extends BasePageQueryDTO {
    private Long enterpriseId;
    private String positionName;
    private String recruitmentType;
    private String educationRequirement;
    private String positionStatus;
    private String province;
    private String city;
}