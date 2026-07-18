package com.haifeng.admin.dto.employment.civilService;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SelectionPositionUpdateDTO {
    @Size(max = 200, message = "岗位名称长度不能超过200")
    private String positionName;
    @Size(max = 50, message = "选调类型长度不能超过50")
    private String selectionType;
    @Size(max = 10, message = "年份长度不能超过10")
    private String year;
    @Size(max = 30, message = "省份长度不能超过30")
    private String province;
    @Size(max = 200, message = "组织部门长度不能超过200")
    private String organizingDept;
    @Size(max = 200, message = "目标单位长度不能超过200")
    private String targetUnit;
    @Size(max = 200, message = "工作地点长度不能超过200")
    private String workLocation;
    @Size(max = 100, message = "培养方向长度不能超过100")
    private String trainingDirection;
    @Size(max = 30, message = "基层服务年限长度不能超过30")
    private String grassrootsServiceYears;
    private String trainingPlan;
    @Size(max = 30, message = "学历要求长度不能超过30")
    private String educationRequirement;
    @Size(max = 30, message = "学位要求长度不能超过30")
    private String degreeRequirement;
    @Size(max = 500, message = "专业要求长度不能超过500")
    private String majorRequirement;
    private String[] majorCategories;
    @Size(max = 100, message = "院校要求长度不能超过100")
    private String universityRequirement;
    private String[] targetUniversities;
    @Size(max = 30, message = "政治面貌长度不能超过30")
    private String politicalStatus;
    @Size(max = 200, message = "学生干部要求长度不能超过200")
    private String studentCadreRequirement;
    private String awardsRequirement;
    private Integer ageLimit;
    private Integer recruitmentCount;
    @Size(max = 200, message = "考试科目长度不能超过200")
    private String examSubjects;
    @Size(max = 100, message = "面试形式长度不能超过100")
    private String interviewForm;
    private OffsetDateTime regStartDate;
    private OffsetDateTime regEndDate;
    private OffsetDateTime examTime;
    @Size(max = 500, message = "报名链接长度不能超过500")
    private String applyLink;
    @Size(max = 20, message = "状态长度不能超过20")
    private String positionStatus;
    private String remark;
    @Size(max = 50, message = "联系电话长度不能超过50")
    private String contactPhone;
    @Size(max = 500, message = "官方链接长度不能超过500")
    private String officialLink;
    private String content;
    private Integer sortOrder;
}
