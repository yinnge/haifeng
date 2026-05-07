package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class LaboratoryExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("实验室名称")
    private String name;

    @ExcelProperty("实验室类型")
    private String labType;

    @ExcelProperty("成立时间")
    private String establishedYear;

    @ExcelProperty("所在地区")
    private String region;

    @ExcelProperty("主管部门")
    private String department;

    @ExcelProperty("实验室主任")
    private String director;

    @ExcelProperty("人员规模")
    private String staffCount;

    @ExcelProperty("学生规模")
    private String studentCount;

    @ExcelProperty("联系邮箱")
    private String email;

    @ExcelProperty("联系电话")
    private String phone;

    @ExcelProperty("实验室简介")
    private String introduction;

    @ExcelProperty("研究方向描述")
    private String researchDescription;

    @ExcelProperty("实验室空间")
    private String labSpace;

    @ExcelProperty("开放课题")
    private String openTopics;

    @ExcelProperty("合作交流")
    private String cooperation;

    @ExcelProperty("访问学者")
    private String visitingScholars;

    @ExcelProperty(value = "研究领域(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> researchFields;

    @ExcelProperty(value = "主要设备(逗号分隔)", converter = StringArrayConverter.class)
    private List<String> majorEquipment;

    @ExcelProperty("排序")
    private Integer sortOrder;

    @ExcelProperty("状态")
    private Integer status;
}
