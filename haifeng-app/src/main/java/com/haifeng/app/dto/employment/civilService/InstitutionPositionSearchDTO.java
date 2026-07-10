package com.haifeng.app.dto.employment.civilService;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InstitutionPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "关键词长度不能超过100")
    private String keyword;

    @Size(max = 20, message = "省份长度不能超过20")
    private String province;
    @Size(max = 50, message = "考试类别长度不能超过50")
    private String examCategory;
    @Size(max = 50, message = "职位类型长度不能超过50")
    private String positionType;
    @Size(max = 20, message = "学历要求长度不能超过20")
    private String educationRequirement;
    @Size(max = 20, message = "学位要求长度不能超过20")
    private String degreeRequirement;
    @Size(max = 20, message = "职位状态长度不能超过20")
    private String positionStatus;
    @Size(max = 50, message = "特殊岗位标识长度不能超过50")
    private String specialPosition;
    @Min(value = 0, message = "年龄不能为负数")
    private Integer ageLimit;
}
