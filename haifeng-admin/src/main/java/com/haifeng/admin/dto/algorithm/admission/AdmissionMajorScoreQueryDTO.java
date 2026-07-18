package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionMajorScoreQueryDTO extends BasePageQueryDTO {
    private Integer groupId;

    @Size(max = 50)
    private String majorCode;

    @Size(max = 50)
    private String majorName;

    private String educationLevel;
    private Boolean isDeleted;
}
