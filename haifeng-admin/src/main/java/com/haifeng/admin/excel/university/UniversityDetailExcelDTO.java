package com.haifeng.admin.excel.university;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UniversityDetailExcelDTO {

    @ExcelProperty("院校名称")
    private String universityName;

    @ExcelProperty("学校地址")
    private String address;

    @ExcelProperty("招生电话")
    private String admissionPhone;

    @ExcelProperty("官方网站")
    private String website;

    @ExcelProperty("本科批历史组")
    private Integer historyGroupScore;

    @ExcelProperty("本科批物理组")
    private Integer scienceGroupScore;

    @ExcelProperty(value = "轮播图片URL", converter = StringArrayConverter.class)
    private List<String> carouselImages;

    @ExcelProperty("院校详细介绍")
    private String introduction;

    @ExcelProperty("软科排名")
    private Integer ruanke;

    @ExcelProperty("校友会排名")
    private Integer xiaoyouhui;

    @ExcelProperty("武书连排名")
    private Integer wushulian;

    @ExcelProperty("QS排名")
    private Integer qs;

    @ExcelProperty("U.S.NEWS排名")
    private Integer usnews;

    @ExcelProperty("出国比例")
    private String abroadRate;

    @ExcelProperty("男女比例")
    private String genderRatio;
}
