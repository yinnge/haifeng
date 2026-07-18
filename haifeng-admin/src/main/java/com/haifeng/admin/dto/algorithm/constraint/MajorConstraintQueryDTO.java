package com.haifeng.admin.dto.algorithm.constraint;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorConstraintQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "专业代码最长50字符")
    private String majorCode;

    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @Size(max = 50, message = "约束代码最长50字符")
    private String constraintCode;

    @Size(max = 100, message = "约束名称最长100字符")
    private String constraintName;
}
