package com.haifeng.admin.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubjectReqDictQueryDTO extends BasePageQueryDTO {
    private String code;
    private String displayName;
    private String requirementType;
}
