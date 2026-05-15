package com.haifeng.app.dto.algorithm.admission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdmissionGroupQueryDTO {
    @NotBlank(message = "批次不能为空")
    private String batch;

    private Boolean subjectFilter = false;

    private Integer page = 1;

    private Integer size = 20;
}
