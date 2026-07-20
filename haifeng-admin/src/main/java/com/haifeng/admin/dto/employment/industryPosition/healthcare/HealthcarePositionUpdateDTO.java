package com.haifeng.admin.dto.employment.industryPosition.healthcare;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HealthcarePositionUpdateDTO {
    @Size(max = 200, message = "医疗机构名称最长200字符")
    private String institutionName;
    @Size(max = 50, message = "机构类型最长50字符")
    private String institutionType;
    @Size(max = 30, message = "机构等级最长30字符")
    private String institutionLevel;
    @Size(max = 20, message = "机构性质最长20字符")
    private String institutionNature;
    @Size(max = 200, message = "岗位名称最长200字符")
    private String positionName;
    @Size(max = 100, message = "科室最长100字符")
    private String department;
    @Size(max = 30, message = "岗位类别最长30字符")
    private String positionCategory;
    @Size(max = 30, message = "招聘类型最长30字符")
    private String recruitmentType;
    @Size(max = 30, message = "省份最长30字符")
    private String province;
    @Size(max = 50, message = "城市最长50字符")
    private String city;
    @Size(max = 50, message = "区/县最长50字符")
    private String district;
    @Size(max = 30, message = "学历要求最长30字符")
    private String educationRequirement;
    @Size(max = 30, message = "学位要求最长30字符")
    private String degreeRequirement;
    @Size(max = 500, message = "专业要求最长500字符")
    private String majorRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    @Size(max = 50, message = "工作经验最长50字符")
    private String workExperience;
    @Size(max = 100, message = "执业资格要求最长100字符")
    private String licenseRequirement;
    @Size(max = 30, message = "职称要求最长30字符")
    private String titleRequirement;
    @Size(max = 50, message = "规培要求最长50字符")
    private String internshipRequirement;
    private String researchRequirement;
    @Size(max = 50, message = "薪资待遇最长50字符")
    private String salaryRange;
    private String benefits;
    @Size(max = 100, message = "住房补贴最长100字符")
    private String housingSubsidy;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    @Size(max = 500, message = "考试内容最长500字符")
    private String examContent;
    @Size(max = 500, message = "报名链接最长500字符")
    private String applyLink;
    @Size(max = 20, message = "岗位状态最长20字符")
    private String positionStatus;
    @Size(max = 50, message = "联系电话最长50字符")
    private String contactPhone;
    @Size(max = 50, message = "联系人最长50字符")
    private String contactPerson;
    private String remark;
    private String content;
    private Integer sortOrder;
}
