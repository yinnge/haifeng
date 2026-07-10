package com.haifeng.app.dto.employment.grassrootsPosition;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommunityPositionSearchDTO extends BasePageQueryDTO {

    @Size(max = 100, message = "岗位名称长度不能超过100")
    private String positionName;

    @Size(max = 100, message = "街道办事处/乡镇长度不能超过100")
    private String streetOffice;

    @Size(max = 100, message = "社区名称长度不能超过100")
    private String communityName;

    @Size(max = 100, message = "主管部门长度不能超过100")
    private String supervisingDept;

    @Size(max = 50, message = "岗位类型长度不能超过50")
    private String positionType;

    @Size(max = 50, message = "用工形式长度不能超过50")
    private String employmentType;

    @Size(max = 50, message = "省份长度不能超过50")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50")
    private String city;

    @Size(max = 50, message = "学历要求长度不能超过50")
    private String educationRequirement;

    @Size(max = 100, message = "专业要求长度不能超过100")
    private String majorRequirement;

    @Size(max = 50, message = "政治面貌长度不能超过50")
    private String politicalStatus;

    @Size(max = 50, message = "工作经验长度不能超过50")
    private String workExperience;

    @Size(max = 50, message = "岗位状态长度不能超过50")
    private String positionStatus;

    @Min(value = 0, message = "年龄下限不能为负数")
    private Integer ageLimitMin;

    @Min(value = 0, message = "年龄上限不能为负数")
    private Integer ageLimitMax;
}
