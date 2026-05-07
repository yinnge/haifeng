package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UniversityExcelDTO {

    @ExcelProperty("院校名称")
    private String name;

    @ExcelProperty("院校名称英文")
    private String nameEn;

    @ExcelProperty("省份")
    private String provinceName;

    @ExcelProperty("城市")
    private String cityName;

    @ExcelProperty("所属地区")
    private String region;

    @ExcelProperty("院校类别")
    private String category;

    @ExcelProperty("专业数量")
    private Integer majorCount;

    @ExcelProperty("办学层次")
    private String educationLevel;

    @ExcelProperty("院校性质")
    private String nature;

    @ExcelProperty("是否有博士点")
    private Boolean hasDoctorate;

    @ExcelProperty("是否有硕士点")
    private Boolean hasMaster;

    @ExcelProperty("隶属部门")
    private String department;

    @ExcelProperty(value = "院校标签", converter = StringArrayConverter.class)
    private List<String> tags;

    @ExcelProperty("知名联盟")
    private String famousUnion;

    @ExcelProperty("院校图片URL")
    private String imageUrl;

    @ExcelProperty("院校简介")
    private String introduction;

    @ExcelProperty("推免率")
    private BigDecimal recommendationRate;

    @ExcelProperty("推免年份")
    private Integer recommendationYear;
}
