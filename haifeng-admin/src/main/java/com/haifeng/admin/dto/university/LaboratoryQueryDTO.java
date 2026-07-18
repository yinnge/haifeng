package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LaboratoryQueryDTO extends BasePageQueryDTO {
    @Size(max = 50, message = "院校名称长度不能超过50")
    private String universityName;
    @Size(max = 50, message = "实验室名称长度不能超过50")
    private String name;
    @Size(max = 50, message = "实验室类型长度不能超过50")
    private String labType;
    @Size(max = 50, message = "所在地区长度不能超过50")
    private String region;
    @Size(max = 50, message = "主管部门长度不能超过50")
    private String department;
    private Integer status;
}
