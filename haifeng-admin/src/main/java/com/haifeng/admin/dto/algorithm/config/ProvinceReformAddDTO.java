package com.haifeng.admin.dto.algorithm.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProvinceReformAddDTO {
    @NotBlank(message = "省份不能为空")
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    private Short reformYear;

    @Size(max = 20, message = "改革模式长度不能超过20")
    private String reformModel;
}
