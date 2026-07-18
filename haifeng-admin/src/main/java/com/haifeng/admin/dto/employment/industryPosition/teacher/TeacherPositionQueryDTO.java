package com.haifeng.admin.dto.employment.industryPosition.teacher;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String schoolName;

    @Size(max = 50)
    private String positionName;

    private String schoolType;
    private String province;
    private String city;
    private String district;
    private String positionStatus;
}
