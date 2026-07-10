package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MilitaryPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Size(max = 50, message = "岗位类型长度不能超过50")
    private String positionType;
    @Size(max = 20, message = "学历要求长度不能超过20")
    private String educationRequirement;
    @Size(max = 20, message = "岗位状态长度不能超过20")
    private String positionStatus;
    @Size(max = 50, message = "工作地点长度不能超过50")
    private String workLocation;
    @Size(max = 100, message = "专业要求长度不能超过100")
    private String majorRequirement;
}
