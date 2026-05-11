package com.haifeng.admin.dto.algorithm.constraint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MajorConstraintAddDTO {
    @NotBlank(message = "专业名称不能为空")
    @Size(max = 100, message = "专业名称最长100字符")
    private String majorName;

    @NotBlank(message = "约束名称不能为空")
    @Size(max = 100, message = "约束名称最长100字符")
    private String constraintName;

    @Size(max = 200, message = "备注最长200字符")
    private String remark;
}
