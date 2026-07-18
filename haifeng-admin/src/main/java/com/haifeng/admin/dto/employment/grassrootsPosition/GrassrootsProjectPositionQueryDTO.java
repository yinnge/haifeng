package com.haifeng.admin.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String positionName;

    @Size(max = 50)
    private String organizingDept;

    @Size(max = 50)
    private String serviceUnit;

    @Size(max = 30)
    private String projectType;
    @Size(max = 10)
    private String year;
    @Size(max = 50)
    private String serviceType;
    @Size(max = 30)
    private String province;
    @Size(max = 50)
    private String city;
    @Size(max = 50)
    private String county;
    @Size(max = 20)
    private String positionStatus;
}
