package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnterpriseStatusDTO {
    @NotNull(message = "状态不能为空")
    private Boolean isDeleted;
}
