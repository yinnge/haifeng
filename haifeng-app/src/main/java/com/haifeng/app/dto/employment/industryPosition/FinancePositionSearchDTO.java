package com.haifeng.app.dto.employment.industryPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinancePositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100)
    private String keyword;

    @Size(max = 50)
    private String institutionCategory;

    @Size(max = 50)
    private String institutionType;

    @Size(max = 50)
    private String branchName;

    @Size(max = 50)
    private String positionCategory;

    @Size(max = 50)
    private String recruitmentType;

    @Size(max = 20)
    private String province;

    @Size(max = 20)
    private String city;

    @Min(value = 0, message = "年龄上限不能为负数")
    private Integer ageLimit;

    @Min(value = 0, message = "最低月薪不能为负数")
    private Integer salaryMin;

    @Size(max = 20)
    private String positionStatus;

    @Size(max = 20)
    private String educationRequirement;

    @Size(max = 20)
    private String degreeRequirement;

    @Size(max = 100)
    private String majorRequirement;
}
