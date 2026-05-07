package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryQueryDTO extends BasePageQueryDTO {
    private String universityName;
    private String name;
    private String labType;
    private String region;
    private String department;
    private Integer status;
}
