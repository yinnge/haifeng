package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "院校名称不能超过50个字符")
    private String name;

    @Size(max = 50, message = "省份名称不能超过50个字符")
    private String provinceName;

    @Size(max = 50, message = "院校类别不能超过50个字符")
    private String category;

    private Integer status;
}
