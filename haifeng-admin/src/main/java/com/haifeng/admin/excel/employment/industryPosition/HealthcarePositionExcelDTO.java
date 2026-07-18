package com.haifeng.admin.excel.employment.industryPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.OffsetDateTimeConverter;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HealthcarePositionExcelDTO {
    @ExcelProperty("医疗机构名称") private String institutionName;
    @ExcelProperty("机构类型") private String institutionType;
    @ExcelProperty("机构等级") private String institutionLevel;
    @ExcelProperty("机构性质") private String institutionNature;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("科室") private String department;
    @ExcelProperty("岗位类别") private String positionCategory;
    @ExcelProperty("招聘类型") private String recruitmentType;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("工作经验要求") private String workExperience;
    @ExcelProperty("执业资格要求") private String licenseRequirement;
    @ExcelProperty("职称要求") private String titleRequirement;
    @ExcelProperty("规培要求") private String internshipRequirement;
    @ExcelProperty("科研要求") private String researchRequirement;
    @ExcelProperty("薪资待遇") private String salaryRange;
    @ExcelProperty("福利待遇") private String benefits;
    @ExcelProperty("住房补贴") private String housingSubsidy;
    @ExcelProperty(value = "报名开始日期", converter = OffsetDateTimeConverter.class) private OffsetDateTime regStartDate;
    @ExcelProperty(value = "报名截止日期", converter = OffsetDateTimeConverter.class) private OffsetDateTime regEndDate;
    @ExcelProperty(value = "考试时间", converter = OffsetDateTimeConverter.class) private OffsetDateTime examTime;
    @ExcelProperty("考试内容") private String examContent;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("联系人") private String contactPerson;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
