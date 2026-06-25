package com.haifeng.admin.dto.system;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class ModelProviderUpdateDTO {

    @ToString.Exclude
    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 50, message = "供应商名称长度不能超过50")
    private String providerName;

    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;
}
