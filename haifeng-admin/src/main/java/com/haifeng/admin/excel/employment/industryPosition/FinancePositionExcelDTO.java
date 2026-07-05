package com.haifeng.admin.excel.employment.industryPosition;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class FinancePositionExcelDTO {
    @ExcelProperty("机构名称") private String institutionName;
    @ExcelProperty("机构大类") private String institutionCategory;
    @ExcelProperty("机构细分类型") private String institutionType;
    @ExcelProperty("机构Logo") private String institutionLogo;
    @ExcelProperty("分支机构名称") private String branchName;
    @ExcelProperty("岗位名称") private String positionName;
    @ExcelProperty("岗位类别") private String positionCategory;
    @ExcelProperty("招聘类型") private String recruitmentType;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("详细工作地点") private String workLocation;
    @ExcelProperty("是否支持远程") private Boolean isRemote;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("专业要求") private String majorRequirement;
    @ExcelProperty(value = "优先专业", converter = StringArrayConverter.class) private List<String> majorPreference;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("工作经验要求") private String workExperience;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty(value = "证书要求", converter = StringArrayConverter.class) private List<String> certRequirements;
    @ExcelProperty("语言要求") private String languageRequirement;
    @ExcelProperty("计算机要求") private String computerRequirement;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("最低月薪") private Integer salaryMin;
    @ExcelProperty("最高月薪") private Integer salaryMax;
    @ExcelProperty("薪资文本说明") private String salaryText;
    @ExcelProperty("福利待遇") private String benefits;
    @ExcelProperty("考试内容") private String examContent;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("面试轮次说明") private String interviewRounds;
    @ExcelProperty("报名开始") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止") private OffsetDateTime regEndDate;
    @ExcelProperty("网申链接") private String applyLink;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("联系方式") private String contactInfo;
    @ExcelProperty("备注") private String remark;
    @ExcelProperty("详细说明") private String content;
    @ExcelProperty("排序") private Integer sortOrder;
}
