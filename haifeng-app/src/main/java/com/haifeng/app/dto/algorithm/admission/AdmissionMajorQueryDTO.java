package com.haifeng.app.dto.algorithm.admission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdmissionMajorQueryDTO {
    @NotNull(message = "专业组ID不能为空")
    private Integer groupId;

    private Integer page = 1;

    private Integer size = 20;
}
