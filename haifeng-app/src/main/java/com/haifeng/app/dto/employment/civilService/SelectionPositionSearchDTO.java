package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SelectionPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Size(max = 50, message = "选调类型长度不能超过50")
    private String selectionType;
    @Size(max = 10, message = "年份长度不能超过10")
    private String year;
    @Size(max = 20, message = "省份长度不能超过20")
    private String province;
    @Size(max = 20, message = "学历要求长度不能超过20")
    private String educationRequirement;
    @Size(max = 20, message = "学位要求长度不能超过20")
    private String degreeRequirement;
    @Size(max = 20, message = "政治面貌长度不能超过20")
    private String politicalStatus;
    @Size(max = 100, message = "专业要求长度不能超过100")
    private String majorRequirement;
    @Size(max = 100, message = "院校要求长度不能超过100")
    private String universityRequirement;
    @Min(value = 0, message = "年龄不能为负数")
    private Integer ageLimit;
    @Size(max = 20, message = "岗位状态长度不能超过20")
    private String positionStatus;
}
