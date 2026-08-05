package com.haifeng.admin.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EnterprisePositionUpdateDTO {
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 200, message = "岗位名称长度不能超过200个字符")
    private String positionName;

    @Pattern(regexp = "校招|社招|实习", message = "招聘类型必须是：校招、社招、实习")
    private String recruitmentType;

    @Size(max = 500, message = "岗位要求长度不能超过500个字符")
    private String positionRequirement;

    private List<String> positionTags;

    @Size(max = 30, message = "省份长度不能超过30个字符")
    private String province;

    @Size(max = 50, message = "城市长度不能超过50个字符")
    private String city;

    @Size(max = 200, message = "工作地点长度不能超过200个字符")
    private String workLocation;

    @Pattern(regexp = "不限|大专|本科|硕士|博士", message = "学历要求必须是：不限、大专、本科、硕士、博士")
    private String educationRequirement;

    @Size(max = 500, message = "专业要求长度不能超过500个字符")
    private String majorRequirement;

    @Size(max = 50, message = "工作经验长度不能超过50个字符")
    private String workExperience;

    private Integer salaryMin;
    private Integer salaryMax;

    @Size(max = 500, message = "申请链接长度不能超过500个字符")
    private String applyLink;

    private OffsetDateTime deadline;

    @Pattern(regexp = "招聘中|已结束", message = "岗位状态必须是：招聘中、已结束")
    private String positionStatus;
}
