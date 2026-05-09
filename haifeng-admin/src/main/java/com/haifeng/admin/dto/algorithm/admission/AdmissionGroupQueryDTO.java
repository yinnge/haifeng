package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {
    private String universityName;
    private Short year;
    private String province;
    private String subjectType;
    private String enrollmentCode;
    private String groupCode;
    private String groupName;
    private Boolean isDeleted;
}
