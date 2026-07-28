package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProvinceReformBatchStatusDTO {

    @NotEmpty(message = "请选择要操作的记录")
    @Size(max = 100, message = "最多操作100条记录")
    private List<Long> ids;

    @NotNull(message = "状态不能为空")
    private Boolean isDeleted;
}
