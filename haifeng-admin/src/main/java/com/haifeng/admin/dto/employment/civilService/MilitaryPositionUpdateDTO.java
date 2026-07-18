package com.haifeng.admin.dto.employment.civilService;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MilitaryPositionUpdateDTO {
    @Size(max = 200, message = "岗位名称长度不能超过200")
    private String positionName;
    @Size(max = 200, message = "用人单位长度不能超过200")
    private String employerUnit;
    @Size(max = 200, message = "科室长度不能超过200")
    private String department;
    @Size(max = 50, message = "岗位类型长度不能超过50")
    private String positionType;
    @Size(max = 100, message = "工作地点长度不能超过100")
    private String workLocation;
    @Size(max = 50, message = "薪资范围长度不能超过50")
    private String salaryRange;
    @Size(max = 500, message = "专业要求长度不能超过500")
    private String majorRequirement;
    @Size(max = 30, message = "学历要求长度不能超过30")
    private String educationRequirement;
    @Size(max = 30, message = "报名截止长度不能超过30")
    private String regDeadline;
    @Size(max = 20, message = "状态长度不能超过20")
    private String positionStatus;
    private String positionDescription;
    private String[] responsibilities;
    private String[] qualifications;
    private Integer sortOrder;
}
