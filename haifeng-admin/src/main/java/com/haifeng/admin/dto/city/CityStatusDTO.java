package com.haifeng.admin.dto.city;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CityStatusDTO {

    @NotNull(message = "删除状态不能为空")
    private Boolean isDeleted;
}
