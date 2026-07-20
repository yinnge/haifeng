package com.haifeng.app.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionMajorQueryDTO extends BasePageQueryDTO {

    @NotNull(message = "专业组ID不能为空")
    @Min(value = 1, message = "专业组ID必须大于0")
    private Integer groupId;
}
