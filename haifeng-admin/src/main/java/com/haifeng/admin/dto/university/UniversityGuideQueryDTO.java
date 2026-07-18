package com.haifeng.admin.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityGuideQueryDTO extends BasePageQueryDTO {

    @Size(max = 50, message = "院校名称不能超过50个字符")
    private String universityName;

    private Integer status;
}
