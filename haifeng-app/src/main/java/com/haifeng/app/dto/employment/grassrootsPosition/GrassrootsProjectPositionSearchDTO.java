package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GrassrootsProjectPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String positionName;

    @Size(max = 100, message = "组织单位长度不能超过100")
    private String organizingDept;

    @Size(max = 100, message = "服务单位长度不能超过100")
    private String serviceUnit;

    @Size(max = 50, message = "项目类型长度不能超过50")
    private String projectType;

    @Size(max = 10, message = "年份长度不能超过10")
    private String year;

    @Size(max = 50, message = "服务类型长度不能超过50")
    private String serviceType;

    @Size(max = 50, message = "省份长度不能超过50")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Size(max = 50, message = "区/县长度不能超过50")
    private String county;

    @Size(max = 50, message = "学历要求长度不能超过50")
    private String educationRequirement;

    @Size(max = 100, message = "专业要求长度不能超过100")
    private String majorRequirement;

    @Size(max = 50, message = "毕业年份要求长度不能超过50")
    private String gradYearRequirement;

    @Size(max = 50, message = "政治面貌长度不能超过50")
    private String politicalStatus;

    @Size(max = 50, message = "岗位状态长度不能超过50")
    private String positionStatus;

    @Min(value = 0, message = "年龄下限不能为负数")
    private Integer ageLimitMin;

    @Min(value = 0, message = "年龄上限不能为负数")
    private Integer ageLimitMax;
}
