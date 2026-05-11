package com.haifeng.admin.dto.algorithm.constraint;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorConstraintQueryDTO extends BasePageQueryDTO {
    private String majorCode;
    private String majorName;
    private String constraintCode;
    private String constraintName;
}
