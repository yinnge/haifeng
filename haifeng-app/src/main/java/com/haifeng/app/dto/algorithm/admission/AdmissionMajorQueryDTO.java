package com.haifeng.app.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionMajorQueryDTO extends BasePageQueryDTO {

    @NotNull(message = "专业组ID不能为空")
    @Min(value = 1, message = "专业组ID必须大于0")
    private Integer groupId;

    @Size(max = 100, message = "专业名称不能超过100字")
    private String majorName;

    @Size(max = 20, message = "专业代码不能超过20字")
    private String majorCode;
}
