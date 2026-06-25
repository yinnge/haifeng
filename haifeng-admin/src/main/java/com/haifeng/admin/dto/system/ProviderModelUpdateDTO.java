package com.haifeng.admin.dto.system;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProviderModelUpdateDTO {

    @NotBlank(message = "服务商名称不能为空")
    private String providerName;

    @NotBlank(message = "模型名称不能为空")
    private String modelName;
}
