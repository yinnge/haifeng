package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import com.haifeng.admin.converter.OffsetDateTimeConverter;
import com.haifeng.admin.excel.university.StringArrayConverter;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 企业岗位导入DTO
 */
@Data
public class EnterprisePositionExcelDTO {

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("岗位名称")
    private String positionName;

    @ExcelProperty("招聘类型")
    private String recruitmentType;

    @ExcelProperty("岗位要求")
    private String positionRequirement;

    @ExcelProperty(value = "岗位标签", converter = StringArrayConverter.class)
    private List<String> positionTags;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("城市")
    private String city;

    @ExcelProperty("工作地点")
    private String workLocation;

    @ExcelProperty("学历要求")
    private String educationRequirement;

    @ExcelProperty("专业要求")
    private String majorRequirement;

    @ExcelProperty("工作经验")
    private String workExperience;

    @ExcelProperty("最低薪资")
    private Integer salaryMin;

    @ExcelProperty("最高薪资")
    private Integer salaryMax;

    @ExcelProperty("申请链接")
    private String applyLink;

    @ExcelProperty(value = "截止日期", converter = OffsetDateTimeConverter.class)
    private OffsetDateTime deadline;

    @ExcelProperty("岗位状态")
    private String positionStatus;
}
