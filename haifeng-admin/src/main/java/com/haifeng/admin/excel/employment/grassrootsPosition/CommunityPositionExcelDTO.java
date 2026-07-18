package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.OffsetDateTimeConverter;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class CommunityPositionExcelDTO {
    @ExcelProperty("街道办事处/乡镇") private String streetOffice;
    @ExcelProperty("社区名称") private String communityName;
    @ExcelProperty("主管部门") private String supervisingDept;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("岗位类型") private String positionType;
    @ExcelProperty("用工形式") private String employmentType;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("工作经验") private String workExperience;
    @ExcelProperty("社工证要求") private String socialWorkCert;
    @ExcelProperty("社区经验要求") private String communityExperience;
    @ExcelProperty("居住地要求") private String residenceRequirement;
    @ExcelProperty("薪资待遇") private String salaryRange;
    @ExcelProperty("薪资构成") private String salaryComposition;
    @ExcelProperty("福利待遇") private String benefits;
    @ExcelProperty("笔试内容") private String examContent;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty(value = "报名开始", converter = OffsetDateTimeConverter.class) private OffsetDateTime regStartDate;
    @ExcelProperty(value = "报名截止", converter = OffsetDateTimeConverter.class) private OffsetDateTime regEndDate;
    @ExcelProperty(value = "考试时间", converter = OffsetDateTimeConverter.class) private OffsetDateTime examTime;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("报名方式") private String applyMethod;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("报名地址") private String contactAddress;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
