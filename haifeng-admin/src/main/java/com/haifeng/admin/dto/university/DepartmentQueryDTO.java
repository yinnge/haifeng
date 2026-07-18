package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DepartmentQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "院校名称长度不能超过50")
    private String universityName;
    @Size(max = 50, message = "院系名称长度不能超过50")
    private String departmentName;
    private String departmentType;
    private Integer status;
}
