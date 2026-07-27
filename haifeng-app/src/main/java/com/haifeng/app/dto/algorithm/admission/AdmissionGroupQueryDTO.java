package com.haifeng.app.dto.algorithm.admission;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdmissionGroupQueryDTO extends BasePageQueryDTO {

    @NotBlank(message = "批次不能为空")
    @Size(max = 50, message = "批次名称不能超过50字")
    private String batch;

    private Boolean subjectFilter = false;

    @Size(max = 50, message = "院校名称不能超过50字")
    private String universityName;

    @Size(max = 50, message = "城市名称不能超过50字")
    private String cityName;

    @Size(max = 100, message = "专业组名称不能超过100字")
    private String groupName;

    @Size(max = 30, message = "招生代码不能超过30字")
    private String enrollmentCode;
}
