package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GrassrootsProjectPositionExcelDTO {
    @ExcelProperty("项目类型") private String projectType;
    @ExcelProperty("年份") private String year;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("服务类型") private String serviceType;
    @ExcelProperty("组织单位") private String organizingDept;
    @ExcelProperty("服务单位") private String serviceUnit;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String county;
    @ExcelProperty("乡镇/街道") private String township;
    @ExcelProperty("服务期限") private String servicePeriod;
    @ExcelProperty("服务开始日期") private String serviceStartDate;
    @ExcelProperty("服务结束日期") private String serviceEndDate;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招募人数") private Integer recruitmentCount;
    @ExcelProperty("毕业年份要求") private String gradYearRequirement;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("政治面貌") private String politicalStatus;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("笔试内容") private String examContent;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("面试形式") private String interviewForm;
    @ExcelProperty("月补贴标准") private String monthlySubsidy;
    @ExcelProperty("社保缴纳") private String socialInsurance;
    @ExcelProperty("住房安排") private String housingInfo;
    @ExcelProperty("其他待遇") private String otherBenefits;
    @ExcelProperty("期满政策") private String afterServicePolicy;
    @ExcelProperty("可定向考公") private Boolean canTransferToCivil;
    @ExcelProperty("可转事业编") private Boolean canTransferToInstitution;
    @ExcelProperty("考试加分") private String examBonusPoints;
    @ExcelProperty("学费补偿") private String tuitionCompensation;
    @ExcelProperty("考研加分") private String postgradBonus;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("报名链接") private String applyLink;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
