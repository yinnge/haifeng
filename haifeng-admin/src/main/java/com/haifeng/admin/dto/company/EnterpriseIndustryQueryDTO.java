package com.haifeng.admin.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseIndustryQueryDTO extends BasePageQueryDTO {
    private String enterpriseName;
    private String industryName;
}
