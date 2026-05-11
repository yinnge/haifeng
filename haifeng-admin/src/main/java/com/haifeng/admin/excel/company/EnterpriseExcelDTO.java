package com.haifeng.admin.excel.company;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 企业主表导入DTO
 */
@Data
public class EnterpriseExcelDTO {

    @ExcelProperty("城市名称")
    private String cityName;

    @ExcelProperty("企业名称")
    private String enterpriseName;

    @ExcelProperty("企业性质")
    private String enterpriseNature;

    @ExcelProperty("企业类型")
    private String enterpriseType;

    @ExcelProperty("Logo地址")
    private String logoUrl;

    @ExcelProperty("官网")
    private String officialWebsite;

    @ExcelProperty("总部地区")
    private String region;

    @ExcelProperty("企业规模")
    private String enterpriseScale;

    @ExcelProperty("主营业务")
    private String mainBusiness;

    @ExcelProperty("企业简介")
    private String enterpriseIntro;

    @ExcelProperty("招聘状态")
    private String recruitmentStatus;
}
