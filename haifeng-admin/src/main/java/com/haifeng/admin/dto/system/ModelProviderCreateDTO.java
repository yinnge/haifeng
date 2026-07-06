package com.haifeng.admin.dto.system;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class ModelProviderCreateDTO {

    @ToString.Exclude
    @NotBlank(message = "API Key不能为空")
    private String apiKey;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 50, message = "供应商名称长度不能超过50")
    private String providerName;

    @NotBlank(message = "类型不能为空")
    @Size(max = 50, message = "类型长度不能超过50")
    private String type;

    @Size(max = 255, message = "描述长度不能超过255")
    private String description;

    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 1, message = "状态值最大为1")
    private Integer status;
}
