package com.haifeng.admin.dto.employment.civilService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CivilPositionUpdateDTO {
    @Size(max = 200, message = "职位名称最多200字符")
    private String positionName;

    @Pattern(regexp = "国考|省考", message = "考试类型只能是 国考 或 省考")
    private String examType;

    @Size(max = 200, message = "招录部门最多200字符")
    private String recruitingDept;

    @Size(max = 30, message = "部门代码最多30字符")
    private String deptCode;

    @Size(max = 30, message = "职位代码最多30字符")
    private String positionCode;

    @Size(max = 200, message = "所属司局最多200字符")
    private String affiliatedBureau;

    @Size(max = 500, message = "专业要求最多500字符")
    private String majorRequirement;

    @Pattern(regexp = "不限|大专|本科|硕士|博士", message = "学历要求只能是 不限/大专/本科/硕士/博士")
    private String minEducation;

    @Pattern(regexp = "不限|学士|硕士|博士", message = "学位要求只能是 不限/学士/硕士/博士")
    private String degreeRequirement;

    @Pattern(regexp = "不限|中共党员|共青团员|群众", message = "政治面貌只能是 不限/中共党员/共青团员/群众")
    private String politicalStatus;

    @Size(max = 50, message = "工作年限要求最多50字符")
    private String workExperience;

    @Size(max = 50, message = "基层经验要求最多50字符")
    private String grassrootsExperience;

    @Size(max = 50, message = "考试类别最多50字符")
    private String examCategory;

    @Size(max = 20, message = "面试比例最多20字符")
    private String interviewRatio;

    @Min(value = 1, message = "招录人数最少1人")
    private Integer recruitmentCount;

    private Boolean hasProfessionalTest;

    @Size(max = 100, message = "工作地点最多100字符")
    private String workLocation;

    @Size(max = 200, message = "工作地点详情最多200字符")
    private String workLocationDetail;

    @Size(max = 100, message = "户籍要求最多100字符")
    private String householdRequirement;

    @Size(max = 100, message = "户籍所在地最多100字符")
    private String householdLocation;

    private String positionIntro;
    private String remark;

    @Size(max = 500, message = "官网地址最多500字符")
    private String officialWebsite;

    @Size(max = 50, message = "联系电话最多50字符")
    private String contactPhone;

    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;

    @Pattern(regexp = "报名中|已结束|即将开始", message = "报名状态只能是 报名中/已结束/即将开始")
    private String regStatus;

    @Min(value = 0, message = "报名人数不能为负")
    private Integer applicantCount;

    @Min(value = 0, message = "排序值不能为负")
    private Integer sortOrder;
}
