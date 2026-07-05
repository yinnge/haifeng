package com.haifeng.admin.excel.employment.civilService;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.StringToArrayConverter;
import lombok.Data;

@Data
public class InstitutionPositionExcelDTO {
    @ExcelProperty("职位名称") private String positionName;
    @ExcelProperty("主管部门") private String supervisingDept;
    @ExcelProperty("事业单位") private String institution;
    @ExcelProperty("工作地点") private String workLocation;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("考试类别") private String examCategory;
    @ExcelProperty("岗位类型") private String positionType;
    @ExcelProperty("子分类") private String subCategory;
    @ExcelProperty("学历要求") private String educationRequirement;
    @ExcelProperty("学位要求") private String degreeRequirement;
    @ExcelProperty("年龄上限") private Integer ageLimit;
    @ExcelProperty("招聘人数") private Integer recruitmentCount;
    @ExcelProperty("薪资范围") private String salaryRange;
    @ExcelProperty("报名截止") private String regDeadline;
    @ExcelProperty(value = "专业要求", converter = StringToArrayConverter.class) private String[] majorRequirements;
    @ExcelProperty("特殊岗位") private String specialPosition;
    @ExcelProperty("其他要求") private String otherRequirement;
    @ExcelProperty("其他要求说明") private String otherRequirementDesc;
    @ExcelProperty("备注类型") private String remarkType;
    @ExcelProperty("备注说明") private String remarkDesc;
    @ExcelProperty("咨询电话") private String consultationPhone;
    @ExcelProperty("监督电话") private String supervisionPhone;
    @ExcelProperty("状态") private String positionStatus;
    @ExcelProperty("标签") private String positionTag;
    @ExcelProperty("标签文字") private String tagText;
    @ExcelProperty("排序") private Integer sortOrder;
}
