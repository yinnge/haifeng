package com.haifeng.admin.dto.resource;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceStatusDTO {

    @NotNull(message = "删除状态不能为空")
    private Boolean isDeleted;
}
