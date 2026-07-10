package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicWelfarePositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String positionName;

    @Size(max = 100, message = "开发单位长度不能超过100")
    private String developingUnit;

    @Size(max = 100, message = "用工单位长度不能超过100")
    private String employingUnit;

    @Size(max = 50, message = "岗位类别长度不能超过50")
    private String positionCategory;

    @Size(max = 50, message = "省份长度不能超过50")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Size(max = 50, message = "区/县长度不能超过50")
    private String district;

    @Size(max = 50, message = "学历要求长度不能超过50")
    private String educationRequirement;

    @Size(max = 50, message = "户籍要求长度不能超过50")
    private String householdRequirement;

    @Size(max = 50, message = "岗位状态长度不能超过50")
    private String positionStatus;

    @Size(max = 50, message = "面向人群长度不能超过50")
    private String targetGroup;

    @Min(value = 0, message = "最长服务年限不能为负数")
    private Integer maxServiceYears;

    @Min(value = 0, message = "年龄下限不能为负数")
    private Integer ageRangeMin;

    @Min(value = 0, message = "年龄上限不能为负数")
    private Integer ageRangeMax;
}
