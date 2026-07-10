package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MajorListQueryDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "专业名称最长100个字符")
    private String name;

    @Size(max = 20, message = "专业代码最长20个字符")
    private String code;

    @Size(max = 50, message = "专业类型最长50个字符")
    private String majorType;

    @Size(max = 50, message = "专业类别最长50个字符")
    private String majorCategory;
}
