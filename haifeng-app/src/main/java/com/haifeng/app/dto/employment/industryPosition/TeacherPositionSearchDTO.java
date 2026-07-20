package com.haifeng.app.dto.employment.industryPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100)
    private String keyword;

    @Size(max = 50)
    private String schoolType;

    @Size(max = 50)
    private String schoolNature;

    @Size(max = 50)
    private String subject;

    @Min(value = 0, message = "招聘人数不能为负数")
    private Integer recruitmentCount;

    @Min(value = 0, message = "年龄上限不能为负数")
    private Integer ageLimit;

    @Size(max = 20)
    private String province;

    @Size(max = 20)
    private String city;

    @Size(max = 20)
    private String district;

    @Size(max = 20)
    private String positionStatus;

    @Size(max = 20)
    private String educationRequirement;

    @Size(max = 20)
    private String degreeRequirement;

    @Size(max = 100)
    private String majorRequirement;
}
