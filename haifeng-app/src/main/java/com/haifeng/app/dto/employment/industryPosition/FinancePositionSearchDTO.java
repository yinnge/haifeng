package com.haifeng.app.dto.employment.industryPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinancePositionSearchDTO extends BasePageQueryDTO {

    private String keyword;

    private String institutionCategory;

    private String institutionType;

    private String branchName;

    private String positionCategory;

    private String recruitmentType;

    private String province;

    private String city;

    private Integer ageLimit;

    private Integer salaryMin;

    private String positionStatus;
}
