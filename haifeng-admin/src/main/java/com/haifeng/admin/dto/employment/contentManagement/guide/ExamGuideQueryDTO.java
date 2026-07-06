package com.haifeng.admin.dto.employment.contentManagement.guide;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamGuideQueryDTO extends BasePageQueryDTO {
    private String title;
    private String subtitle;
    private String guideCategory;
    private String guideType;
    private Boolean isTop;
}
