package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {
    private String province;
    private String batch;
    private String cityName;
}
