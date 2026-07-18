package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String universityName;

    private Short year;

    @Size(max = 50)
    private String province;

    @Size(max = 50)
    private String requirementType;

    @Size(max = 50)
    private String enrollmentCode;

    @Size(max = 50)
    private String groupCode;

    @Size(max = 50)
    private String groupName;

    private Boolean isDeleted;
}
