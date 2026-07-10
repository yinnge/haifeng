package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CivilPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Size(max = 20, message = "考试类型长度不能超过20")
    private String examType;
    @Size(max = 50, message = "职位代码长度不能超过50")
    private String positionCode;
    @Size(max = 50, message = "部门代码长度不能超过50")
    private String deptCode;
    @Size(max = 20, message = "最低学历长度不能超过20")
    private String minEducation;
    @Size(max = 20, message = "学位要求长度不能超过20")
    private String degreeRequirement;
    @Size(max = 20, message = "政治面貌长度不能超过20")
    private String politicalStatus;
    @Size(max = 50, message = "考试类别长度不能超过50")
    private String examCategory;
    @Size(max = 100, message = "专业要求长度不能超过100")
    private String majorRequirement;
}
