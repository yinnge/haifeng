package com.haifeng.admin.excel.employment.grassrootsPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class PublicWelfarePositionExcelDTO {
    @ExcelProperty("开发单位") private String developingUnit;
    @ExcelProperty("用工单位") private String employingUnit;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("岗位类别") private String positionCategory;
    @ExcelProperty("工作内容") private String workContent;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("区/县") private String district;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty(value = "面向人群", converter = StringToArrayConverter.class) private String[] targetGroup;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("年龄范围") private String ageRange;
    @ExcelProperty("身体条件") private String healthRequirement;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("户籍要求") private String householdRequirement;
    @ExcelProperty("困难认定") private Boolean employmentDifficultyCert;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("合同期限") private String contractPeriod;
    @ExcelProperty("可续签") private Boolean isRenewable;
    @ExcelProperty("最长服务年限") private Integer maxServiceYears;
    @ExcelProperty("月工资") private String monthlySalary;
    @ExcelProperty("工资来源") private String salarySource;
    @ExcelProperty("补贴标准") private String subsidyStandard;
    @ExcelProperty("社保缴纳") private String socialInsuranceInfo;
    @ExcelProperty("其他福利") private String otherBenefits;
    @ExcelProperty("工作时间") private String workSchedule;
    @ExcelProperty("是否倒班") private Boolean isShiftWork;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("报名方式") private String applyMethod;
    @ExcelProperty("报名地址") private String applyAddress;
    @ExcelProperty("所需材料") private String requiredDocuments;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系电话") private String contactPhone;
    @ExcelProperty("联系人") private String contactPerson;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
