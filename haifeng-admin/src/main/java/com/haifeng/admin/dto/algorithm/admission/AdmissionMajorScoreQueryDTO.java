package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionMajorScoreQueryDTO extends BasePageQueryDTO {
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private Boolean isDeleted;
}
