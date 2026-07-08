package com.haifeng.common.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BasePageQueryDTO {

    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Min(value = 10, message = "每页最小10条")
    @Max(value = 100, message = "每页最大100条")
    private Integer size = 10;
}
