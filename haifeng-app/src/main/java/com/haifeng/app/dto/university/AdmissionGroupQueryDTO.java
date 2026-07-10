package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {

    @Size(max = 20, message = "省份长度不能超过20")
    private String province;

    @Size(max = 20, message = "批次长度不能超过20")
    private String batch;

    @Size(max = 50, message = "城市名称长度不能超过50")
    private String cityName;
}
