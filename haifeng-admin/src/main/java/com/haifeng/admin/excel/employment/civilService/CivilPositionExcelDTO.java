package com.haifeng.admin.excel.employment.civilService;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CivilPositionExcelDTO {
    @ExcelProperty("职位名称") private String positionName;
    @ExcelProperty("考试类型") private String examType;
    @ExcelProperty("招录部门") private String recruitingDept;
    @ExcelProperty("部门代码") private String deptCode;
    @ExcelProperty("职位代码") private String positionCode;
    @ExcelProperty("隶属局") private String affiliatedBureau;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("最低学历") private String minEducation;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("工作经验") private String workExperience;
    @ExcelProperty("基层经验") private String grassrootsExperience;
    @ExcelProperty("考试类别") private String examCategory;
    @ExcelProperty("面试比例") private String interviewRatio;
    @ExcelProperty("招录人数") private Integer recruitmentCount;
    @ExcelProperty("专业考试") private Boolean hasProfessionalTest;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("工作地点详情") private String workLocationDetail;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("户籍所在地") private String householdLocation;
    @ExcelProperty("职位简介") private String positionIntro;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("官网") private String officialWebsite;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("报名状态") private String regStatus;
    @ExcelProperty("报名人数") private Integer applicantCount;
    @ExcelProperty("排序") private Integer sortOrder;
}
