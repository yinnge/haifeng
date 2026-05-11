package com.haifeng.admin.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseQueryDTO extends BasePageQueryDTO {
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String recruitmentStatus;
    private Boolean isDeleted;
}
