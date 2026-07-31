package com.haifeng.admin.dto.employment.contentManagement.guide;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamGuideQueryDTO extends BasePageQueryDTO {
    @Size(max = 50)
    private String title;

    @Size(max = 50)
    private String subtitle;

    private String guideCategory;
    private String guideType;
    private Boolean isTop;

    /** 状态筛选：0=启用，1=禁用，null=全部（含禁用） */
    private Integer status;
}
