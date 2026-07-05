package com.haifeng.admin.excel.employment.civilService;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SelectionPositionExcelDTO {
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("选调类型") private String selectionType;
    @ExcelProperty("年份") private String year;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("组织部门") private String organizingDept;
    @ExcelProperty("目标单位") private String targetUnit;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("培养方向") private String trainingDirection;
    @ExcelProperty("基层服务年限") private String grassrootsServiceYears;
    @ExcelProperty("培养计划") private String trainingPlan;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty(value = "专业类别", converter = StringToArrayConverter.class) private String[] majorCategories;
    @ExcelProperty("院校要求") private String universityRequirement;
    @ExcelProperty(value = "目标院校", converter = StringToArrayConverter.class) private String[] targetUniversities;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("学生干部要求") private String studentCadreRequirement;
    @ExcelProperty("奖项要求") private String awardsRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招录人数") private Integer recruitmentCount;
    @ExcelProperty("考试科目") private String examSubjects;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("官方链接") private String officialLink;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
